/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoRestController;
import com.serotonin.m2m2.web.mvc.spring.components.UserAuthJwtService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * JSON Web Token REST endpoint
 *
 * @author Jared Wiltshire
 */
@Api(value = "JWT", description = "JSON web tokens")
@RestController
@RequestMapping("/v2/jwt")
public class JwtRestController extends MangoRestController {
    
    @Autowired
    UserAuthJwtService jwtService;

    @ApiOperation(value = "Create token", notes = "Creates a token for the current user")
    @RequestMapping(path="/create", method = RequestMethod.POST)
    public ResponseEntity<String> createToken(
            @RequestParam(required = false) Date expiry,
            @AuthenticationPrincipal User user) {

        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
        }
        // TODO enforce min/max limits on expiry

        String token = jwtService.generateToken(user, expiry);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Create token for user", notes = "Creates a token for a given user")
    @RequestMapping(path="/create/{username}", method = RequestMethod.POST)
    //@Secured("ROLE_SUPERADMIN")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<String> createTokenForUser(
            @PathVariable String username,
            @RequestParam(required = false) Date expiry) {

        if (expiry == null) {
            expiry = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
        }
        // TODO enforce min/max limits on expiry

        User user = UserDao.instance.getUser(username);
        if (user == null) {
            throw new NotFoundRestException();
        }
        
        String token = jwtService.generateToken(user, expiry);
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
}
