/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;

import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.jwt.JwtSignerVerifier;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
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
            notes="This endpoint is for verifying new user's email addresses, if a user is registered with this email address already they will recieve a warning email.")
    @RequestMapping(method = RequestMethod.POST, value = "/public/send-email")
    public ResponseEntity<Void> publicSendEmail(
            @RequestBody String emailAddress,

            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        emailVerificationService.sendVerificationEmail(emailAddress, null, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Creates a token for registering a user with a verified email", notes="Only useful for testing")
    @RequestMapping(method = RequestMethod.POST, value = "/create")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<CreateTokenResponse> createPublicRegistrationToken(
            @RequestBody String emailAddress,

            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        String token = emailVerificationService.generateToken(emailAddress, null, null, user);

        CreateTokenResponse response = new CreateTokenResponse();
        response.setToken(token);
        response.setFullUrl(emailVerificationService.generateEmailVerificationUrl(token));
        response.setRelativeUrl(emailVerificationService.generateRelativeEmailVerificationUrl(token));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Sends an email containing an email verification link for a given user", notes="If username is not supplied, the current user will be updated")
    @RequestMapping(method = RequestMethod.POST, value = "/send-email/{username}")
    public ResponseEntity<Void> sendEmail(
            @ApiParam(value = "Username of the user to update", required = false, allowMultiple = false)
            @PathVariable String username,

            @RequestBody String emailAddress,

            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        User userToUpdate = null;
        if (username != null) {
            userToUpdate = this.service.get(username, user);
        } else {
            userToUpdate = user;
        }

        emailVerificationService.sendVerificationEmail(emailAddress, userToUpdate, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * CAUTION: This method is public!
     * However the token is cryptographically verified.
     */
    @ApiOperation(value = "Verifies an email verification token then creates a new user", notes="The new user is created disabled and must be approved by an administrator.")
    @RequestMapping(method = RequestMethod.POST, value = "/public/create-user")
    public ResponseEntity<UserModel> verifyEmailCreateUser(
            @RequestBody EmailVerificationRequestBody body) {

        body.ensureValid();
        try {
            User newUser = body.getUser().toVO();
            User created = emailVerificationService.publicCreateNewUser(body.getToken(), newUser);
            return new ResponseEntity<>(new UserModel(created), HttpStatus.OK);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
    }

    /**
     * CAUTION: This method is public!
     * However the token is cryptographically verified.
     */
    @ApiOperation(value = "Verifies an email verification token then updates the target user", notes="")
    @RequestMapping(method = RequestMethod.POST, value = "/public/update-user")
    public ResponseEntity<UserModel> verifyEmailUpdateUser(
            @RequestBody String token) {

        try {
            User updated = emailVerificationService.verifyUserEmail(token);
            return new ResponseEntity<>(new UserModel(updated), HttpStatus.OK);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
    }

    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all email verification tokens")
    @RequestMapping(path="/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin()")
    public ResponseEntity<Void> resetKeys() {
        emailVerificationService.resetKeys();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static class EmailVerificationRequestBody implements Validatable {
        private String token;
        private UserModel user;

        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public UserModel getUser() {
            return user;
        }
        public void setUser(UserModel user) {
            this.user = user;
        }

        @Override
        public void validate(ProcessResult response) {
            if(StringUtils.isEmpty(token)) {
                response.addContextualMessage("token", "validate.required");
            }
            if(user == null) {
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
