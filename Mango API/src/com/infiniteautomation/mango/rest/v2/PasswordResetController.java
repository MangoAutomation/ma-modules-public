/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;

import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoRestController;
import com.serotonin.m2m2.web.mvc.spring.components.PasswordResetService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import freemarker.template.TemplateException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * Password reset REST endpoints
 *
 * WARNING! This REST controller is PUBLIC by default. Add @PreAuthorize annotations to restrict individual end-points.
 * 
 * @author Jared Wiltshire
 */
@Api(value = "Password reset", description = "Endpoints for resetting user passwords")
@RestController
@RequestMapping("/v2/password-reset")
public class PasswordResetController extends MangoRestController {

    private final PasswordResetService passwordResetService;
    
    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @ApiOperation(value = "Sends the user an email containing a password reset link")
    @RequestMapping(method = RequestMethod.POST, value = "/send-email")
    public void sendEmail(
            @RequestBody SendEmailRequestBody body) throws AddressException, TemplateException, IOException {
        
        User user = UserDao.instance.getUser(body.getUsername());
        if (user == null) {
            throw new NotFoundRestException();
        }
        
        String email = body.getEmail();
        if (email == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.emailRequired"));
        }
        
        if (!email.equals(user.getEmail())) {
            throw new BadRequestException(new TranslatableMessage("rest.error.incorrectEmail"));
        }
        
        passwordResetService.sendEmail(user);
    }

    @ApiOperation(value = "Resets the user's password if the token is correct")
    @RequestMapping(method = RequestMethod.POST, value = "/reset")
    public void reset(
            @RequestBody PasswordResetRequestBody body) {

        try {
            passwordResetService.resetPassword(body.getToken(), body.getNewPassword());
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidPasswordResetToken"), e);
        }
    }
    
    @ApiOperation(value = "Gets the public key for verifying password reset tokens")
    @RequestMapping(path="/public-key", method = RequestMethod.GET)
    public String getPublicKey() {
        return this.passwordResetService.getPublicKey();
    }

    @ApiOperation(value = "Verify the sigature and parse a password reset token", notes="Does NOT verify the claims")
    @RequestMapping(path="/verify", method = RequestMethod.GET)
    public Jws<Claims> verifyToken(
            @ApiParam(value = "The token to parse", required = true, allowMultiple = false)
            @RequestParam(required=true) String token) {
        return this.passwordResetService.parse(token);
    }
    
    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all password reset tokens")
    @RequestMapping(path="/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin()")
    public void resetKeys() {
        passwordResetService.resetKeys();
    }

    public static class SendEmailRequestBody {
        private String username;
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
    
    public static class PasswordResetRequestBody {
        private String token;
        private String newPassword;
        
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public String getNewPassword() {
            return newPassword;
        }
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
