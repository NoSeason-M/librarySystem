package com.library.librarysystem.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaCode;
}
