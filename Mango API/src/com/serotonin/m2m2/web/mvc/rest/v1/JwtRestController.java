/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.components.JwtService;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * TODO This is Currently Disabled
 * JSON Web Token REST endpoint
 *
 * @author Jared Wiltshire
 *
 */
//@Api(value = "JWT", description = "JSON web tokens")
//@RestController
//@RequestMapping("/v1/jwt")
public class JwtRestController extends MangoRestController {
    @Autowired
    JwtService jwtService;

    @ApiOperation(value = "Create token", notes = "Creates a token for the current user")
    @RequestMapping(path="/create", method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<TokenModel> createToken(
            @RequestParam(required = false) Date expiry,
            @AuthenticationPrincipal User user,
            HttpServletRequest request, HttpServletResponse response) {

        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
        }
        // TODO enforce min/max limits on expiry

        TokenModel token = new TokenModel(user.getUsername(), expiry, jwtService.generateToken(user.getUsername(), expiry));
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Create token for user", notes = "Creates a token for a given user")
    @RequestMapping(path="/create/{username}", method = RequestMethod.GET, produces={"application/json"})
    @Secured("ROLE_SUPERADMIN")
    public ResponseEntity<TokenModel> createTokenForUser(
            @PathVariable String username,
            @RequestParam(required = false) Date expiry,
            HttpServletRequest request, HttpServletResponse response) {

        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
        }
        // TODO enforce min/max limits on expiry

        User requestedUser = UserDao.instance.getUser(username);
        if (requestedUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        TokenModel token = new TokenModel(username, expiry, jwtService.generateToken(username, expiry));
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
    
    public static class TokenModel {
        @JsonProperty
        private String username;
        
        @JsonProperty
        private Date expiry;
        
        @JsonProperty
        private String token;
        
        public TokenModel(String username, Date expiry, String token) {
            this.username = username;
            this.expiry = expiry;
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Date getExpiry() {
            return expiry;
        }

        public void setExpiry(Date expiry) {
            this.expiry = expiry;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}