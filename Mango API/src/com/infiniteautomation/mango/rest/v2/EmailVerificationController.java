/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
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
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.model.jwt.HeaderClaimsModel;
import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.spring.components.EmailAddressVerificationService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.Validatable;

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
 * @author Jared Wiltshire
 */
@Api(value="Email verification")
@RestController
@RequestMapping("/email-verification")
public class EmailVerificationController {

    private final UsersService service;
    private final EmailAddressVerificationService emailVerificationService;

    @Autowired
    public EmailVerificationController(UsersService service, EmailAddressVerificationService emailVerificationService) {
        this.service = service;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * CAUTION: This method is public!
     */
    @ApiOperation(value = "Public endpoint that sends an email containing an email verification link",
            notes="This endpoint is for verifying new user's email addresses only, if a user is registered with this email address already they will recieve a warning email.")
    @RequestMapping(method = RequestMethod.POST, value = "/public/send-email")
    public ResponseEntity<Void> publicSendEmail(
            @RequestBody PublicEmailVerificationRequest body) throws AddressException, TemplateException, IOException {

        body.ensureValid();
        emailVerificationService.sendVerificationEmail(body.getEmailAddress(), null, null, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Creates a token for verifying an email address and sends it to that email address",
            notes="If the username is specified then the generated token is used to update that user's email address")
    @RequestMapping(method = RequestMethod.POST, value = "/send-email")
    public ResponseEntity<Void> sendEmail(
            @RequestBody EmailVerificationRequest body,

            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        body.ensureValid();

        User userToUpdate = null;
        String username = body.getUsername();
        if (username != null && !username.isEmpty()) {
            userToUpdate = this.service.get(username, user);
        }

        emailVerificationService.sendVerificationEmail(body.getEmailAddress(), userToUpdate, null, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Creates a token for updating/verifying a user's email address, or for registering a new user if a username is not supplied")
    @RequestMapping(method = RequestMethod.POST, value = "/create-token")
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public ResponseEntity<CreateTokenResponse> createPublicRegistrationToken(
            @RequestBody CreateTokenRequest body,

            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        body.ensureValid();

        User userToUpdate = null;
        String username = body.getUsername();
        if (username != null && !username.isEmpty()) {
            userToUpdate = this.service.get(username, user);
        }

        String token;
        if (body.isSendEmail()) {
            token = emailVerificationService.sendVerificationEmail(body.getEmailAddress(), userToUpdate, body.getExpiry(), user);
        } else {
            token = emailVerificationService.generateToken(body.getEmailAddress(), userToUpdate, body.getExpiry(), user);
        }

        CreateTokenResponse response = new CreateTokenResponse();
        response.setToken(token);
        response.setFullUrl(emailVerificationService.generateEmailVerificationUrl(token));
        response.setRelativeUrl(emailVerificationService.generateRelativeEmailVerificationUrl(token));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * CAUTION: This method is public!
     * However the token's signature is cryptographically verified.
     */
    @ApiOperation(value = "Registers a new user if the token's signature can be verified", notes="The new user is created disabled and must be approved by an administrator.")
    @RequestMapping(method = RequestMethod.POST, value = "/public/register")
    public ResponseEntity<UserModel> verifyEmailCreateUser(
            @RequestBody PublicRegistrationRequest body) {

        body.ensureValid();
        try {
            User newUser = body.getUser().toVO();
            User created = emailVerificationService.publicRegisterNewUser(body.getToken(), newUser);
            return new ResponseEntity<>(new UserModel(created), HttpStatus.OK);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
    }

    /**
     * CAUTION: This method is public!
     * However the token's signature is cryptographically verified.
     */
    @ApiOperation(value = "Updates the target user's email address if the token's signature can be verified")
    @RequestMapping(method = RequestMethod.POST, value = "/public/update-email")
    public ResponseEntity<UserModel> verifyEmailUpdateUser(
            @RequestBody UpdateEmailRequest body) {

        body.ensureValid();
        try {
            User updated = emailVerificationService.updateUserEmailAddress(body.getToken());
            return new ResponseEntity<>(new UserModel(updated), HttpStatus.OK);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
    }

    /**
     * CAUTION: This method is public!
     */
    @ApiOperation(value = "Gets the public key for verifying email verification tokens")
    @RequestMapping(path="/public/public-key", method = RequestMethod.GET)
    public String getPublicKey() {
        return this.emailVerificationService.getPublicKey();
    }

    /**
     * CAUTION: This method is public!
     */
    @ApiOperation(value = "Verify the signature and parse an email verification token", notes="Does NOT verify the claims")
    @RequestMapping(path="/public/verify", method = RequestMethod.GET)
    public HeaderClaimsModel verifyToken(
            @ApiParam(value = "The token to parse", required = true, allowMultiple = false)
            @RequestParam(required=true) String token) {

        try {
            return new HeaderClaimsModel(this.emailVerificationService.parse(token));
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
    }

    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all email verification tokens")
    @RequestMapping(path="/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin() and isPasswordAuthenticated()")
    public ResponseEntity<Void> resetKeys() {
        emailVerificationService.resetKeys();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static class PublicEmailVerificationRequest implements Validatable {
        String emailAddress;

        @Override
        public void validate(ProcessResult response) {
            if (emailAddress == null || emailAddress.isEmpty()) {
                response.addContextualMessage("emailAddress", "validate.required");
            }
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }

    public static class EmailVerificationRequest extends PublicEmailVerificationRequest {
        String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class CreateTokenRequest extends EmailVerificationRequest {
        boolean sendEmail = false;
        Date expiry;

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
    }

    public static class UpdateEmailRequest implements Validatable {
        private String token;

        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public void validate(ProcessResult response) {
            if (StringUtils.isEmpty(token)) {
                response.addContextualMessage("token", "validate.required");
            }
        }
    }

    public static class PublicRegistrationRequest extends UpdateEmailRequest {
        private UserModel user;

        public UserModel getUser() {
            return user;
        }
        public void setUser(UserModel user) {
            this.user = user;
        }

        @Override
        public void validate(ProcessResult response) {
            super.validate(response);
            if (user == null) {
                response.addContextualMessage("user", "validate.required");
            }
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
}
