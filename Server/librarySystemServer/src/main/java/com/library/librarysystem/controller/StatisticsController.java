package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 流通统计 — ADMIN / LIBRARIAN
    @GetMapping("/circulation")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> circulation() {
        return Result.success(statisticsService.getCirculationStats());
    }

    // 馆藏统计 — ADMIN / CATALOGER / LIBRARIAN
    @GetMapping("/collection")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CATALOGER', 'ROLE_LIBRARIAN')")
    public Result<Map<String, Object>> collection() {
        return Result.success(statisticsService.getCollectionStats());
    }

    // 读者统计 — 仅 ADMIN
    @GetMapping("/readers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> readers() {
        return Result.success(statisticsService.getReaderStats());
    }

    // 借阅统计 — ADMIN / LIBRARIAN / CATALOGER
    @GetMapping("/borrow")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> borrowStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "month") String type) {
        return Result.success(statisticsService.getBorrowStats(startDate, endDate, type));
    }

    // 热门排行 — 所有已认证用户
    @GetMapping("/hot-books")
    public Result<List<Map<String, Object>>> hotBooks(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getHotBooks(type, limit));
    }

    // 近期活动 — ADMIN / LIBRARIAN
    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN')")
    public Result<List<Map<String, Object>>> recentActivity() {
        return Result.success(statisticsService.getRecentActivity(10));
    }
}
