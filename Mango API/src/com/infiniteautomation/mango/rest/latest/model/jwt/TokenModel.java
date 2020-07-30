/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.jwt;

public class TokenModel {
    private String token;
    
    public TokenModel() {
    }
    
    public TokenModel(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
            parts[2] = "<redacted>";
            return "TokenModel [token=" + String.join(".", parts) + "]";
        } else {
            return "TokenModel [token=<redacted>]";
        }
    }
}