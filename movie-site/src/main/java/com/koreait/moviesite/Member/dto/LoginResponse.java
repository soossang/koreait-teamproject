package com.koreait.moviesite.Member.dto;

public class LoginResponse {

    private String tokenType;     // "Bearer"
    private String token;         // JWT
    private long expiresIn;       // seconds
    private String loginId;
    private String role;

    public LoginResponse() {}

    public LoginResponse(String tokenType, String token, long expiresIn, String loginId, String role) {
        this.tokenType = tokenType;
        this.token = token;
        this.expiresIn = expiresIn;
        this.loginId = loginId;
        this.role = role;
    }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
