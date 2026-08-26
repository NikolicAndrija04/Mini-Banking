package com.minibanking.api_gateway.dto;

public class TokenResponse {

    private String token;
    private String tokenType;
    private long expiresIn;

    public TokenResponse(
            String token,
            String tokenType,
            long expiresIn
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}