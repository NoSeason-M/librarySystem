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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderMapper readerMapper;
    private final ReaderTypeMapper readerTypeMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> listReaders(String keyword, Long readerTypeId, Integer cardStatus, int page, int size) {
        // Start with all readers
        List<Reader> readers = readerMapper.selectList(null);

        // Filter by keyword (search in reader_no and related user real_name)
        if (keyword != null && !keyword.isEmpty()) {
            readers = readers.stream().filter(r -> {
                SysUser user = userMapper.selectById(r.getUserId());
                if (user == null) return false;
                String name = user.getRealName() != null ? user.getRealName() : "";
                String phone = user.getPhone() != null ? user.getPhone() : "";
                return r.getReaderNo().contains(keyword)
                        || name.contains(keyword)
                        || phone.contains(keyword);
            }).collect(Collectors.toList());
        }

        // Filter by type
        if (readerTypeId != null) {
            readers = readers.stream()
                    .filter(r -> r.getReaderTypeId() != null && r.getReaderTypeId().equals(readerTypeId))
                    .collect(Collectors.toList());
        }

        // Filter by card status
        if (cardStatus != null) {
            readers = readers.stream()
                    .filter(r -> r.getCardStatus() != null && r.getCardStatus().equals(cardStatus))
                    .collect(Collectors.toList());
        }

        // Manual pagination
        int total = readers.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<Reader> pageList = fromIndex >= total ? List.of() : readers.subList(fromIndex, toIndex);

        List<Map<String, Object>> records = pageList.stream().map(this::toReaderItem).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", (int) Math.ceil((double) total / size));
        return result;
    }

    @Transactional
    public Map<String, Object> createReader(Map<String, Object> req) {
        String realName = (String) req.get("realName");
        Long readerTypeId = req.get("readerTypeId") != null ? ((Number) req.get("readerTypeId")).longValue() : 1L;
        String email = (String) req.get("email");
        String phone = (String) req.get("phone");

        // Generate username from name + random digits
        String baseName = realName != null ? realName.toLowerCase().replaceAll("[^a-z0-9]", "") : "reader";
        String username = baseName + (int)(Math.random() * 10000);

        // Create sys_user
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456")); // default password
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        userMapper.insert(user);

        // Assign ROLE_READER
        SysRole readerRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "ROLE_READER"));
        if (readerRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(readerRole.getId());
            userRoleMapper.insert(ur);
        }

        // Create reader record
        Reader reader = new Reader();
        reader.setUserId(user.getId());
        reader.setReaderNo(generateReaderNo());
        reader.setReaderTypeId(readerTypeId);
        reader.setCardStatus(1); // normal
        reader.setRegisterDate(LocalDate.now());
        reader.setExpireDate(LocalDate.now().plusYears(4));
        reader.setTotalBorrowed(0);
        reader.setCurrentBorrowed(0);
        readerMapper.insert(reader);

        Map<String, Object> result = new HashMap<>(toReaderItem(reader));
        result.put("username", username);
        result.put("initialPassword", "123456");
        return result;
    }

    public void updateReader(Long id, Map<String, Object> req) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new BusinessException("Reader not found");

        if (req.containsKey("readerTypeId")) {
            reader.setReaderTypeId(req.get("readerTypeId") != null ? ((Number) req.get("readerTypeId")).longValue() : null);
        }
        if (req.containsKey("remark")) reader.setRemark((String) req.get("remark"));

        // Update user info too
        if (req.containsKey("email") || req.containsKey("phone") || req.containsKey("realName")) {
            SysUser user = userMapper.selectById(reader.getUserId());
            if (user != null) {
                if (req.containsKey("email")) user.setEmail((String) req.get("email"));
                if (req.containsKey("phone")) user.setPhone((String) req.get("phone"));
                if (req.containsKey("realName")) user.setRealName((String) req.get("realName"));
                userMapper.updateById(user);
            }
        }

        readerMapper.updateById(reader);
    }

    public void updateCardStatus(Long id, String action) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new BusinessException("Reader not found");

        switch (action) {
            case "lost" -> reader.setCardStatus(0);     // 0=lost
            case "restore" -> reader.setCardStatus(1);  // 1=normal
            case "freeze" -> reader.setCardStatus(2);   // 2=frozen
            case "unfreeze" -> reader.setCardStatus(1); // back to normal
            default -> throw new BusinessException("Invalid action: " + action);
        }
        readerMapper.updateById(reader);
    }

    public List<Map<String, Object>> getReaderTypes() {
        return readerTypeMapper.selectList(null).stream().map(rt -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", rt.getId());
            item.put("name", rt.getName());
            item.put("code", rt.getCode());
            item.put("maxBorrow", rt.getMaxBorrow());
            item.put("borrowDays", rt.getBorrowDays());
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getReaderDetail(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) throw new BusinessException("Reader not found");
        return toReaderItem(reader);
    }

    private Map<String, Object> toReaderItem(Reader reader) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", reader.getId());
        item.put("userId", reader.getUserId());
        item.put("readerNo", reader.getReaderNo());
        item.put("cardStatus", reader.getCardStatus());
        item.put("cardStatusLabel", getCardStatusLabel(reader.getCardStatus()));
        item.put("currentBorrowed", reader.getCurrentBorrowed() != null ? reader.getCurrentBorrowed() : 0);
        item.put("totalBorrowed", reader.getTotalBorrowed());
        item.put("registerDate", reader.getRegisterDate() != null ? reader.getRegisterDate().toString() : null);
        item.put("expireDate", reader.getExpireDate() != null ? reader.getExpireDate().toString() : null);

        // Get user info
        SysUser user = userMapper.selectById(reader.getUserId());
        if (user != null) {
            item.put("realName", user.getRealName());
            item.put("email", user.getEmail());
            item.put("phone", user.getPhone());
        }

        // Get reader type
        if (reader.getReaderTypeId() != null) {
            ReaderType rt = readerTypeMapper.selectById(reader.getReaderTypeId());
            if (rt != null) {
                item.put("readerTypeName", rt.getName());
                item.put("readerTypeId", reader.getReaderTypeId());
                item.put("maxBorrow", rt.getMaxBorrow());
            }
        }

        return item;
    }

    private String getCardStatusLabel(Integer status) {
        if (status == null) return "Normal";
        return switch (status) {
            case 0 -> "Lost";
            case 1 -> "Normal";
            case 2 -> "Frozen";
            default -> "Unknown";
        };
    }

    private String generateReaderNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        // Get current max reader_no
        List<Reader> all = readerMapper.selectList(
                new LambdaQueryWrapper<Reader>()
                        .likeRight(Reader::getReaderNo, "RD" + year)
                        .orderByDesc(Reader::getReaderNo)
                        .last("LIMIT 1"));
        int next = 1;
        if (!all.isEmpty()) {
            String last = all.get(0).getReaderNo();
            next = Integer.parseInt(last.substring(year.length() + 2)) + 1;
        }
        return String.format("RD%s%04d", year, next);
    }
}
