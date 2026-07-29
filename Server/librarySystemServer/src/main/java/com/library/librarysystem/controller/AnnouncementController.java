package com.library.librarysystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Announcement;
import com.library.librarysystem.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    /**
     * 11.5 获取已发布的公开公告列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "5") int size) {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getTopFlag)
                        .orderByDesc(Announcement::getPublishTime)
                        .last("LIMIT " + size));
        return Result.success(list.stream().map(a -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("content", a.getContent());
            item.put("type", a.getType());
            item.put("targetRoles", a.getTargetRoles());
            item.put("topFlag", a.getTopFlag());
            item.put("publishTime", a.getPublishTime() != null ? a.getPublishTime().toString() : "");
            return item;
        }).collect(Collectors.toList()));
    }
}
