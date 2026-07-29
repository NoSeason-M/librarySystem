package com.library.librarysystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Favorite;
import com.library.librarysystem.entity.BookInfo;
import com.library.librarysystem.entity.Reader;
import com.library.librarysystem.mapper.FavoriteMapper;
import com.library.librarysystem.mapper.BookInfoMapper;
import com.library.librarysystem.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;
    private final ReaderMapper readerMapper;
    private final BookInfoMapper bookInfoMapper;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam String readerNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) return Result.success(Map.of("records", List.of(), "total", 0));

        IPage<Favorite> p = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getReaderId, reader.getId())
                        .orderByDesc(Favorite::getCreateTime));

        List<Map<String, Object>> records = p.getRecords().stream().map(f -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("bookInfoId", f.getBookInfoId());
            BookInfo book = bookInfoMapper.selectById(f.getBookInfoId());
            if (book != null) {
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("coverUrl", book.getCoverUrl());
            }
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        return Result.success(result);
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam Long bookInfoId, @RequestParam String readerNo) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        boolean favorited = reader != null && favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getReaderId, reader.getId())
                        .eq(Favorite::getBookInfoId, bookInfoId)) > 0;
        return Result.success(Map.of("favorited", favorited));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> req) {
        Long bookInfoId = ((Number) req.get("bookInfoId")).longValue();
        String readerNo = (String) req.get("readerNo");
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) return Result.badRequest("Reader not found");

        Favorite fav = new Favorite();
        fav.setReaderId(reader.getId());
        fav.setBookInfoId(bookInfoId);
        favoriteMapper.insert(fav);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> remove(@RequestParam Long bookInfoId, @RequestParam String readerNo) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader != null) {
            favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getReaderId, reader.getId())
                    .eq(Favorite::getBookInfoId, bookInfoId));
        }
        return Result.success();
    }
}
