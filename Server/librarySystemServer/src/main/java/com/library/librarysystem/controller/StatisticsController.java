package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/recent-activity")
    public Result<List<Map<String, Object>>> recentActivity() {
        return Result.success(statisticsService.getRecentActivity(10));
    }
}
