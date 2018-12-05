/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.jwt.HeaderClaimsModel;
import com.infiniteautomation.mango.rest.v2.model.jwt.TokenModel;
import com.infiniteautomation.mango.spring.components.TokenAuthenticationService;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * JSON Web Token REST endpoint.
 *
 * WARNING! This REST controller is PUBLIC by default. Add @PreAuthorize annotations to restrict individual end-points.
 *
 * @author Jared Wiltshire
 */
@Api(value = "Authentication tokens", description = "Creates and verifies JWT (JSON web token) authentication tokens")
@RestController
@RequestMapping("/auth-tokens")
public class AuthenticationTokenRestController {

    private final TokenAuthenticationService tokenAuthService;
    private final MangoSessionRegistry sessionRegistry;

    @Autowired
    public AuthenticationTokenRestController(TokenAuthenticationService jwtService, MangoSessionRegistry sessionRegistry) {
        this.tokenAuthService = jwtService;
        this.sessionRegistry = sessionRegistry;
    }

    @ApiOperation(value = "Create auth token", notes = "Creates an authentication token for the current user or for the username specified (admin only)")
    @RequestMapping(path="/create", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated() and isPasswordAuthenticated()")
    public ResponseEntity<TokenModel> createToken(
            @RequestBody
            CreateTokenRequest requestBody,

            @AuthenticationPrincipal User currentUser) {

        Date expiry = requestBody.getExpiry();
        String username = requestBody.getUsername();

        User user = currentUser;
        if (username != null && !username.equals(currentUser.getUsername())) {
            if (!currentUser.hasAdminPermission()) {
                throw new AccessDeniedException(new TranslatableMessage("rest.error.onlyAdminsCanCreateTokens"));
            }

            user = UserDao.getInstance().getUser(username);
            if (user == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.unknownUser", username));
            }
        }

        String token = tokenAuthService.generateToken(user, expiry);
        return new ResponseEntity<>(new TokenModel(token), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Revoke all tokens", notes = "Revokes all tokens for the current user")
    @RequestMapping(path="/revoke", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated() and isPasswordAuthenticated()")
    public ResponseEntity<Void> revokeTokens(
            @AuthenticationPrincipal User user,

            HttpServletRequest request) {

        tokenAuthService.revokeTokens(user);
        sessionRegistry.userUpdated(request, user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Revoke all tokens for user", notes = "Revokes all tokens for a given user")
    @RequestMapping(path="/revoke/{username}", method = RequestMethod.POST)
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public ResponseEntity<Void> createTokenForUser(
            @PathVariable String username,

            HttpServletRequest request) {

        User user = UserDao.getInstance().getUser(username);
        if (user == null) {
            throw new NotFoundRestException();
        }

        tokenAuthService.revokeTokens(user);
        sessionRegistry.userUpdated(request, user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Gets the public key for verifying authentication tokens")
    @RequestMapping(path="/public-key", method = RequestMethod.GET)
    public String getPublicKey() {
        return this.tokenAuthService.getPublicKey();
    }

    @ApiOperation(value = "Verify the sigature and parse an authentication token", notes="Does NOT verify the claims")
    @RequestMapping(path="/verify", method = RequestMethod.GET)
    public HeaderClaimsModel verifyToken(
            @ApiParam(value = "The token to parse", required = true, allowMultiple = false)
            @RequestParam(required=true) String token) {

        Jws<Claims> jwsToken = this.tokenAuthService.parse(token);
        return new HeaderClaimsModel(jwsToken);
    }

    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all authentication tokens")
    @RequestMapping(path="/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public ResponseEntity<Void> resetKeys() {
        tokenAuthService.resetKeys();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static class CreateTokenRequest {
        private String username;
        private Date expiry;

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
        @Override
        public String toString() {
            return "CreateTokenRequest [username=" + username + ", expiry=" + expiry + "]";
        }
    }
}
