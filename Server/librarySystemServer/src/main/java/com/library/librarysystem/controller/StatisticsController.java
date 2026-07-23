package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/circulation")
    public Result<Map<String, Object>> circulation() {
        return Result.success(statisticsService.getCirculationStats());
    }

    @GetMapping("/collection")
    public Result<Map<String, Object>> collection() {
        return Result.success(statisticsService.getCollectionStats());
    }

    @GetMapping("/readers")
    public Result<Map<String, Object>> readers() {
        return Result.success(statisticsService.getReaderStats());
    }

    @GetMapping("/borrow")
    public Result<Map<String, Object>> borrowStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "month") String type) {
        return Result.success(statisticsService.getBorrowStats(startDate, endDate, type));
    }

    @GetMapping("/hot-books")
    public Result<List<Map<String, Object>>> hotBooks(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getHotBooks(type, limit));
    }

    @GetMapping("/recent-activity")
    public Result<List<Map<String, Object>>> recentActivity() {
        return Result.success(statisticsService.getRecentActivity(10));
    }
}
