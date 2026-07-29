package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineRecordMapper fineRecordMapper;
    private final ReaderMapper readerMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam String readerNo,
            @RequestParam(required = false) Integer paid) {
        com.library.librarysystem.entity.Reader reader = readerMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.library.librarysystem.entity.Reader>()
                        .eq(com.library.librarysystem.entity.Reader::getReaderNo, readerNo));
        if (reader == null) return Result.success(List.of());

        var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.library.librarysystem.entity.FineRecord>()
                .eq(com.library.librarysystem.entity.FineRecord::getReaderId, reader.getId())
                .orderByDesc(com.library.librarysystem.entity.FineRecord::getCreateTime);

        if (paid != null) {
            qw.eq(com.library.librarysystem.entity.FineRecord::getPaid, paid);
        }

        return Result.success(fineRecordMapper.selectList(qw).stream().map(f -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("fineType", switch (f.getFineType()) {
                case "overdue" -> "逾期罚款";
                case "damage" -> "损坏赔偿";
                case "lost" -> "丢失赔偿";
                default -> f.getFineType();
            });
            item.put("amount", f.getAmount());
            item.put("paid", f.getPaid() == 1);
            item.put("createTime", f.getCreateTime() != null ? f.getCreateTime().toLocalDate().toString() : "");
            if (f.getBorrowRecordId() != null) {
                var record = borrowRecordMapper.selectById(f.getBorrowRecordId());
                if (record != null) {
                    var book = bookInfoMapper.selectById(record.getBookInfoId());
                    item.put("bookTitle", book != null ? book.getTitle() : "");
                }
            }
            // Calculate overdue days from borrow record
            if (f.getBorrowRecordId() != null) {
                var record = borrowRecordMapper.selectById(f.getBorrowRecordId());
                if (record != null && record.getDueDate() != null && record.getReturnDate() != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate().toLocalDate());
                    item.put("overdueDays", days > 0 ? days : 0);
                } else {
                    item.put("overdueDays", 0);
                }
            }
            return item;
        }).collect(Collectors.toList()));
    }
}
