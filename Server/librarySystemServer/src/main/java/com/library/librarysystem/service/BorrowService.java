package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ReaderTypeMapper readerTypeMapper;
    private final FineRecordMapper fineRecordMapper;
    private final SysUserMapper userMapper;
    private final ReservationMapper reservationMapper;
    private final SysConfigMapper configMapper;

    // ==================== Reader-facing ====================

    /**
     * Get reader's borrowing history
     */
    public List<Map<String, Object>> getBorrowHistory(String readerNo, String startDate, String endDate) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) return List.of();

        LambdaQueryWrapper<BorrowRecord> qw = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getReaderId, reader.getId())
                .eq(BorrowRecord::getStatus, "returned")
                .orderByDesc(BorrowRecord::getCreateTime);

        if (startDate != null && !startDate.isEmpty()) {
            qw.ge(BorrowRecord::getBorrowDate, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le(BorrowRecord::getBorrowDate, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }

        return borrowRecordMapper.selectList(qw).stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            BookInfo book = bookInfoMapper.selectById(r.getBookInfoId());
            item.put("bookTitle", book != null ? book.getTitle() : "");
            BookCopy copy = bookCopyMapper.selectById(r.getBookCopyId());
            item.put("barcode", copy != null ? copy.getBarcode() : "");
            item.put("borrowDate", r.getBorrowDate() != null ? r.getBorrowDate().toLocalDate().toString() : "");
            item.put("dueDate", r.getDueDate() != null ? r.getDueDate().toString() : "");
            item.put("returnDate", r.getReturnDate() != null ? r.getReturnDate().toLocalDate().toString() : "");
            boolean wasOverdue = r.getDueDate() != null && r.getReturnDate() != null
                    && r.getReturnDate().toLocalDate().isAfter(r.getDueDate());
            item.put("status", wasOverdue ? "逾期归还" : "已归还");
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Get reader's current borrowing
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

    // ==================== Admin: Borrow ====================

    /**
     * Look up reader by card number for borrowing.
     */
    public Map<String, Object> checkReader(String readerNo) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) throw new BusinessException(404, "Reader not found");

        if (reader.getCardStatus() == 0) throw new BusinessException("Reader card is lost");
        if (reader.getCardStatus() == 2) throw new BusinessException("Reader card is frozen");

        SysUser user = userMapper.selectById(reader.getUserId());
        ReaderType rt = reader.getReaderTypeId() != null ?
                readerTypeMapper.selectById(reader.getReaderTypeId()) : null;

        int maxBorrow = rt != null && rt.getMaxBorrow() != null ? rt.getMaxBorrow() : 5;
        int borrowDays = rt != null && rt.getBorrowDays() != null ? rt.getBorrowDays() : 30;
        int currentBorrowed = reader.getCurrentBorrowed() != null ? reader.getCurrentBorrowed() : 0;

        long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getReaderId, reader.getId())
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, LocalDate.now()));

        Map<String, Object> result = new HashMap<>();
        result.put("readerId", reader.getId());
        result.put("readerNo", reader.getReaderNo());
        result.put("realName", user != null ? user.getRealName() : "");
        result.put("readerTypeName", rt != null ? rt.getName() : "");
        result.put("maxBorrow", maxBorrow);
        result.put("borrowDays", borrowDays);
        result.put("currentBorrowed", currentBorrowed);
        result.put("overdueCount", overdueCount);
        result.put("cardStatus", reader.getCardStatus());
        result.put("cardStatusLabel", getCardStatusLabel(reader.getCardStatus()));
        return result;
    }

    /**
     * Check a barcode is borrowable — returns book info.
     */
    public Map<String, Object> checkBarcode(String barcode) {
        BookCopy copy = bookCopyMapper.selectOne(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, barcode));
        if (copy == null) throw new BusinessException(404, "Book copy not found");

        if (!"in".equals(copy.getStatus())) {
            throw new BusinessException("Book is " + getCopyStatusLabel(copy.getStatus()) + ", cannot borrow");
        }

        BookInfo book = bookInfoMapper.selectById(copy.getBookId());
        if (book == null || book.getStatus() != 1) throw new BusinessException("Book is not active");

        Map<String, Object> result = new HashMap<>();
        result.put("bookCopyId", copy.getId());
        result.put("bookInfoId", book.getId());
        result.put("barcode", copy.getBarcode());
        result.put("title", book.getTitle());
        result.put("author", book.getAuthor());
        result.put("coverUrl", book.getCoverUrl());
        result.put("isbn", book.getIsbn());
        return result;
    }

    /**
     * Execute borrow — create records for each barcode.
     * Matches API.md 5.1 response format.
     */
    @Transactional
    public Map<String, Object> borrowBooks(String readerNo, List<String> barcodes, Long operatorId) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) throw new BusinessException(404, "Reader not found");
        if (reader.getCardStatus() != 1) throw new BusinessException("Reader card is " + getCardStatusLabel(reader.getCardStatus()));

        ReaderType rt = reader.getReaderTypeId() != null ?
                readerTypeMapper.selectById(reader.getReaderTypeId()) : null;
        int maxBorrow = rt != null && rt.getMaxBorrow() != null ? rt.getMaxBorrow() : 5;
        int borrowDays = rt != null && rt.getBorrowDays() != null ? rt.getBorrowDays() : 30;
        int currentBorrowed = reader.getCurrentBorrowed() != null ? reader.getCurrentBorrowed() : 0;

        // Check overdue: PRD 4.2.1 — cannot borrow if overdue exists
        long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getReaderId, reader.getId())
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, LocalDate.now()));
        if (overdueCount > 0) throw new BusinessException("Reader has overdue books — cannot borrow until returned");

        // Check slots
        if (currentBorrowed + barcodes.size() > maxBorrow) {
            int available = maxBorrow - currentBorrowed;
            throw new BusinessException("Only " + available + " slot(s) available, tried to borrow " + barcodes.size());
        }

        LocalDate dueDate = LocalDate.now().plusDays(borrowDays);
        List<Map<String, Object>> records = new ArrayList<>();
        List<Map<String, Object>> failDetails = new ArrayList<>();
        int successCount = 0;

        for (String barcode : barcodes) {
            Map<String, Object> item = new HashMap<>();
            item.put("barcode", barcode);
            try {
                BookCopy copy = bookCopyMapper.selectOne(
                        new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, barcode));
                if (copy == null) throw new BusinessException("Copy not found: " + barcode);
                if (!"in".equals(copy.getStatus())) throw new BusinessException("Copy is " + getCopyStatusLabel(copy.getStatus()));

                BookInfo book = bookInfoMapper.selectById(copy.getBookId());
                if (book == null) throw new BusinessException("Book metadata not found");

                // Create borrow record
                BorrowRecord rec = new BorrowRecord();
                rec.setReaderId(reader.getId());
                rec.setBookCopyId(copy.getId());
                rec.setBookInfoId(book.getId());
                rec.setBorrowDate(LocalDateTime.now());
                rec.setDueDate(dueDate);
                rec.setRenewCount(0);
                rec.setStatus("borrowed");
                rec.setOperatorId(operatorId);
                borrowRecordMapper.insert(rec);

                // Update copy status
                copy.setStatus("borrowed");
                bookCopyMapper.updateById(copy);

                // Update book stats
                book.setAvailableCopies(book.getAvailableCopies() != null ? book.getAvailableCopies() - 1 : 0);
                book.setBorrowCount(book.getBorrowCount() != null ? book.getBorrowCount() + 1 : 1);
                bookInfoMapper.updateById(book);

                item.put("success", true);
                item.put("title", book.getTitle());
                item.put("dueDate", dueDate.toString());
                successCount++;
            } catch (Exception e) {
                item.put("success", false);
                item.put("title", "");
                item.put("dueDate", "");
                Map<String, Object> fail = new HashMap<>();
                fail.put("barcode", barcode);
                fail.put("reason", e.getMessage());
                failDetails.add(fail);
            }
            records.add(item);
        }

        // Update reader counts only for successful borrows
        if (successCount > 0) {
            reader.setCurrentBorrowed(currentBorrowed + successCount);
            reader.setTotalBorrowed(reader.getTotalBorrowed() != null ? reader.getTotalBorrowed() + successCount : successCount);
            readerMapper.updateById(reader);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", barcodes.size() - successCount);
        result.put("records", records);
        result.put("failDetails", failDetails);
        return result;
    }

    // ==================== Admin: Return ====================

    /**
     * Look up an active borrow record by barcode for return processing.
     */
    public Map<String, Object> findReturnByBarcode(String barcode) {
        BookCopy copy = bookCopyMapper.selectOne(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, barcode));
        if (copy == null) throw new BusinessException(404, "Book copy not found");

        if (!"borrowed".equals(copy.getStatus())) {
            throw new BusinessException("Book is " + getCopyStatusLabel(copy.getStatus()) + " — not currently borrowed");
        }

        BorrowRecord record = borrowRecordMapper.selectOne(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getBookCopyId, copy.getId())
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .orderByDesc(BorrowRecord::getCreateTime)
                        .last("LIMIT 1"));
        if (record == null) throw new BusinessException("No active borrow record for this copy");

        BookInfo book = bookInfoMapper.selectById(copy.getBookId());
        Reader reader = readerMapper.selectById(record.getReaderId());
        SysUser user = reader != null ? userMapper.selectById(reader.getUserId()) : null;

        boolean isOverdue = record.getDueDate().isBefore(LocalDate.now());
        long overdueDays = isOverdue ? ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now()) : 0;

        BigDecimal fineAmount = BigDecimal.ZERO;
        if (isOverdue) {
            ReaderType rt = reader != null && reader.getReaderTypeId() != null ?
                    readerTypeMapper.selectById(reader.getReaderTypeId()) : null;
            BigDecimal rate = rt != null && rt.getOverdueFineRate() != null ? rt.getOverdueFineRate() : BigDecimal.valueOf(0.50);
            fineAmount = rate.multiply(BigDecimal.valueOf(overdueDays));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("borrowRecordId", record.getId());
        result.put("barcode", barcode);
        result.put("bookInfoId", book != null ? book.getId() : null);
        result.put("title", book != null ? book.getTitle() : "");
        result.put("author", book != null ? book.getAuthor() : "");
        result.put("coverUrl", book != null ? book.getCoverUrl() : "");
        result.put("readerId", reader != null ? reader.getId() : null);
        result.put("readerNo", reader != null ? reader.getReaderNo() : "");
        result.put("readerName", user != null ? user.getRealName() : "");
        result.put("borrowDate", record.getBorrowDate() != null ? record.getBorrowDate().toLocalDate().toString() : "");
        result.put("dueDate", record.getDueDate().toString());
        result.put("isOverdue", isOverdue);
        result.put("overdueDays", overdueDays);
        result.put("fineAmount", fineAmount);
        return result;
    }

    /**
     * Execute return for borrowed books (batch).
     * Matches API.md 5.2: PUT /borrow/return {barcodes: [...], damageInfo: null}
     */
    @Transactional
    public Map<String, Object> returnBooks(List<String> barcodes, Long operatorId) {
        List<Map<String, Object>> records = new ArrayList<>();

        for (String barcode : barcodes) {
            Map<String, Object> item = new HashMap<>();
            item.put("barcode", barcode);
            try {
                BookCopy copy = bookCopyMapper.selectOne(
                        new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBarcode, barcode));
                if (copy == null) throw new BusinessException("Copy not found");

                BorrowRecord record = borrowRecordMapper.selectOne(
                        new LambdaQueryWrapper<BorrowRecord>()
                                .eq(BorrowRecord::getBookCopyId, copy.getId())
                                .eq(BorrowRecord::getStatus, "borrowed")
                                .orderByDesc(BorrowRecord::getCreateTime)
                                .last("LIMIT 1"));
                if (record == null) throw new BusinessException("No active borrow record for this copy");

                BookInfo book = bookInfoMapper.selectById(record.getBookInfoId());

                // Calculate overdue
                boolean isOverdue = record.getDueDate().isBefore(LocalDate.now());
                long overdueDays = 0;
                BigDecimal fineAmount = BigDecimal.ZERO;
                if (isOverdue) {
                    overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
                    Reader r = readerMapper.selectById(record.getReaderId());
                    ReaderType rt = r != null && r.getReaderTypeId() != null ?
                            readerTypeMapper.selectById(r.getReaderTypeId()) : null;
                    BigDecimal rate = rt != null && rt.getOverdueFineRate() != null ? rt.getOverdueFineRate() : BigDecimal.valueOf(0.50);
                    fineAmount = rate.multiply(BigDecimal.valueOf(overdueDays));
                }

                // Create fine if overdue
                if (isOverdue && fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                    FineRecord fine = new FineRecord();
                    fine.setReaderId(record.getReaderId());
                    fine.setBorrowRecordId(record.getId());
                    fine.setFineType("overdue");
                    fine.setAmount(fineAmount);
                    fine.setPaid(0);
                    fine.setWaive(0);
                    fine.setOperatorId(operatorId);
                    fineRecordMapper.insert(fine);
                }

                // Close borrow record
                LocalDateTime returnDate = LocalDateTime.now();
                record.setReturnDate(returnDate);
                record.setStatus("returned");
                borrowRecordMapper.updateById(record);

                // Update copy status
                copy.setStatus("in");
                bookCopyMapper.updateById(copy);

                // Decrement reader's current borrowed
                Reader reader = readerMapper.selectById(record.getReaderId());
                if (reader != null) {
                    reader.setCurrentBorrowed(Math.max(0, (reader.getCurrentBorrowed() != null ? reader.getCurrentBorrowed() : 0) - 1));
                    readerMapper.updateById(reader);
                }

                // Update book available copies
                if (book != null) {
                    book.setAvailableCopies(book.getAvailableCopies() != null ? book.getAvailableCopies() + 1 : 0);
                    bookInfoMapper.updateById(book);
                }

                item.put("title", book != null ? book.getTitle() : "");
                item.put("borrowDate", record.getBorrowDate() != null ?
                        record.getBorrowDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                item.put("dueDate", record.getDueDate().toString());
                item.put("returnDate", returnDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("overdueDays", (int) overdueDays);
                item.put("fineAmount", fineAmount.doubleValue());
            } catch (Exception e) {
                item.put("title", "");
                item.put("borrowDate", "");
                item.put("dueDate", "");
                item.put("returnDate", "");
                item.put("overdueDays", 0);
                item.put("fineAmount", 0.00);
                item.put("error", e.getMessage());
            }
            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        return result;
    }

    // ==================== Renew ====================

    /**
     * Renew a borrowed book.
     * Matches API.md 5.3: POST /borrow/{id}/renew
     * Rules:
     * - Due date ≤ 7 days away
     * - Not overdue
     * - Renew count not exceeded
     * - No other reader has reserved this book
     */
    @Transactional
    public Map<String, Object> renewBook(Long recordId, String readerNo) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(404, "Borrow record not found");

        // Verify the reader owns this record
        Reader reader = readerMapper.selectById(record.getReaderId());
        if (reader == null || !reader.getReaderNo().equals(readerNo)) {
            throw new BusinessException("This borrow record does not belong to this reader");
        }

        // Check not overdue
        if (record.getDueDate() != null && record.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Book is overdue — cannot renew, please return it first");
        }

        // Check due date within renewal window (configurable)
        int renewAdvanceDays = getConfigInt("borrow.renew_advance_days", 7);
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());
        if (daysUntilDue > renewAdvanceDays) {
            throw new BusinessException("Book can only be renewed within " + renewAdvanceDays + " days of due date (currently " + daysUntilDue + " days left)");
        }

        // Get reader type for renewal settings
        ReaderType rt = reader.getReaderTypeId() != null ?
                readerTypeMapper.selectById(reader.getReaderTypeId()) : null;
        int maxRenewCount = rt != null && rt.getRenewCount() != null ? rt.getRenewCount() : 1;
        int renewDays = rt != null && rt.getRenewDays() != null ? rt.getRenewDays() : 15;

        // Check renew count not exceeded
        if (record.getRenewCount() >= maxRenewCount) {
            throw new BusinessException("Renewal limit reached (max " + maxRenewCount + " time(s))");
        }

        // Check no other reader has reserved this book
        long reservationCount = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getBookInfoId, record.getBookInfoId())
                        .in(Reservation::getStatus, "waiting", "ready"));
        if (reservationCount > 0) {
            throw new BusinessException("Another reader has reserved this book — cannot renew");
        }

        // Execute renewal
        LocalDate oldDueDate = record.getDueDate();
        LocalDate newDueDate = oldDueDate.plusDays(renewDays);
        record.setDueDate(newDueDate);
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordMapper.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("oldDueDate", oldDueDate != null ? oldDueDate.toString() : "");
        result.put("newDueDate", newDueDate.toString());
        result.put("renewCount", record.getRenewCount());
        return result;
    }

    // ==================== Helpers ====================

    private int getConfigInt(String key, int defaultValue) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config == null || config.getConfigValue() == null) return defaultValue;
        try { return Integer.parseInt(config.getConfigValue()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private String getCardStatusLabel(Integer status) {
        if (status == null) return "Normal";
        return switch (status) {
            case 0 -> "Lost";
            case 1 -> "Normal";
            case 2 -> "Frozen";
            default -> "Unknown";
        };
    }

    private String getCopyStatusLabel(String status) {
        if (status == null) return "";
        return switch (status) {
            case "in" -> "available";
            case "borrowed" -> "borrowed";
            case "reserved" -> "reserved";
            case "maintenance" -> "in maintenance";
            case "lost" -> "lost";
            case "discarded" -> "discarded";
            default -> status;
        };
    }
}
