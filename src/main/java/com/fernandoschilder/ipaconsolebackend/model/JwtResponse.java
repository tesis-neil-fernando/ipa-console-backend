package com.fernandoschilder.ipaconsolebackend.model;

import lombok.Getter;
import lombok.Setter;

public class JwtResponse {
    private String token;
    private String type = "Bearer";

    @Getter
    @Setter
    private String username;

    public JwtResponse(String accessToken, String username) {
        this.token = accessToken;
        this.username = username;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

}