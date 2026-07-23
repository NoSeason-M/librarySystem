package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class SystemController {

    private final SystemService systemService;

    // ==================== Users ====================

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(systemService.listUsers(keyword, page, size));
    }

    @PostMapping("/users")
    public Result<Map<String, Object>> createUser(@RequestBody Map<String, Object> req) {
        Long id = systemService.createUser(req);
        return Result.success(Map.of("userId", id));
    }

    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateUser(id, req);
        return Result.success();
    }

    @PutMapping("/users/{id}/reset-pwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        systemService.resetPassword(id);
        return Result.success();
    }

    // ==================== Roles ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> listRoles() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listRoles());
    }

    @PostMapping("/roles")
    public Result<Map<String, Object>> createRole(@RequestBody Map<String, Object> req) {
        Long id = systemService.createRole(req);
        return Result.success(Map.of("roleId", id));
    }

    @PutMapping("/roles/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateRole(id, req);
        return Result.success();
    }

    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        systemService.deleteRole(id);
        return Result.success();
    }

    // ==================== Menus ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/menus")
    public Result<List<Map<String, Object>>> listMenus() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listMenus());
    }

    @PostMapping("/menus")
    public Result<Map<String, Object>> createMenu(@RequestBody Map<String, Object> req) {
        Long id = systemService.createMenu(req);
        return Result.success(Map.of("menuId", id));
    }

    @PutMapping("/menus/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateMenu(id, req);
        return Result.success();
    }

    @DeleteMapping("/menus/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        systemService.deleteMenu(id);
        return Result.success();
    }

    // ==================== Config ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/config")
    public Result<Map<String, Object>> listConfigs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(systemService.listConfigs(keyword, page, size));
    }

    @PutMapping("/config/{id}")
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateConfig(id, req);
        return Result.success();
    }

    @GetMapping("/config/public")
    public Result<List<Map<String, Object>>> publicConfig() {
        return Result.success(systemService.listPublicConfigs());
    }

    // ==================== Dicts ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/dicts")
    public Result<List<Map<String, Object>>> listDicts() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listDicts());
    }

    @GetMapping("/dicts/{code}")
    public Result<Map<String, Object>> dictWithItems(@PathVariable String code) {
        return Result.success(systemService.getDictWithItems(code));
    }

    @PostMapping("/dicts/items")
    public Result<Void> createDictItem(@RequestBody Map<String, Object> req) {
        systemService.createDictItem(req);
        return Result.success();
    }

    @PutMapping("/dicts/items/{id}")
    public Result<Void> updateDictItem(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateDictItem(id, req);
        return Result.success();
    }

    @DeleteMapping("/dicts/items/{id}")
    public Result<Void> deleteDictItem(@PathVariable Long id) {
        systemService.deleteDictItem(id);
        return Result.success();
    }

    // ==================== Logs ====================

    @GetMapping("/logs")
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        return Result.success(systemService.listLogs(keyword, module, startDate, endDate, page, size));
    }

    // ==================== Announcements ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/announcements")
    public Result<Map<String, Object>> listAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(systemService.listAnnouncements(page, size));
    }

    @PostMapping("/announcements")
    public Result<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> req) {
        Long id = systemService.createAnnouncement(req);
        return Result.success(Map.of("id", id));
    }

    @PutMapping("/announcements/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateAnnouncement(id, req);
        return Result.success();
    }

    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        systemService.deleteAnnouncement(id);
        return Result.success();
    }

    // ==================== Backup ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/backup")
    public Result<List<Map<String, Object>>> listBackups() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listBackups());
    }

    @PostMapping("/backup")
    public Result<Map<String, Object>> createBackup() {
        return Result.success(systemService.createBackup());
    }
}
