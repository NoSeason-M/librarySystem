package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookCopyMapper bookCopyMapper;
    private final ReaderMapper readerMapper;

    /**
     * Get reader's current borrowing
     * GET /api/borrow/current
     */
    public List<Map<String, Object>> getCurrentBorrowing(String readerNo) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) return List.of();

        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getReaderId, reader.getId())
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .orderByDesc(BorrowRecord::getCreateTime));

        return records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("bookInfoId", r.getBookInfoId());

            BookInfo book = bookInfoMapper.selectById(r.getBookInfoId());
            if (book != null) {
                item.put("bookTitle", book.getTitle());
                item.put("bookAuthor", book.getAuthor());
                item.put("coverUrl", book.getCoverUrl());
            }

            BookCopy copy = bookCopyMapper.selectById(r.getBookCopyId());
            if (copy != null) {
                item.put("barcode", copy.getBarcode());
            }

            item.put("borrowDate", r.getBorrowDate().toLocalDate().toString());
            item.put("dueDate", r.getDueDate().toString());
            item.put("renewCount", r.getRenewCount());

            long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), r.getDueDate());
            boolean overdue = remainingDays < 0;
            item.put("remainingDays", overdue ? 0 : remainingDays);
            item.put("overdue", overdue);
            item.put("overdueDays", overdue ? (int) Math.abs(remainingDays) : 0);
            item.put("canRenew", !overdue && r.getRenewCount() < 3 && remainingDays <= 7);

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Get reader borrowing summary stats
     */
    public Map<String, Object> getReaderSummary(String readerNo) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) {
            return Map.of("currentBorrowed", 0, "overdueCount", 0,
                    "pendingReservationCount", 0, "unpaidFineAmount", "¥0.00");
        }

        long currentBorrowed = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getReaderId, reader.getId())
                        .eq(BorrowRecord::getStatus, "borrowed"));

        long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getReaderId, reader.getId())
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, LocalDate.now()));

        Map<String, Object> result = new HashMap<>();
        result.put("currentBorrowed", currentBorrowed);
        result.put("overdueCount", overdueCount);
        result.put("pendingReservationCount", 0);
        result.put("unpaidFineAmount", "¥0.00");
        return result;
    }
}
