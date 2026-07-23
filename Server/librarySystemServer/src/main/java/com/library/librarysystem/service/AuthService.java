package com.library.librarysystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.librarysystem.common.exception.BusinessException;
import com.library.librarysystem.dto.auth.*;
import com.library.librarysystem.entity.*;
import com.library.librarysystem.mapper.*;
import com.library.librarysystem.security.JwtUtils;
import com.library.librarysystem.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ReaderMapper readerMapper;
    private final ReaderTypeMapper readerTypeMapper;
    private final CaptchaService captchaService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.captcha:1}")
    private int captchaEnabled;

    @Value("${security.failed_limit:5}")
    private int failedLimit;

    /**
     * Login
     */
    public LoginResponse login(LoginRequest req) {
        // Find user
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));

        if (user == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Check if user is disabled
        if (user.getStatus() != 1) {
            throw new BusinessException("Account is disabled. Please contact administrator.");
        }

        // Check failed login attempts — captcha required if exceeded
        if (user.getFailedLogin() != null && user.getFailedLogin() >= failedLimit) {
            if (!captchaService.validateCaptcha(req.getCaptchaKey(), req.getCaptchaCode())) {
                throw new BusinessException("Verification code is incorrect or expired");
            }
        }

        // Verify password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            // Increment failed count
            int failed = (user.getFailedLogin() == null ? 0 : user.getFailedLogin()) + 1;
            user.setFailedLogin(failed);
            userMapper.updateById(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Reset failed count on successful login
        user.setFailedLogin(0);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // Load roles
        List<String> roleCodes = getUserRoleCodes(user.getId());

        // Generate JWT
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(7200)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roleCodes)
                .permissions(List.of())
                .build();
    }

    /**
     * Register a new reader account
     */
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        // Validate passwords match
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Check username uniqueness (auto-generate from email prefix)
        String username = generateUsername(req.getFirstName(), req.getLastName());
        SysUser existing = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (existing != null) {
            // Fallback: add random suffix
            username = username + (int)(Math.random() * 1000);
        }

        // Check email uniqueness
        SysUser emailUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, req.getEmail()));
        if (emailUser != null) {
            throw new BusinessException("Email is already registered");
        }

        // Find role = ROLE_READER (id=4)
        SysRole readerRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "ROLE_READER"));
        if (readerRole == null) {
            throw new BusinessException("System configuration error: reader role not found");
        }

        // Find reader type by code
        ReaderType readerType = readerTypeMapper.selectOne(
                new LambdaQueryWrapper<ReaderType>().eq(ReaderType::getCode, req.getReaderType()));
        if (readerType == null) {
            // Default to STUDENT (id=1)
            readerType = readerTypeMapper.selectById(1L);
        }

        // Create sys_user
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getFirstName() + " " + req.getLastName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setStatus(1); // enabled
        user.setFailedLogin(0);
        userMapper.insert(user);

        // Assign ROLE_READER
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(readerRole.getId());
        userRoleMapper.insert(userRole);

        // Create reader record
        Reader reader = new Reader();
        reader.setUserId(user.getId());
        reader.setReaderNo(generateReaderNo());
        reader.setReaderTypeId(readerType.getId());
        reader.setCardStatus(1); // normal
        reader.setRegisterDate(LocalDate.now());
        reader.setExpireDate(LocalDate.now().plusYears(4)); // 4 years validity
        readerMapper.insert(reader);

        // Auto-login after registration — generate tokens
        List<String> roleCodes = List.of("ROLE_READER");
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(7200)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roleCodes)
                .permissions(List.of())
                .build();
    }

    /**
     * Refresh access token
     */
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(401, "Invalid or expired refresh token");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(401, "User account is disabled");
        }

        List<String> roleCodes = getUserRoleCodes(userId);
        String newAccessToken = jwtUtils.generateAccessToken(userId, user.getUsername(), roleCodes);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(7200)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roleCodes)
                .permissions(List.of())
                .build();
    }

    /**
     * Change password
     */
    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException("New passwords do not match");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * Get current user info (for /auth/me)
     */
    public LoginResponse getCurrentUser(UserDetailsImpl userDetails) {
        SysUser user = userMapper.selectById(userDetails.getId());
        List<String> roleCodes = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roleCodes)
                .permissions(List.of())
                .build();
    }

    // ---- Helper methods ----

    private List<String> getUserRoleCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        return userRoles.stream()
                .map(ur -> {
                    SysRole role = roleMapper.selectById(ur.getRoleId());
                    return role != null ? role.getCode() : null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    private String generateUsername(String firstName, String lastName) {
        // Simple username: firstname.lastname + random digit
        String base = (firstName + "." + lastName).toLowerCase().replaceAll("[^a-z.]", "");
        return base.length() > 20 ? base.substring(0, 20) : base;
    }

    private String generateReaderNo() {
        // Format: RD + year + 5-digit sequence
        String year = String.valueOf(LocalDate.now().getYear());
        // Count existing readers this year
        long count = readerMapper.selectCount(
                new LambdaQueryWrapper<Reader>()
                        .likeRight(Reader::getReaderNo, "RD" + year));
        return String.format("RD%s%04d", year, count + 1);
    }
}
