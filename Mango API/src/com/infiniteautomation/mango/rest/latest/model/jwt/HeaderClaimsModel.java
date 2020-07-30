/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;

public class HeaderClaimsModel {
    private JwsHeader<?> header;
    private Claims body;
    
    public HeaderClaimsModel() {
    }
    
    public HeaderClaimsModel(Jws<Claims> token) {
        this.header = token.getHeader();
        this.body = token.getBody();
    }

    public JwsHeader<?> getHeader() {
        return header;
    }

    public void setHeader(JwsHeader<?> header) {
        this.header = header;
    }

    public Claims getBody() {
        return body;
    }

    public void setBody(Claims body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "HeaderClaimsModel [header=" + header + ", body=" + body + "]";
    }
}