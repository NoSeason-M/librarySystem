package com.library.librarysystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.Result;
import com.library.librarysystem.entity.Notification;
import com.library.librarysystem.entity.Reader;
import com.library.librarysystem.mapper.NotificationMapper;
import com.library.librarysystem.mapper.ReaderMapper;
import com.library.librarysystem.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final ReaderMapper readerMapper;

    /**
     * 11.1 我的通知列表
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer readFlag) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getUserId, user.getId()));
        if (reader == null) return Result.success(Map.of("records", List.of(), "total", 0, "unreadCount", 0));

        var qw = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReaderId, reader.getId())
                .orderByDesc(Notification::getCreateTime);
        if (readFlag != null) {
            qw.eq(Notification::getReadFlag, readFlag);
        }

        IPage<Notification> p = notificationMapper.selectPage(new Page<>(page, size), qw);
        long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getReaderId, reader.getId())
                        .eq(Notification::getReadFlag, 0));

        List<Map<String, Object>> records = p.getRecords().stream().map(this::toItem).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        result.put("unreadCount", unreadCount);
        return Result.success(result);
    }

    /**
     * 11.4 获取未读通知数
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@AuthenticationPrincipal UserDetailsImpl user) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getUserId, user.getId()));
        long count = 0;
        if (reader != null) {
            count = notificationMapper.selectCount(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getReaderId, reader.getId())
                            .eq(Notification::getReadFlag, 0));
        }
        return Result.success(Map.of("unreadCount", count));
    }

    /**
     * 11.2 标记已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Notification notif = notificationMapper.selectById(id);
        if (notif != null) {
            notif.setReadFlag(1);
            notif.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notif);
        }
        return Result.success();
    }

    /**
     * 11.2 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@AuthenticationPrincipal UserDetailsImpl user) {
        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getUserId, user.getId()));
        if (reader != null) {
            List<Notification> unread = notificationMapper.selectList(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getReaderId, reader.getId())
                            .eq(Notification::getReadFlag, 0));
            for (Notification n : unread) {
                n.setReadFlag(1);
                n.setReadTime(LocalDateTime.now());
                notificationMapper.updateById(n);
            }
        }
        return Result.success();
    }

    /**
     * 11.3 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 11.3 批量删除
     */
    @DeleteMapping
    public Result<Void> batchDelete(@RequestBody Map<String, Object> req) {
        List<Integer> ids = (List<Integer>) req.get("ids");
        if (ids != null) {
            for (Integer id : ids) {
                notificationMapper.deleteById(id.longValue());
            }
        }
        return Result.success();
    }

    private Map<String, Object> toItem(Notification n) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", n.getId());
        item.put("title", n.getTitle());
        item.put("content", n.getContent());
        item.put("type", n.getType());
        item.put("typeLabel", getTypeLabel(n.getType()));
        item.put("readFlag", n.getReadFlag() == 1);
        item.put("createTime", n.getCreateTime() != null ?
                n.getCreateTime().toString().replace("T", " ") : "");
        return item;
    }

    private String getTypeLabel(String type) {
        return switch (type) {
            case "overdue_due" -> "逾期提醒";
            case "due_soon" -> "即将到期";
            case "arrival" -> "预约到书";
            case "cancel" -> "预约取消";
            case "fine" -> "罚款通知";
            case "system" -> "系统公告";
            default -> type;
        };
    }
}
