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
public class SystemController {

    private final SystemService systemService;

    // ==================== Users (仅 ADMIN) ====================

    @GetMapping("/users") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> listUsers(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(systemService.listUsers(keyword, page, size));
    }
    @PostMapping("/users") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> createUser(@RequestBody Map<String, Object> req) {
        return Result.success(Map.of("userId", systemService.createUser(req)));
    }
    @PutMapping("/users/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateUser(id, req); return Result.success();
    }
    @PutMapping("/users/{id}/reset-pwd") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        systemService.resetPassword(id); return Result.success();
    }

    // ==================== Roles (仅 ADMIN) ====================

    @GetMapping("/roles") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<Map<String, Object>>> listRoles() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listRoles());
    }
    @PostMapping("/roles") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> createRole(@RequestBody Map<String, Object> req) {
        return Result.success(Map.of("roleId", systemService.createRole(req)));
    }
    @PutMapping("/roles/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateRole(id, req); return Result.success();
    }
    @DeleteMapping("/roles/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        systemService.deleteRole(id); return Result.success();
    }

    // ==================== Menus (仅 ADMIN) ====================

    @GetMapping("/menus") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<Map<String, Object>>> listMenus() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listMenus());
    }
    @PostMapping("/menus") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> createMenu(@RequestBody Map<String, Object> req) {
        return Result.success(Map.of("menuId", systemService.createMenu(req)));
    }
    @PutMapping("/menus/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateMenu(id, req); return Result.success();
    }
    @DeleteMapping("/menus/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        systemService.deleteMenu(id); return Result.success();
    }

    // ==================== Config (仅 ADMIN) ====================

    @GetMapping("/config") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> listConfigs(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(systemService.listConfigs(keyword, page, size));
    }
    @PutMapping("/config/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateConfig(id, req); return Result.success();
    }
    @GetMapping("/config/public")
    public Result<List<Map<String, Object>>> publicConfig() {
        return Result.success(systemService.listPublicConfigs());
    }

    // ==================== Dicts (仅 ADMIN) ====================

    @GetMapping("/dicts") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<Map<String, Object>>> listDicts() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listDicts());
    }
    @GetMapping("/dicts/{code}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> dictWithItems(@PathVariable String code) {
        return Result.success(systemService.getDictWithItems(code));
    }
    @PostMapping("/dicts/items") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> createDictItem(@RequestBody Map<String, Object> req) {
        systemService.createDictItem(req); return Result.success();
    }
    @PutMapping("/dicts/items/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateDictItem(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateDictItem(id, req); return Result.success();
    }
    @DeleteMapping("/dicts/items/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> deleteDictItem(@PathVariable Long id) {
        systemService.deleteDictItem(id); return Result.success();
    }

    // ==================== Logs (仅 ADMIN) ====================

    @GetMapping("/logs") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> listLogs(@RequestParam(required = false) String keyword, @RequestParam(required = false) String module, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int size) {
        return Result.success(systemService.listLogs(keyword, module, startDate, endDate, page, size));
    }

    // ==================== Announcements (ADMIN / LIBRARIAN / CATALOGER) ====================

    @GetMapping("/announcements") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> listAnnouncements(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(systemService.listAnnouncements(page, size));
    }
    @PostMapping("/announcements") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> req) {
        return Result.success(Map.of("id", systemService.createAnnouncement(req)));
    }
    @PutMapping("/announcements/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        systemService.updateAnnouncement(id, req); return Result.success();
    }
    @DeleteMapping("/announcements/{id}") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        systemService.deleteAnnouncement(id); return Result.success();
    }
    @PutMapping("/announcements/{id}/publish") @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_LIBRARIAN', 'ROLE_CATALOGER')")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        systemService.publishAnnouncement(id); return Result.success();
    }

    // ==================== Backup (仅 ADMIN) ====================

    @GetMapping("/backup") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<Map<String, Object>>> listBackups() {
        return Result.success((List<Map<String, Object>>) (List<?>) systemService.listBackups());
    }
    @PostMapping("/backup") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> createBackup() {
        return Result.success(systemService.createBackup());
    }
}
