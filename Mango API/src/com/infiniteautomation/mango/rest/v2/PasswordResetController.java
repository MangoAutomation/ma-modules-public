/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Locale;

import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.jwt.JwtSignerVerifier;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.jwt.HeaderClaimsModel;
import com.infiniteautomation.mango.spring.components.PasswordResetService;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;

import freemarker.template.TemplateException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Password reset REST endpoints
 *
 * WARNING! This REST controller is PUBLIC by default. Add @PreAuthorize annotations to restrict individual end-points.
 *
 * @author Jared Wiltshire
 */
@Api(value = "Password reset", description = "Endpoints for resetting user passwords")
@RestController
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @ApiOperation(value = "Sends the user an email containing a password reset link")
    @RequestMapping(method = RequestMethod.POST, value = "/send-email")
    public ResponseEntity<Void> sendEmail(
            @RequestBody
            SendEmailRequestBody body
            ) throws AddressException, TemplateException, IOException {

        User user = UserDao.getInstance().getUser(body.getUsername());
        if (user == null) {
            throw new NotFoundRestException();
        }

        String email = body.getEmail();
        if (email == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.emailRequired"));
        }

        String providedEmail = email.toLowerCase(Locale.ROOT);
        String userEmail = user.getEmail().toLowerCase(Locale.ROOT);
        if (!providedEmail.equals(userEmail)) {
            throw new BadRequestException(new TranslatableMessage("rest.error.incorrectEmail"));
        }

        if (user.isDisabled()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.userIsDisabled"));
        }

        passwordResetService.sendEmail(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Resets the user's password if the token is correct")
    @RequestMapping(method = RequestMethod.POST, value = "/reset")
    public ResponseEntity<Void> reset(
            @RequestBody PasswordResetRequestBody body) {

        try {
            passwordResetService.resetPassword(body.getToken(), body.getNewPassword());
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidPasswordResetToken"), e);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Gets the public key for verifying password reset tokens")
    @RequestMapping(path="/public-key", method = RequestMethod.GET)
    public String getPublicKey() {
        return this.passwordResetService.getPublicKey();
    }

    @ApiOperation(value = "Verify the sigature and parse a password reset token", notes="Does NOT verify the claims")
    @RequestMapping(path="/verify", method = RequestMethod.GET)
    public HeaderClaimsModel verifyToken(
            @ApiParam(value = "The token to parse", required = true, allowMultiple = false)
            @RequestParam(required=true) String token) {
        return new HeaderClaimsModel(this.passwordResetService.parse(token));
    }

    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all password reset tokens")
    @RequestMapping(path="/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public ResponseEntity<Void> resetKeys() {
        passwordResetService.resetKeys();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Creates a password reset token and link for the given user")
    @RequestMapping(method = RequestMethod.POST, value = "/create")
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public CreateTokenResponse createTokenForUser(
            @RequestBody
            CreateTokenRequest requestBody,

            @AuthenticationPrincipal User currentUser) throws AddressException, TemplateException, IOException {

        String username = requestBody.getUsername();
        boolean lockPassword = requestBody.isLockPassword();
        boolean sendEmail = requestBody.isSendEmail();
        Date expiry = requestBody.getExpiry();

        User user = UserDao.getInstance().getUser(username);
        if (user == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.unknownUser", username));
        }

        if (user.getId() == currentUser.getId()) {
            throw new AccessDeniedException(new TranslatableMessage("rest.error.cantResetOwnUser"));
        }

        if (lockPassword) {
            UserDao.getInstance().lockPassword(user);
        }

        CreateTokenResponse response = new CreateTokenResponse();

        String token = passwordResetService.generateToken(user, expiry);
        response.setToken(token);
        response.setFullUrl(passwordResetService.generateResetUrl(token));
        response.setRelativeUrl(passwordResetService.generateRelativeResetUrl(token));

        if (sendEmail) {
            passwordResetService.sendEmail(user, token);
        }

        return response;
    }


    public static class CreateTokenRequest {
        String username;
        boolean lockPassword = true;
        boolean sendEmail = false;
        Date expiry;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public boolean isLockPassword() {
            return lockPassword;
        }
        public void setLockPassword(boolean lockPassword) {
            this.lockPassword = lockPassword;
        }
        public boolean isSendEmail() {
            return sendEmail;
        }
        public void setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
        }
        public Date getExpiry() {
            return expiry;
        }
        public void setExpiry(Date expiry) {
            this.expiry = expiry;
        }

        @Override
        public String toString() {
            return "CreateTokenRequest [username=" + username + ", lockPassword=" + lockPassword
                    + ", sendEmail=" + sendEmail + ", expiry=" + expiry + "]";
        }

    }

    public static class CreateTokenResponse {
        private String token;
        private URI fullUrl;
        private URI relativeUrl;

        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public URI getFullUrl() {
            return fullUrl;
        }
        public void setFullUrl(URI fullUrl) {
            this.fullUrl = fullUrl;
        }
        public URI getRelativeUrl() {
            return relativeUrl;
        }
        public void setRelativeUrl(URI relativeUrl) {
            this.relativeUrl = relativeUrl;
        }

        @Override
        public String toString() {
            return "CreateTokenResponse [token=" + JwtSignerVerifier.printToken(token) + ", fullUrl=" + fullUrl + ", relativeUrl="
                    + relativeUrl + "]";
        }
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
