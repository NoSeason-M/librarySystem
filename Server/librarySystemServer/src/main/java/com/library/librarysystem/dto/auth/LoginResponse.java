package com.library.librarysystem.dto.auth;

import java.util.List;

/**
 * Login response — matches API.md §2.1 spec.
 */
public class LoginResponse {
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;

    // Getters
    public String getAccessToken() { return accessToken; }
    public long getExpiresIn() { return expiresIn; }
    public String getRefreshToken() { return refreshToken; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRealName() { return realName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAvatar() { return avatar; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }

    // Setters
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRealName(String realName) { this.realName = realName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public static LoginResponseBuilder builder() { return new LoginResponseBuilder(); }

    public static class LoginResponseBuilder {
        private final LoginResponse r = new LoginResponse();
        public LoginResponseBuilder accessToken(String v) { r.accessToken = v; return this; }
        public LoginResponseBuilder expiresIn(long v) { r.expiresIn = v; return this; }
        public LoginResponseBuilder refreshToken(String v) { r.refreshToken = v; return this; }
        public LoginResponseBuilder userId(Long v) { r.userId = v; return this; }
        public LoginResponseBuilder username(String v) { r.username = v; return this; }
        public LoginResponseBuilder realName(String v) { r.realName = v; return this; }
        public LoginResponseBuilder email(String v) { r.email = v; return this; }
        public LoginResponseBuilder phone(String v) { r.phone = v; return this; }
        public LoginResponseBuilder avatar(String v) { r.avatar = v; return this; }
        public LoginResponseBuilder roles(List<String> v) { r.roles = v; return this; }
        public LoginResponseBuilder permissions(List<String> v) { r.permissions = v; return this; }
        public LoginResponse build() { return r; }
    }
}
