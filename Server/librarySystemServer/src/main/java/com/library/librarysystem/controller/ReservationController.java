package com.library.librarysystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.common.Result;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationMapper reservationMapper;
    private final ReaderMapper readerMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookCopyMapper bookCopyMapper;
    private final LocationMapper locationMapper;
    private final SysConfigMapper configMapper;
    private final SysUserMapper userMapper;
    private final NotificationMapper notificationMapper;

    // ==================== 6.1 创建预约 ====================

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        Long bookInfoId = ((Number) req.get("bookInfoId")).longValue();
        Long pickLocationId = req.get("pickLocationId") != null ? ((Number) req.get("pickLocationId")).longValue() : null;
        String readerNo = (String) req.get("readerNo");

        BookInfo book = bookInfoMapper.selectById(bookInfoId);
        if (book == null) throw new BusinessException(404, "Book not found");
        if (book.getStatus() != 1) throw new BusinessException("Book is not active");

        Reader reader = readerMapper.selectOne(new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) throw new BusinessException(404, "Reader not found");
        if (reader.getCardStatus() != 1) throw new BusinessException("Reader card is not active");

        // Check: no duplicate reservation in progress
        long existing = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getReaderId, reader.getId())
                .eq(Reservation::getBookInfoId, bookInfoId)
                .in(Reservation::getStatus, "waiting", "ready"));
        if (existing > 0) throw new BusinessException("You already have a pending reservation for this book");

        // Calculate expire time from config (default 24h)
        int keepHours = 24;
        try { String val = getConfig("reservation.keep_hours"); if (val != null) keepHours = Integer.parseInt(val); } catch (Exception ignored) {}

        int availCopies = book.getAvailableCopies() != null ? book.getAvailableCopies() : 0;
        Reservation reservation = new Reservation();
        reservation.setReaderId(reader.getId());
        reservation.setBookInfoId(bookInfoId);
        reservation.setPickLocationId(pickLocationId);
        reservation.setReserveDate(LocalDateTime.now());
        reservation.setExpireDate(LocalDateTime.now().plusHours(keepHours));

        if (availCopies > 0) {
            // Has available copy → assign it directly, set ready status
            BookCopy copy = bookCopyMapper.selectOne(
                    new LambdaQueryWrapper<BookCopy>()
                            .eq(BookCopy::getBookId, bookInfoId)
                            .eq(BookCopy::getStatus, "in")
                            .last("LIMIT 1"));
            if (copy != null) {
                // Mark copy as reserved
                copy.setStatus("reserved");
                bookCopyMapper.updateById(copy);
                reservation.setBookCopyId(copy.getId());
                reservation.setStatus("ready");
                // Decrement available copies
                book.setAvailableCopies(availCopies - 1);
                bookInfoMapper.updateById(book);
            } else {
                reservation.setStatus("waiting");
            }
        } else {
            reservation.setStatus("waiting");
        }

        // Calculate queue position for waiting reservations
        long queuePosition = 0;
        if ("waiting".equals(reservation.getStatus())) {
            queuePosition = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getBookInfoId, bookInfoId)
                    .eq(Reservation::getStatus, "waiting")) + 1;
        }

        reservationMapper.insert(reservation);

        // Send notification
        if ("ready".equals(reservation.getStatus())) {
            notifyReader(reader.getId(), "预约到书", "您预约的《" + book.getTitle() + "》已为您预留，请在 " + keepHours + " 小时内到馆办理借阅。", "arrival", reservation.getId());
        } else {
            notifyReader(reader.getId(), "预约成功", "您已成功预约《" + book.getTitle() + "》，当前排队第 " + queuePosition + " 位。", "arrival", reservation.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", reservation.getId());
        result.put("bookTitle", book.getTitle());
        result.put("reserveDate", reservation.getReserveDate().toString());
        result.put("status", reservation.getStatus());
        result.put("queuePosition", queuePosition);
        return Result.success(result);
    }

    // ==================== 6.2 取消预约 ====================

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "Reservation not found");
        if (!"waiting".equals(reservation.getStatus())) {
            throw new BusinessException("Only waiting reservations can be cancelled");
        }
        reservation.setStatus("cancelled");
        reservationMapper.updateById(reservation);
        return Result.success();
    }

    // ==================== 6.3 当前预约列表 ====================

    @GetMapping("/current")
    public Result<List<Map<String, Object>>> current(@RequestParam String readerNo) {
        Reader reader = readerMapper.selectOne(new LambdaQueryWrapper<Reader>().eq(Reader::getReaderNo, readerNo));
        if (reader == null) return Result.success(List.of());

        return Result.success(reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getReaderId, reader.getId())
                .orderByDesc(Reservation::getReserveDate)
        ).stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("bookInfoId", r.getBookInfoId());
            var book = bookInfoMapper.selectById(r.getBookInfoId());
            if (book != null) {
                item.put("bookTitle", book.getTitle());
                item.put("bookAuthor", book.getAuthor());
                item.put("coverUrl", book.getCoverUrl());
            }
            item.put("reserveDate", r.getReserveDate() != null ? r.getReserveDate().toString() : "");
            item.put("expireDate", r.getExpireDate() != null ? r.getExpireDate().toString() : "");

            String statusLabel = switch (r.getStatus()) {
                case "waiting" -> "等待中";
                case "ready" -> "待取书";
                case "fulfilled" -> "已完成";
                case "cancelled" -> "已取消";
                case "expired" -> "已过期";
                default -> r.getStatus();
            };
            item.put("status", r.getStatus());
            item.put("statusLabel", statusLabel);

            if ("waiting".equals(r.getStatus())) {
                long pos = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getBookInfoId, r.getBookInfoId())
                        .eq(Reservation::getStatus, "waiting")
                        .lt(Reservation::getId, r.getId())) + 1;
                item.put("queuePosition", pos);
            } else {
                item.put("queuePosition", 0);
            }

            if (r.getPickLocationId() != null) {
                Location loc = locationMapper.selectById(r.getPickLocationId());
                item.put("pickLocationName", loc != null ? loc.getName() : null);
            }
            return item;
        }).collect(Collectors.toList()));
    }

    // ==================== 6.4 取书确认 ====================

    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    @Transactional
    public Result<Map<String, Object>> pickup(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long operatorId = req.get("operatorId") != null ? ((Number) req.get("operatorId")).longValue() : null;

        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) throw new BusinessException(404, "Reservation not found");
        if (!"ready".equals(reservation.getStatus())) {
            throw new BusinessException("Only ready reservations can be picked up");
        }

        BookCopy copy = bookCopyMapper.selectById(reservation.getBookCopyId());
        if (copy == null) throw new BusinessException("Assigned copy not found");

        BookInfo book = bookInfoMapper.selectById(reservation.getBookInfoId());
        Reader reader = readerMapper.selectById(reservation.getReaderId());
        if (reader == null) throw new BusinessException("Reader not found");
        if (reader.getCardStatus() != 1) throw new BusinessException("Reader card is not active");

        reservation.setStatus("fulfilled");
        reservation.setOperatorId(operatorId);
        reservationMapper.updateById(reservation);

        copy.setStatus("borrowed");
        bookCopyMapper.updateById(copy);

        reader.setCurrentBorrowed(reader.getCurrentBorrowed() != null ? reader.getCurrentBorrowed() + 1 : 1);
        reader.setTotalBorrowed(reader.getTotalBorrowed() != null ? reader.getTotalBorrowed() + 1 : 1);
        readerMapper.updateById(reader);

        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() != null ? book.getAvailableCopies() - 1 : 0);
            book.setBorrowCount(book.getBorrowCount() != null ? book.getBorrowCount() + 1 : 1);
            bookInfoMapper.updateById(book);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reservationId", reservation.getId());
        result.put("bookTitle", book != null ? book.getTitle() : "");
        result.put("readerNo", reader.getReaderNo());
        result.put("readerName", getReaderRealName(reader.getUserId()));
        return Result.success(result);
    }

    // ==================== Admin: 预约管理列表 ====================

    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<List<Map<String, Object>>> adminList(@RequestParam(required = false) String status) {
        var qw = new LambdaQueryWrapper<Reservation>().orderByDesc(Reservation::getReserveDate);
        if (status != null && !status.isEmpty()) {
            qw.eq(Reservation::getStatus, status);
        }

        return Result.success(reservationMapper.selectList(qw).stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("readerId", r.getReaderId());
            item.put("bookInfoId", r.getBookInfoId());
            item.put("status", r.getStatus());
            item.put("reserveDate", r.getReserveDate() != null ? r.getReserveDate().toString() : "");
            item.put("bookCopyId", r.getBookCopyId());

            Reader rd = readerMapper.selectById(r.getReaderId());
            if (rd != null) {
                item.put("readerNo", rd.getReaderNo());
                item.put("readerName", getReaderRealName(rd.getUserId()));
            }

            var book = bookInfoMapper.selectById(r.getBookInfoId());
            if (book != null) item.put("bookTitle", book.getTitle());

            if (r.getPickLocationId() != null) {
                Location loc = locationMapper.selectById(r.getPickLocationId());
                item.put("pickLocationName", loc != null ? loc.getName() : null);
            }
            return item;
        }).collect(Collectors.toList()));
    }

    private String getReaderRealName(Long userId) {
        if (userId == null) return "";
        SysUser u = userMapper.selectById(userId);
        return u != null ? u.getRealName() : "";
    }

    private String getConfig(String key) {
        SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return c != null ? c.getConfigValue() : null;
    }

    private void notifyReader(Long readerId, String title, String content, String type, Long relatedId) {
        com.library.librarysystem.entity.Notification n = new com.library.librarysystem.entity.Notification();
        n.setReaderId(readerId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRelatedId(relatedId);
        n.setReadFlag(0);
        notificationMapper.insert(n);
    }
}
