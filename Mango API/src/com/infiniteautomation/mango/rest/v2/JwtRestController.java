/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoRestController;
import com.serotonin.m2m2.web.mvc.spring.components.UserAuthJwtService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * JSON Web Token REST endpoint
 *
 * @author Jared Wiltshire
 */
@Api(value = "JWT", description = "JSON web tokens")
@RestController
@RequestMapping("/v2/auth-tokens")
public class JwtRestController extends MangoRestController {

    private static final int DEFAULT_EXPIRY = 5 * 60 * 1000; // 5 minutes
    
    private final UserAuthJwtService jwtService;
    
    @Autowired
    public JwtRestController(UserAuthJwtService jwtService) {
        this.jwtService = jwtService;
    }

    @ApiOperation(value = "Create token", notes = "Creates a token for the current user")
    @RequestMapping(path="/create", method = RequestMethod.POST)
    public ResponseEntity<String> createToken(
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
            Date expiry,
            
            @AuthenticationPrincipal User user,
            Authentication authentication) {
        
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
        }

        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + DEFAULT_EXPIRY);
        }

        String token = jwtService.generateToken(user, expiry);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Create token for user", notes = "Creates a token for a given user")
    @RequestMapping(path="/create/{username}", method = RequestMethod.POST)
    //@Secured("ROLE_SUPERADMIN")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<String> createTokenForUser(
            @PathVariable String username,
            
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
            Date expiry,
            
            Authentication authentication) {

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
        }
        
        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + DEFAULT_EXPIRY);
        }

        User user = UserDao.instance.getUser(username);
        if (user == null) {
            throw new NotFoundRestException();
        }
        
        String token = jwtService.generateToken(user, expiry);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Gets the public key for verifying authentication tokens")
    @RequestMapping(path="/public-key", method = RequestMethod.GET)
    public String getPublicKey() {
        return this.jwtService.getPublicKey();
    }

    @ApiOperation(value = "Verifies and parses an authentication token")
    @RequestMapping(path="/verify", method = RequestMethod.GET)
    public Jws<Claims> verifyToken(
            @ApiParam(value = "The token to parse", required = true, allowMultiple = false)
            @RequestParam(required=true) String token) {
        return this.jwtService.parse(token);
    }

}
