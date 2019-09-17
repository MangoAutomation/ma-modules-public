/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody.PatchIdField;
import com.infiniteautomation.mango.spring.components.EmailAddressVerificationService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.Validatable;
import com.serotonin.m2m2.vo.permission.PermissionDetails;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;

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
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Users V2 Controller")
@RestController("UsersV2RestController")
@RequestMapping("/users")
public class UserRestController {

    private final BiFunction<User, User, UserModel> map = (vo, user) -> {return new UserModel(vo);};
    private final UsersService service;
    private final MangoSessionRegistry sessionRegistry;
    private final EmailAddressVerificationService emailConfirmationService;
    
    @Autowired
    public UserRestController(UsersService service, MangoSessionRegistry sessionRegistry, EmailAddressVerificationService emailConfirmationService) {
        this.service = service;
        this.sessionRegistry = sessionRegistry;
        this.emailConfirmationService = emailConfirmationService;
    }
    
    @ApiOperation(
            value = "Get User by username",
            notes = "",
            response=UserModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{username}")
    public UserModel getUser(
            @ApiParam(value = "Valid username", required = true, allowMultiple = false)
            @PathVariable String username,
            @AuthenticationPrincipal User user) {
        return new UserModel(service.get(username, user));
    }
    
    @ApiOperation(value = "Get current user", 
            notes = "Returns the logged in user")
    @RequestMapping(method = RequestMethod.GET, value = "/current")
    public UserModel getCurrentUser(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        return new UserModel(user);
    }
    
    @ApiOperation(
            value = "Query Users",
            notes = "Use RQL formatted query",
            response=UserModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }
    
    @ApiOperation(
            value = "Create User",
            notes = "Superadmin or permission to created disabled user required",
            response=UserModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserModel> createUser(
            @ApiParam(value="User", required=true)
            @RequestBody(required=true)
            UserModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        User newUser = service.insert(model.toVO(), user);
        URI location = builder.path("/users/{username}").buildAndExpand(newUser.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(newUser), headers, HttpStatus.OK);

    }
    
    @ApiOperation(
            value = "Update User",
            notes = "Admin or Update Self only",
            response=UserModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{username}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable String username,
            @ApiParam(value="User", required=true)
            @RequestBody(required=true)
            UserModel model,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            Authentication authentication) {
        
        User update = service.update(username, model.toVO(), user);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        sessionRegistry.userUpdated(request, update);
        URI location = builder.path("/users/{username}").buildAndExpand(update.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(update), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a User",
            notes = "Admin or Patch Self only",
            response=UserModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value="/{username}")
    
    public ResponseEntity<UserModel> patchUser(
            @PathVariable String username,
            @ApiParam(value="User", required=true)
            @PatchVORequestBody(
                    service=UsersService.class,
                    modelClass=UserModel.class,
                    idType=PatchIdField.OTHER,
                    urlPathVariableName="username")
            UserModel model,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            Authentication authentication) {
        
        User update = service.update(username, model.toVO(), user);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        
        sessionRegistry.userUpdated(request, update);
        URI location = builder.path("/users/{username}").buildAndExpand(update.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(update), headers, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Delete a user", notes="Admin only")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{username}")
    public UserModel deleteUser(
            @ApiParam(value = "Valid username", required = true, allowMultiple = false)
            @PathVariable String username,
            @AuthenticationPrincipal User user) {
        return new UserModel(service.delete(username, user));
    }
    

    @ApiOperation(value = "Update a user's home url")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/homepage")
    public ResponseEntity<UserModel> updateHomeUrl(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,

            @ApiParam(value = "Home Url", required = true, allowMultiple = false)
            @RequestParam(required=true)
            String url,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            Authentication authentication) throws RestValidationFailedException {

        User update = service.get(username, user);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        update.setHomeUrl(url);
        update = service.update(username, update, user);
        sessionRegistry.userUpdated(request, update);
        URI location = builder.path("/users/{username}").buildAndExpand(update.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(update), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Update a user's audio mute setting",
            notes = "If you do not provide the mute parameter the current setting will be toggled"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/mute")
    public ResponseEntity<UserModel> updateMuted(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,

            @ApiParam(value = "Mute", required = false, defaultValue="Toggle the current setting", allowMultiple = false)
            @RequestParam(required=false)
            Boolean mute,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            @AuthenticationPrincipal User user,
            Authentication authentication) throws RestValidationFailedException {

        User update = service.get(username, user);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        if(mute == null){
            update.setMuted(!update.isMuted());
        }else{
            update.setMuted(mute);
        }
        update = service.update(username, update, user);
        sessionRegistry.userUpdated(request, update);
        URI location = builder.path("/users/{username}").buildAndExpand(update.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(update), headers, HttpStatus.OK);

    }
    
    @ApiOperation(value = "Locks a user's password", notes = "The user with a locked password cannot login using a username and password. " +
            "However the user's auth tokens will still work and the user can still reset their password using a reset token or email link")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/lock-password")
    public void lockPassword(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        service.lockPassword(username, currentUser);
    }
    
    @ApiOperation(value = "Get User Permissions Information for all users")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions")
    public Set<PermissionDetails> getUserPermissions(
            @AuthenticationPrincipal User user) {
        return service.getPermissionDetails(null, user);
    }

    @ApiOperation(value = "Get User Permissions Information for all users, exclude provided groups in query")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions/{query}")
    public Set<PermissionDetails> getUserPermissions(
            @ApiParam(value = "Query of permissions to show as already added", required = true, allowMultiple = false)
            @PathVariable String query,
            @AuthenticationPrincipal User user) {

        return service.getPermissionDetails(query, user);
    }


    @ApiOperation(value = "Get All User Groups that a user can 'see'")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups")
    public Set<String> getAllUserGroups(@AuthenticationPrincipal User user) {
        return service.getUserGroups(null, user);
    }

    @ApiOperation(value = "Get All User Groups that a user can 'see', Optionally excluding groups")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups/{exclude}")
    public Set<String> getAllUserGroups(
            @ApiParam(value = "Exclude Groups comma separated", required = false, allowMultiple = false, defaultValue="")
            @PathVariable List<String> exclude,
            @AuthenticationPrincipal User user) {
        return service.getUserGroups(exclude, user);
    }

    
    
    @ApiOperation(value = "Public endpoint that sends an email containing an email confirmation link", notes="This endpoint is for new users, existing users will recieve a warning email")
    @RequestMapping(method = RequestMethod.POST, value = "/registration/public/send-email")
    public ResponseEntity<Void> sendEmailPublic(@RequestBody String emailAddress) throws AddressException, TemplateException, IOException {

        //Is the system property set
        if(!Common.envProps.getBoolean("web.security.publicRegistrationEnabled", false)) {
            throw new AccessDeniedException();
        }
        
        emailConfirmationService.sendEmail(emailAddress, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Sends an email containing an email confirmation link for a given user")
    @RequestMapping(method = RequestMethod.POST, value = "/registration/send-email")
    public ResponseEntity<Void> sendEmail(
            @RequestBody int userId,
            @AuthenticationPrincipal User user) throws AddressException, TemplateException, IOException {

        if(!Permissions.hasAdminPermission(user) && userId != user.getId()) {
            throw new PermissionException(new TranslatableMessage("rest.error.cannotValidateAnotherUsersEmail"), user);
        }
        emailConfirmationService.sendEmail(service.get(userId, user), null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Public endpoint to verify the user's email if the token is correct", notes="If a User model is supplied and this is a new user, a new disabled user is created")
    @RequestMapping(method = RequestMethod.POST, value = "/registration/public/verify-email")
    public ResponseEntity<Void> verify(
            @RequestBody EmailVerificationRequestBody body) {

        body.ensureValid();
        try {
            User newUser = body.getUser().toVO();
            emailConfirmationService.verifyEmail(body.getToken(), newUser);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Verifies the user's email if the token is correct", notes="")
    @RequestMapping(method = RequestMethod.POST, value = "/registration/verify-email")
    public ResponseEntity<Void> verifyPublic(
            @RequestBody String token) {
        try {
            emailConfirmationService.verifyEmail(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException | MissingClaimException | IncorrectClaimException e) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidEmailVerificationToken"), e);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
    @ApiOperation(value = "Resets the public and private keys", notes = "Will invalidate all email verification tokens")
    @RequestMapping(path="/registration/reset-keys", method = RequestMethod.POST)
    @PreAuthorize("isAdmin()")
    public ResponseEntity<Void> resetKeys() {
        emailConfirmationService.resetKeys();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    public StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, vo -> map.apply(vo, user), false);
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, vo -> map.apply(vo, user), false);
        }
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
}
