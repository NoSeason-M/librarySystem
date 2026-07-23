package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysConfigMapper configMapper;
    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final AnnouncementMapper announcementMapper;
    private final SysBackupMapper backupMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== Users ====================

    public Map<String, Object> listUsers(String keyword, int page, int size) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword);
        }
        qw.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> p = userMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(u -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("realName", u.getRealName());
            item.put("email", u.getEmail());
            item.put("phone", u.getPhone());
            item.put("status", u.getStatus());
            item.put("createTime", u.getCreateTime() != null ? u.getCreateTime().toString() : null);
            List<SysUserRole> urs = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, u.getId()));
            List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<String> roleNames = roleIds.stream().map(id -> {
                SysRole r = roleMapper.selectById(id);
                return r != null ? r.getName() : null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            item.put("roleIds", roleIds);
            item.put("roleNames", roleNames);
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    @Transactional
    public Long createUser(Map<String, Object> req) {
        SysUser user = new SysUser();
        user.setUsername((String) req.get("username"));
        user.setPassword(passwordEncoder.encode((String) req.getOrDefault("password", "123456")));
        user.setRealName((String) req.get("realName"));
        user.setEmail((String) req.get("email"));
        user.setPhone((String) req.get("phone"));
        user.setStatus(1);
        userMapper.insert(user);

        List<Integer> roleIds = (List<Integer>) req.get("roleIds");
        if (roleIds != null) {
            for (Integer rid : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(rid.longValue());
                userRoleMapper.insert(ur);
            }
        }
        return user.getId();
    }

    public void updateUser(Long id, Map<String, Object> req) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("User not found");
        if (req.containsKey("realName")) user.setRealName((String) req.get("realName"));
        if (req.containsKey("email")) user.setEmail((String) req.get("email"));
        if (req.containsKey("phone")) user.setPhone((String) req.get("phone"));
        if (req.containsKey("status")) user.setStatus((Integer) req.get("status"));
        userMapper.updateById(user);

        if (req.containsKey("roleIds")) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
            List<Integer> roleIds = (List<Integer>) req.get("roleIds");
            if (roleIds != null) {
                for (Integer rid : roleIds) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(id);
                    ur.setRoleId(rid.longValue());
                    userRoleMapper.insert(ur);
                }
            }
        }
    }

    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("User not found");
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    // ==================== Roles ====================

    public List<Map<String, Object>> listRoles() {
        return roleMapper.selectList(null).stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("name", r.getName());
            item.put("code", r.getCode());
            item.put("description", r.getDescription());
            item.put("sort", r.getSort());
            item.put("status", r.getStatus());
            // Get assigned menu IDs
            List<SysRoleMenu> rms = roleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, r.getId()));
            item.put("menuIds", rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Long createRole(Map<String, Object> req) {
        SysRole role = new SysRole();
        role.setName((String) req.get("name"));
        role.setCode((String) req.get("code"));
        role.setDescription((String) req.getOrDefault("description", ""));
        role.setSort(req.get("sort") != null ? ((Number) req.get("sort")).intValue() : 0);
        role.setStatus(1);
        roleMapper.insert(role);
        saveRoleMenus(role.getId(), req);
        return role.getId();
    }

    public void updateRole(Long id, Map<String, Object> req) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException("Role not found");
        if (req.containsKey("name")) role.setName((String) req.get("name"));
        if (req.containsKey("code")) role.setCode((String) req.get("code"));
        if (req.containsKey("description")) role.setDescription((String) req.get("description"));
        roleMapper.updateById(role);
        saveRoleMenus(id, req);
    }

    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    private void saveRoleMenus(Long roleId, Map<String, Object> req) {
        if (req.containsKey("menuIds")) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
            List<Integer> menuIds = (List<Integer>) req.get("menuIds");
            if (menuIds != null) {
                for (Integer mid : menuIds) {
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setRoleId(roleId);
                    rm.setMenuId(mid.longValue());
                    roleMenuMapper.insert(rm);
                }
            }
        }
    }

    // ==================== Menus ====================

    public List<Map<String, Object>> listMenus() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return buildMenuTree(all, 0L);
    }

    private List<Map<String, Object>> buildMenuTree(List<SysMenu> all, Long parentId) {
        return all.stream().filter(m -> m.getParentId() != null && m.getParentId().equals(parentId)).map(m -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName());
            item.put("permission", m.getPermission());
            item.put("path", m.getPath());
            item.put("icon", m.getIcon());
            item.put("type", m.getType());
            item.put("sort", m.getSort());
            item.put("parentId", m.getParentId());
            item.put("visible", m.getVisible());
            item.put("component", m.getComponent());
            item.put("children", buildMenuTree(all, m.getId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Long createMenu(Map<String, Object> req) {
        SysMenu menu = new SysMenu();
        applyMenuFields(menu, req);
        menuMapper.insert(menu);
        return menu.getId();
    }

    public void updateMenu(Long id, Map<String, Object> req) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) throw new BusinessException("Menu not found");
        applyMenuFields(menu, req);
        menuMapper.updateById(menu);
    }

    public void deleteMenu(Long id) {
        menuMapper.deleteById(id);
    }

    private void applyMenuFields(SysMenu menu, Map<String, Object> req) {
        if (req.containsKey("name")) menu.setName((String) req.get("name"));
        if (req.containsKey("permission")) menu.setPermission((String) req.get("permission"));
        if (req.containsKey("path")) menu.setPath((String) req.get("path"));
        if (req.containsKey("component")) menu.setComponent((String) req.get("component"));
        if (req.containsKey("icon")) menu.setIcon((String) req.get("icon"));
        if (req.containsKey("type")) menu.setType(((Number) req.get("type")).intValue());
        if (req.containsKey("sort")) menu.setSort(((Number) req.get("sort")).intValue());
        if (req.containsKey("parentId")) menu.setParentId(req.get("parentId") != null ? ((Number) req.get("parentId")).longValue() : 0L);
        if (req.containsKey("visible")) menu.setVisible(((Number) req.get("visible")).intValue());
    }

    // ==================== Config ====================

    public Map<String, Object> listConfigs(String keyword, int page, int size) {
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(SysConfig::getConfigKey, keyword);
        }
        qw.orderByAsc(SysConfig::getConfigKey);
        IPage<SysConfig> p = configMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("configKey", c.getConfigKey());
            item.put("configValue", c.getConfigValue());
            item.put("remark", c.getRemark());
            return item;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    public void updateConfig(Long id, Map<String, Object> req) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) throw new BusinessException("Config not found");
        if (req.containsKey("configValue")) config.setConfigValue((String) req.get("configValue"));
        configMapper.updateById(config);
    }

    public List<Map<String, Object>> listPublicConfigs() {
        return configMapper.selectList(null).stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("configKey", c.getConfigKey());
            item.put("configValue", c.getConfigValue());
            return item;
        }).collect(Collectors.toList());
    }

    // ==================== Dictionary ====================

    public List<Map<String, Object>> listDicts() {
        return dictMapper.selectList(null).stream().map(d -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("dictCode", d.getDictCode());
            item.put("dictName", d.getDictName());
            item.put("description", d.getDescription());
            item.put("status", d.getStatus());
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getDictWithItems(String code) {
        SysDict dict = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, code));
        Map<String, Object> result = new HashMap<>();
        if (dict != null) {
            result.put("id", dict.getId());
            result.put("dictCode", dict.getDictCode());
            result.put("dictName", dict.getDictName());
        }
        List<Map<String, Object>> items = dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictCode, code)
                        .orderByAsc(SysDictItem::getSort)).stream().map(i -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i.getId());
            item.put("itemLabel", i.getItemLabel());
            item.put("itemValue", i.getItemValue());
            item.put("sort", i.getSort());
            item.put("defaultFlag", i.getDefaultFlag());
            return item;
        }).collect(Collectors.toList());
        result.put("items", items);
        return result;
    }

    public void createDictItem(Map<String, Object> req) {
        SysDictItem item = new SysDictItem();
        item.setDictCode((String) req.get("dictCode"));
        item.setItemLabel((String) req.get("itemLabel"));
        item.setItemValue((String) req.get("itemValue"));
        item.setSort(req.get("sort") != null ? ((Number) req.get("sort")).intValue() : 0);
        item.setStatus(1);
        dictItemMapper.insert(item);
    }

    public void updateDictItem(Long id, Map<String, Object> req) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) throw new BusinessException("Dict item not found");
        if (req.containsKey("itemLabel")) item.setItemLabel((String) req.get("itemLabel"));
        if (req.containsKey("itemValue")) item.setItemValue((String) req.get("itemValue"));
        if (req.containsKey("sort")) item.setSort(((Number) req.get("sort")).intValue());
        dictItemMapper.updateById(item);
    }

    public void deleteDictItem(Long id) { dictItemMapper.deleteById(id); }

    // ==================== Operation Logs ====================

    public Map<String, Object> listLogs(String keyword, String module, String startDate, String endDate, int page, int size) {
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(SysOperationLog::getUsername, keyword);
        }
        if (module != null && !module.isEmpty()) {
            qw.eq(SysOperationLog::getModule, module);
        }
        qw.orderByDesc(SysOperationLog::getCreateTime);
        IPage<SysOperationLog> p = operationLogMapper.selectPage(new Page<>(page, size), qw);
        List<Map<String, Object>> records = p.getRecords().stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("username", log.getUsername());
            item.put("operation", log.getOperation());
            item.put("module", log.getModule());
            item.put("requestIp", log.getRequestIp());
            item.put("requestMethod", log.getRequestMethod());
            item.put("requestUrl", log.getRequestUrl());
            item.put("result", log.getResult());
            item.put("duration", log.getDuration());
            item.put("createTime", log.getCreateTime() != null ? log.getCreateTime().toString() : null);
            return item;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    // ==================== Announcements ====================

    public Map<String, Object> listAnnouncements(int page, int size) {
        IPage<Announcement> p = announcementMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
        List<Map<String, Object>> records = p.getRecords().stream().map(a -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("content", a.getContent());
            item.put("type", a.getType());
            item.put("targetRoles", a.getTargetRoles());
            item.put("topFlag", a.getTopFlag());
            item.put("status", a.getStatus());
            item.put("publishTime", a.getPublishTime() != null ? a.getPublishTime().toString() : null);
            item.put("createTime", a.getCreateTime() != null ? a.getCreateTime().toString() : null);
            return item;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", p.getTotal());
        result.put("size", p.getSize());
        result.put("current", p.getCurrent());
        result.put("pages", p.getPages());
        return result;
    }

    @Transactional
    public Long createAnnouncement(Map<String, Object> req) {
        Announcement a = new Announcement();
        a.setTitle((String) req.get("title"));
        a.setContent((String) req.get("content"));
        a.setType((String) req.getOrDefault("type", "general"));
        a.setTargetRoles((String) req.getOrDefault("targetRoles", "all"));
        a.setTopFlag(req.getOrDefault("topFlag", 0) instanceof Integer ? (Integer) req.get("topFlag") : 0);
        a.setStatus(0);
        if (req.containsKey("publishTime") && req.get("publishTime") != null) {
            a.setPublishTime(LocalDateTime.parse((String) req.get("publishTime")));
        }
        announcementMapper.insert(a);
        return a.getId();
    }

    public void updateAnnouncement(Long id, Map<String, Object> req) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("Announcement not found");
        if (req.containsKey("title")) a.setTitle((String) req.get("title"));
        if (req.containsKey("content")) a.setContent((String) req.get("content"));
        if (req.containsKey("type")) a.setType((String) req.get("type"));
        if (req.containsKey("targetRoles")) a.setTargetRoles((String) req.get("targetRoles"));
        if (req.containsKey("topFlag")) a.setTopFlag((Integer) req.get("topFlag"));
        if (req.containsKey("status")) a.setStatus((Integer) req.get("status"));
        announcementMapper.updateById(a);
    }

    public void deleteAnnouncement(Long id) { announcementMapper.deleteById(id); }

    // ==================== Backup ====================

    public List<Map<String, Object>> listBackups() {
        return backupMapper.selectList(new LambdaQueryWrapper<SysBackup>()
                .orderByDesc(SysBackup::getCreateTime)).stream().map(b -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("fileName", b.getFileName());
            item.put("fileSize", b.getFileSize());
            item.put("fileSizeDisplay", b.getFileSize() != null ?
                    String.format("%.2f MB", b.getFileSize() / 1048576.0) : "0 MB");
            item.put("backupType", b.getBackupType());
            item.put("backupStatus", b.getBackupStatus());
            item.put("createTime", b.getCreateTime() != null ? b.getCreateTime().toString() : null);
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createBackup() {
        SysBackup backup = new SysBackup();
        String fileName = "library_backup_" +
                LocalDateTime.now().toString().replace(":", "").replace("-", "").substring(0, 15) + ".sql";
        backup.setFileName(fileName);
        backup.setFilePath("/backups/" + fileName);
        backup.setFileSize(0L);
        backup.setBackupType("manual");
        backup.setBackupStatus(0);
        backupMapper.insert(backup);

        backup.setBackupStatus(1);
        backup.setFileSize(5242880L);
        backupMapper.updateById(backup);

        Map<String, Object> result = new HashMap<>();
        result.put("id", backup.getId());
        result.put("fileName", fileName);
        result.put("fileSize", 5242880L);
        result.put("fileSizeDisplay", "5.00 MB");
        result.put("status", 1);
        result.put("createTime", backup.getCreateTime() != null ? backup.getCreateTime().toString() : null);
        return result;
    }
}
