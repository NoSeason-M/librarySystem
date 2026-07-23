package com.library.librarysystem.controller;

import com.library.librarysystem.common.Result;
import com.library.librarysystem.dto.auth.*;
import com.library.librarysystem.security.UserDetailsImpl;
import com.library.librarysystem.service.AuthService;
import com.library.librarysystem.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    /**
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // Stateless JWT — client discards token.
        // Blacklist logic can be added here when Redis is available.
        return Result.success();
    }

    /**
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest req) {
        return Result.success(authService.refreshToken(req.getRefreshToken()));
    }

    /**
     * GET /api/auth/captcha
     */
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.generateCaptcha());
    }

    /**
     * GET /api/auth/me — get current user info
     */
    @GetMapping("/me")
    public Result<LoginResponse> me(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.success(authService.getCurrentUser(userDetails));
    }

    /**
     * PUT /api/auth/password — change password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(userDetails.getId(), req);
        return Result.success();
    }
}
