package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    public Map<String, Object> getBorrowStats(String startDate, String endDate, String type) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(6);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        List<Map<String, Object>> chartData = new ArrayList<>();

        long months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
        for (int i = 0; i <= months; i++) {
            LocalDate m = start.plusMonths(i).withDayOfMonth(1);
            LocalDate mEnd = m.withDayOfMonth(m.lengthOfMonth());
            String label = m.format(DateTimeFormatter.ofPattern("MM/dd"));

            LocalDateTime periodStart = m.atStartOfDay();
            LocalDateTime periodEnd = mEnd.atTime(LocalTime.MAX);

            long borrowCount = allRecords.stream()
                    .filter(r -> r.getCreateTime() != null && !r.getCreateTime().isBefore(periodStart) && !r.getCreateTime().isAfter(periodEnd))
                    .count();
            long returnCount = allRecords.stream()
                    .filter(r -> r.getReturnDate() != null && !r.getReturnDate().isBefore(periodStart) && !r.getReturnDate().isAfter(periodEnd))
                    .count();

            if (borrowCount > 0 || returnCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", label);
                item.put("borrowCount", borrowCount);
                item.put("returnCount", returnCount);
                chartData.add(item);
            }
        }

        long totalBorrow = allRecords.size();
        long totalReturn = allRecords.stream().filter(r -> r.getReturnDate() != null).count();
        long days = ChronoUnit.DAYS.between(start, end);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBorrow", totalBorrow);
        summary.put("totalReturn", totalReturn);
        summary.put("avgDailyBorrow", days > 0 ? Math.round(totalBorrow * 10.0 / days) / 10.0 : 0);
        summary.put("peakDay", chartData.stream().max(Comparator.comparing(m -> ((Number) m.get("borrowCount")).longValue()))
                .map(m -> m.get("date")).orElse("—"));

        Map<String, Object> result = new HashMap<>();
        result.put("chartData", chartData);
        result.put("summary", summary);
        return result;
    }

    public List<Map<String, Object>> getHotBooks(String type, int limit) {
        List<BookInfo> books = bookInfoMapper.selectList(
                new LambdaQueryWrapper<BookInfo>()
                        .eq(BookInfo::getStatus, 1)
                        .orderByDesc(BookInfo::getBorrowCount)
                        .last("LIMIT " + limit));

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (BookInfo book : books) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("id", book.getId());
            item.put("title", book.getTitle());
            item.put("author", book.getAuthor());
            item.put("borrowCount", book.getBorrowCount() != null ? book.getBorrowCount() : 0);
            if (book.getCategoryId() != null) {
                Category cat = categoryMapper.selectById(book.getCategoryId());
                item.put("categoryName", cat != null ? cat.getName() : null);
            }
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getCirculationStats() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long todayBorrow = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreateTime, todayStart)
                        .le(BorrowRecord::getCreateTime, todayEnd));
        long todayReturn = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getReturnDate, todayStart)
                        .le(BorrowRecord::getReturnDate, todayEnd));
        long totalBorrowed = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "borrowed"));
        long overdueCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, "borrowed")
                        .lt(BorrowRecord::getDueDate, LocalDate.now()));

        Map<String, Object> today = new HashMap<>();
        today.put("borrowCount", todayBorrow);
        today.put("returnCount", todayReturn);
        today.put("fineCount", 0);

        Map<String, Object> overall = new HashMap<>();
        overall.put("totalBorrow", totalBorrowed);
        overall.put("overdueRate", totalBorrowed > 0 ? Math.round(overdueCount * 100.0 / totalBorrowed * 10) / 10.0 : 0);
        overall.put("reservationFulfillRate", 0);

        Map<String, Object> result = new HashMap<>();
        result.put("today", today);
        result.put("overall", overall);
        return result;
    }

    public Map<String, Object> getCollectionStats() {
        long totalBooks = bookInfoMapper.selectCount(null);
        long totalCopies = bookCopyMapper.selectCount(null);

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

    public Map<String, Object> getReaderStats() {
        long totalReaders = readerMapper.selectCount(null);
        LocalDate monthAgo = LocalDate.now().minusDays(30);
        long activeReaders = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>()
                        .ge(BorrowRecord::getCreateTime, monthAgo.atStartOfDay()));

        List<Reader> allReaders = readerMapper.selectList(null);
        Map<Long, Long> typeCountMap = allReaders.stream()
                .filter(r -> r.getReaderTypeId() != null)
                .collect(Collectors.groupingBy(Reader::getReaderTypeId, Collectors.counting()));

        Map<String, Long> typeMap = new LinkedHashMap<>();
        typeMap.put("Student", typeCountMap.getOrDefault(1L, 0L));
        typeMap.put("Teacher", typeCountMap.getOrDefault(2L, 0L));
        typeMap.put("Staff", typeCountMap.getOrDefault(3L, 0L));
        typeMap.put("External", typeCountMap.getOrDefault(4L, 0L));

        List<Map<String, Object>> typeDistResult = new ArrayList<>();
        for (Map.Entry<String, Long> e : typeMap.entrySet()) {
            if (e.getValue() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", e.getKey());
                item.put("count", e.getValue());
                item.put("percentage", totalReaders > 0 ? Math.round(e.getValue() * 100.0 / totalReaders * 10) / 10.0 : 0);
                typeDistResult.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalReaders", totalReaders);
        result.put("activeReaders", activeReaders);
        result.put("activeRate", totalReaders > 0 ? Math.round(activeReaders * 100.0 / totalReaders * 10) / 10.0 : 0);
        result.put("typeDistribution", typeDistResult);
        return result;
    }

    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<SysOperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<SysOperationLog>()
                        .orderByDesc(SysOperationLog::getCreateTime)
                        .last("LIMIT " + limit));
        if (logs.isEmpty()) return getDefaultActivities();
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
        return List.of(
            Map.of("user", "admin", "action", "登录系统", "time", "just now", "color", "#888888"),
            Map.of("user", "Wang Xiaoming", "action", "借阅《The Great Gatsby》", "time", "10 min ago", "color", "#4A9FD8"),
            Map.of("user", "Li Hua", "action", "归还《1984》", "time", "1 hr ago", "color", "#34D399"),
            Map.of("user", "Zhang Wei", "action", "预约《Pride and Prejudice》", "time", "2 hr ago", "color", "#FBBF24"),
            Map.of("user", "Chen Mei", "action", "缴纳逾期罚款 ¥5.00", "time", "1 day ago", "color", "#F87171")
        );
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
