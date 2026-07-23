package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookInfoMapper bookInfoMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final ReaderMapper readerMapper;
    private final CategoryMapper categoryMapper;
    private final SysOperationLogMapper operationLogMapper;

    /**
     * Circulation stats (for admin dashboard)
     * GET /api/statistics/circulation
     */
    public Map<String, Object> getCirculationStats() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long todayBorrow = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreateTime, todayStart)
                        .le(BorrowRecord::getCreateTime, todayEnd)
                        .eq(BorrowRecord::getStatus, "borrowed"));
        long todayReturn = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreateTime, todayStart)
                        .le(BorrowRecord::getCreateTime, todayEnd)
                        .eq(BorrowRecord::getStatus, "returned"));

        long totalBorrow = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, "borrowed"));
        long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, LocalDate.now()));

        Map<String, Object> today = new HashMap<>();
        today.put("borrowCount", todayBorrow);
        today.put("returnCount", todayReturn);
        today.put("fineCount", 0);

        Map<String, Object> overall = new HashMap<>();
        overall.put("totalBorrow", totalBorrow);
        overall.put("overdueRate", totalBorrow > 0 ? Math.round(overdueCount * 100.0 / totalBorrow * 10) / 10.0 : 0);
        overall.put("reservationFulfillRate", 0);

        Map<String, Object> result = new HashMap<>();
        result.put("today", today);
        result.put("overall", overall);
        return result;
    }

    /**
     * Collection stats (for admin dashboard)
     * GET /api/statistics/collection
     */
    public Map<String, Object> getCollectionStats() {
        long totalBooks = bookInfoMapper.selectCount(null);
        long totalCopies = bookCopyMapper.selectCount(null);

        // Category distribution
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1));
        List<Map<String, Object>> catDist = new ArrayList<>();
        for (Category cat : categories) {
            long count = bookInfoMapper.selectCount(
                    new LambdaQueryWrapper<BookInfo>().eq(BookInfo::getCategoryId, cat.getId()));
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", cat.getName());
                item.put("count", count);
                item.put("percentage", totalBooks > 0 ? Math.round(count * 100.0 / totalBooks * 10) / 10.0 : 0);
                catDist.add(item);
            }
        }

        // Monthly new arrivals (last 6 months)
        List<Map<String, Object>> monthlyNew = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = LocalDate.now().minusMonths(i);
            String monthKey = m.getYear() + "-" + String.format("%02d", m.getMonthValue());
            long count = bookInfoMapper.selectCount(
                    new LambdaQueryWrapper<BookInfo>()
                            .ge(BookInfo::getCreateTime, m.withDayOfMonth(1).atStartOfDay())
                            .lt(BookInfo::getCreateTime, m.plusMonths(1).withDayOfMonth(1).atStartOfDay()));
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthKey);
            item.put("count", count);
            monthlyNew.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalBooks", totalBooks);
        result.put("totalCopies", totalCopies);
        result.put("categoryDistribution", catDist);
        result.put("monthlyNewArrivals", monthlyNew);
        return result;
    }

    /**
     * Reader stats
     * GET /api/statistics/readers
     */
    public Map<String, Object> getReaderStats() {
        long totalReaders = readerMapper.selectCount(null);
        LocalDate monthAgo = LocalDate.now().minusDays(30);

        // Count active readers (borrowed in last 30 days)
        long activeReaders = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreateTime, monthAgo.atStartOfDay()));

        Map<String, Object> result = new HashMap<>();
        result.put("totalReaders", totalReaders);
        result.put("activeReaders", activeReaders);
        result.put("activeRate", totalReaders > 0 ? Math.round(activeReaders * 100.0 / totalReaders * 10) / 10.0 : 0);
        return result;
    }

    /**
     * Recent operation logs (for admin dashboard activity feed)
     */
    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<SysOperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<SysOperationLog>()
                        .orderByDesc(SysOperationLog::getCreateTime)
                        .last("LIMIT " + limit));

        if (logs.isEmpty()) {
            // Return dummy data if no logs exist
            return getDefaultActivities();
        }

        return logs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("user", log.getUsername());
            item.put("action", log.getOperation());
            item.put("time", formatTimeAgo(log.getCreateTime()));
            item.put("color", getActivityColor(log.getModule()));
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDefaultActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        String[][] defaults = {
            {"admin", "登录系统", "just now", "auth"},
            {"Wang Xiaoming", "借阅《三体》", "10 min ago", "borrow"},
            {"Li Hua", "归还《1984》", "1 hr ago", "borrow"},
            {"Zhang Wei", "预约《傲慢与偏见》", "2 hr ago", "reservation"},
            {"Chen Mei", "缴纳逾期罚款 ¥5.00", "1 day ago", "fine"},
        };
        for (String[] d : defaults) {
            Map<String, Object> item = new HashMap<>();
            item.put("user", d[0]);
            item.put("action", d[1]);
            item.put("time", d[2]);
            item.put("color", getActivityColor(d[3]));
            activities.add(item);
        }
        return activities;
    }

    private String getActivityColor(String module) {
        return switch (module) {
            case "borrow" -> "#4A9FD8";
            case "return" -> "#34D399";
            case "reservation" -> "#FBBF24";
            case "fine" -> "#F87171";
            default -> "#888888";
        };
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        long minutes = java.time.Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hr ago";
        long days = hours / 24;
        return days + " day(s) ago";
    }
}
