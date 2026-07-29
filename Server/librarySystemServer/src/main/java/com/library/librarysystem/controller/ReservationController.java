package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.mapper.BookInfoMapper;
import com.library.librarysystem.mapper.ReaderMapper;
import com.library.librarysystem.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationMapper reservationMapper;
    private final ReaderMapper readerMapper;
    private final BookInfoMapper bookInfoMapper;

    @GetMapping("/current")
    public Result<List<Map<String, Object>>> current(@RequestParam String readerNo) {
        com.library.librarysystem.entity.Reader reader = readerMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.library.librarysystem.entity.Reader>()
                        .eq(com.library.librarysystem.entity.Reader::getReaderNo, readerNo));
        if (reader == null) return Result.success(List.of());

        return Result.success(reservationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.library.librarysystem.entity.Reservation>()
                        .eq(com.library.librarysystem.entity.Reservation::getReaderId, reader.getId())
                        .orderByDesc(com.library.librarysystem.entity.Reservation::getReserveDate)
        ).stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("bookInfoId", r.getBookInfoId());
            var book = bookInfoMapper.selectById(r.getBookInfoId());
            if (book != null) {
                item.put("bookTitle", book.getTitle());
                item.put("author", book.getAuthor());
            }
            item.put("reserveDate", r.getReserveDate() != null ? r.getReserveDate().toLocalDate().toString() : "");
            item.put("expireDate", r.getExpireDate() != null ? r.getExpireDate().toLocalDate().toString() : "");
            item.put("status", switch (r.getStatus()) {
                case "waiting" -> "等待中";
                case "ready" -> "待取书";
                case "fulfilled" -> "已完成";
                case "cancelled" -> "已取消";
                case "expired" -> "已过期";
                default -> r.getStatus();
            });
            item.put("queuePosition", 0);
            return item;
        }).collect(Collectors.toList()));
    }
}
