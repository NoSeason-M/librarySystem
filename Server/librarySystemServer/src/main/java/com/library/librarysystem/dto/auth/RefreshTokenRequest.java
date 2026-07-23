package com.library.librarysystem.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
