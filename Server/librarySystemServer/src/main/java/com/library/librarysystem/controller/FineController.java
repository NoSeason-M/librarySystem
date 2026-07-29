package com.library.librarysystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.Result;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.entity.FineRecord;
import com.library.librarysystem.entity.Reader;
import com.library.librarysystem.entity.SysUser;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineRecordMapper fineRecordMapper;
    private final ReaderMapper readerMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final SysUserMapper userMapper;

    /**
     * Admin: list fines with pagination and filters.
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String readerNo,
            @RequestParam(required = false) String fineType,
            @RequestParam(required = false) Integer paid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        LambdaQueryWrapper<FineRecord> qw = new LambdaQueryWrapper<FineRecord>()
                .orderByDesc(FineRecord::getCreateTime);

        if (fineType != null && !fineType.isEmpty()) {
            qw.eq(FineRecord::getFineType, fineType);
        }
        if (paid != null) {
            qw.eq(FineRecord::getPaid, paid);
        }

        // If readerNo provided (reader self-service), filter by that reader
        if (readerNo != null && !readerNo.isEmpty()) {
            Reader reader = readerMapper.selectOne(
                    new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
            if (reader == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", List.of()); empty.put("total", 0);
                empty.put("size", size); empty.put("current", page); empty.put("pages", 0);
                return Result.success(empty);
            }
            qw.eq(FineRecord::getReaderId, reader.getId());
        }

        // Admin keyword search across reader no and name
        if (keyword != null && !keyword.isEmpty() && readerNo == null) {
            List<Reader> matchedReaders = readerMapper.selectList(
                    new LambdaQueryWrapper<Reader>()
                            .like(Reader::getReaderNo, keyword)
                            .or().apply("user_id IN (SELECT id FROM sys_user WHERE real_name LIKE {0})", "%" + keyword + "%"));
            if (!matchedReaders.isEmpty()) {
                List<Long> readerIds = matchedReaders.stream().map(Reader::getId).collect(Collectors.toList());
                qw.in(FineRecord::getReaderId, readerIds);
            } else {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", List.of()); empty.put("total", 0);
                empty.put("size", size); empty.put("current", page); empty.put("pages", 0);
                return Result.success(empty);
            }
        }

        IPage<FineRecord> p = fineRecordMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> records = p.getRecords().stream().map(this::toFineItem).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return Result.success(result);
    }

    /**
     * Pay a single fine.
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @Transactional
    public Result<Void> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> req) {
        FineRecord fine = fineRecordMapper.selectById(id);
        if (fine == null) throw new BusinessException(404, "Fine not found");
        if (fine.getPaid() == 1) throw new BusinessException("Fine already paid");

        fine.setPaid(1);
        fine.setPaidDate(LocalDateTime.now());
        if (req != null && req.containsKey("remark")) {
            fine.setRemark(req.get("remark"));
        }
        fineRecordMapper.updateById(fine);
        return Result.success();
    }

    /**
     * Waive a single fine.
     */
    @PutMapping("/{id}/waive")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @Transactional
    public Result<Void> waive(@PathVariable Long id, @RequestBody Map<String, String> req) {
        FineRecord fine = fineRecordMapper.selectById(id);
        if (fine == null) throw new BusinessException(404, "Fine not found");
        if (fine.getPaid() == 1) throw new BusinessException("Fine already paid");
        if (fine.getWaive() == 1) throw new BusinessException("Fine already waived");

        fine.setWaive(1);
        fine.setWaiveReason(req.getOrDefault("waiveReason", "管理员豁免"));
        fineRecordMapper.updateById(fine);
        return Result.success();
    }

    /**
     * Batch pay fines.
     */
    @PostMapping("/batch-pay")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @Transactional
    public Result<Void> batchPay(@RequestBody Map<String, Object> req) {
        List<Integer> ids = (List<Integer>) req.get("ids");
        if (ids == null || ids.isEmpty()) throw new BusinessException("ids is required");
        for (Integer id : ids) {
            FineRecord fine = fineRecordMapper.selectById(id.longValue());
            if (fine != null && fine.getPaid() == 0) {
                fine.setPaid(1);
                fine.setPaidDate(LocalDateTime.now());
                fineRecordMapper.updateById(fine);
            }
        }
        return Result.success();
    }

    /**
     * Batch waive fines.
     */
    @PutMapping("/batch-waive")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @Transactional
    public Result<Void> batchWaive(@RequestBody Map<String, Object> req) {
        List<Integer> ids = (List<Integer>) req.get("ids");
        String reason = (String) req.getOrDefault("waiveReason", "管理员批量豁免");
        if (ids == null || ids.isEmpty()) throw new BusinessException("ids is required");
        for (Integer id : ids) {
            FineRecord fine = fineRecordMapper.selectById(id.longValue());
            if (fine != null && fine.getPaid() == 0 && fine.getWaive() == 0) {
                fine.setWaive(1);
                fine.setWaiveReason(reason);
                fineRecordMapper.updateById(fine);
            }
        }
        return Result.success();
    }

    /**
     * Get stats summary for fines management.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> summary() {
        List<FineRecord> all = fineRecordMapper.selectList(null);

        long unpaidCount = all.stream().filter(f -> f.getPaid() == 0 && f.getWaive() == 0).count();
        double unpaidAmount = all.stream().filter(f -> f.getPaid() == 0 && f.getWaive() == 0)
                .mapToDouble(f -> f.getAmount() != null ? f.getAmount().doubleValue() : 0).sum();
        long thisMonthCount = all.stream().filter(f -> f.getCreateTime() != null
                && f.getCreateTime().getMonthValue() == LocalDateTime.now().getMonthValue()
                && f.getCreateTime().getYear() == LocalDateTime.now().getYear()).count();
        double paidThisMonth = all.stream().filter(f -> f.getPaid() == 1 && f.getPaidDate() != null
                && f.getPaidDate().getMonthValue() == LocalDateTime.now().getMonthValue()
                && f.getPaidDate().getYear() == LocalDateTime.now().getYear())
                .mapToDouble(f -> f.getAmount() != null ? f.getAmount().doubleValue() : 0).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("unpaidCount", unpaidCount);
        result.put("unpaidAmount", unpaidAmount);
        result.put("thisMonthCount", thisMonthCount);
        result.put("paidThisMonth", paidThisMonth);
        return Result.success(result);
    }

    private Map<String, Object> toFineItem(FineRecord f) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", f.getId());
        item.put("fineType", switch (f.getFineType()) {
            case "overdue" -> "逾期罚款";
            case "damage" -> "损坏赔偿";
            case "lost" -> "丢失赔偿";
            default -> f.getFineType();
        });
        item.put("fineTypeCode", f.getFineType());
        item.put("amount", f.getAmount() != null ? f.getAmount().doubleValue() : 0);
        item.put("paid", f.getPaid() == 1);
        item.put("waive", f.getWaive() == 1);
        item.put("waiveReason", f.getWaiveReason());
        item.put("createTime", f.getCreateTime() != null ? f.getCreateTime().toLocalDate().toString() : "");
        item.put("paidDate", f.getPaidDate() != null ? f.getPaidDate().toLocalDate().toString() : "");

        // Reader info
        Reader reader = readerMapper.selectById(f.getReaderId());
        if (reader != null) {
            item.put("readerNo", reader.getReaderNo());
            SysUser user = userMapper.selectById(reader.getUserId());
            item.put("readerName", user != null ? user.getRealName() : "");
        }

        // Book info from borrow record
        if (f.getBorrowRecordId() != null) {
            var record = borrowRecordMapper.selectById(f.getBorrowRecordId());
            if (record != null) {
                item.put("bookInfoId", record.getBookInfoId());
                var book = bookInfoMapper.selectById(record.getBookInfoId());
                if (book != null) {
                    item.put("bookTitle", book.getTitle());
                }
                // Overdue days
                if (record.getDueDate() != null && record.getReturnDate() != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate().toLocalDate());
                    item.put("overdueDays", days > 0 ? days : 0);
                } else {
                    item.put("overdueDays", 0);
                }
            }
        }
        return item;
    }
}
