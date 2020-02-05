/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.permission.UserRolesDetails;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.permissions.UserRolesDetailsModel;
import com.infiniteautomation.mango.rest.v2.model.user.ApproveUsersModel;
import com.infiniteautomation.mango.rest.v2.model.user.ApprovedUsersModel;
import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody.PatchIdField;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.TranslatableExceptionI;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;

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

    @Autowired
    public UserRestController(UsersService service, MangoSessionRegistry sessionRegistry) {
        this.service = service;
        this.sessionRegistry = sessionRegistry;
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
        return new UserModel(service.get(username));
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
            notes = "Superadmin permission required",
            response=UserModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserModel> createUser(
            @ApiParam(value="User", required=true)
            @RequestBody(required=true)
            UserModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        User newUser = service.insert(model.toVO());
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

        User existing = service.get(username);
        if (existing.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        User update = service.update(existing, model.toVO());

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

        User existing = service.get(username);
        if (existing.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        User update = service.update(existing, model.toVO());

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
        return new UserModel(service.delete(username));
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
            Authentication authentication) {

        User update = service.get(username);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        update.setHomeUrl(url);
        update = service.update(username, update);
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
            Authentication authentication) {

        User update = service.get(username);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        if(mute == null){
            update.setMuted(!update.isMuted());
        }else{
            update.setMuted(mute);
        }
        update = service.update(username, update);
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
        service.lockPassword(username);
    }

    @ApiOperation(value = "Get User Permissions Information for all users")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions")
    public Set<UserRolesDetailsModel> getUserPermissions(
            @AuthenticationPrincipal User user) {
        Set<UserRolesDetailsModel> permissions = new TreeSet<>();
        Set<UserRolesDetails> details = service.getPermissionDetailsForAllUsers();
        for(UserRolesDetails detail : details) {
            permissions.add(new UserRolesDetailsModel(detail));
        }
        return permissions;
    }

    @ApiOperation(value = "Get User Permissions Information for all users, exclude provided groups in query")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions/{query}")
    public Set<UserRolesDetailsModel> getUserPermissions(
            @ApiParam(value = "Query of permissions to show as already added", required = true, allowMultiple = false)
            @PathVariable String query,
            @AuthenticationPrincipal User user) {
        Set<UserRolesDetailsModel> permissions = new TreeSet<>();
        Set<UserRolesDetails> details = service.getPermissionDetailsForAllUsers(PermissionService.explodeLegacyPermissionGroups(query));
        for(UserRolesDetails detail : details) {
            permissions.add(new UserRolesDetailsModel(detail));
        }
        return permissions;
    }


    @ApiOperation(value = "Get All User Groups that a user can 'see'")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups")
    public Set<String> getAllUserGroups(@AuthenticationPrincipal User user) {
        Set<Role> roles = service.getUserRoles();
        Set<String> groups = new HashSet<>();
        for(Role role : roles) {
            groups.add(role.getXid());
        }
        return groups;
    }

    @ApiOperation(value = "Get All User Groups that a user can 'see', Optionally excluding groups")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups/{exclude}")
    public Set<String> getAllUserGroups(
            @ApiParam(value = "Exclude Groups comma separated", required = false, allowMultiple = false, defaultValue="")
            @PathVariable List<String> exclude,
            @AuthenticationPrincipal User user) {
        Set<Role> roles = service.getUserRoles(exclude);
        Set<String> groups = new HashSet<>();
        for(Role role : roles) {
            groups.add(role.getXid());
        }
        return groups;
    }

    @ApiOperation(
            value = "Approve publicly registered User(s)",
            notes = "Superadmin permission required",
            response=UserModel.class
            )
    @RequestMapping(method = RequestMethod.POST, value="/approve-users")
    @PreAuthorize("isAdmin()")
    public ApprovedUsersModel approveUsers(
            @RequestBody()
            ApproveUsersModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ApprovedUsersModel result = new ApprovedUsersModel();
        for(String username : model.getUsernames()) {
            try {
                User approved = service.approveUser(username, model.isSendEmail());
                result.addApproved(approved.getUsername());
            }catch(Exception e) {
                if(e instanceof TranslatableExceptionI) {
                    result.addFailedApproval(username, ((TranslatableExceptionI)e).getTranslatableMessage());
                }else {
                    result.addFailedApproval(username, new TranslatableMessage("common.default", e.getMessage()));
                }
            }
        }
        return result;
    }

    public StreamedArrayWithTotal doQuery(ASTNode rql, User user) {

        if (user.hasAdminRole()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, vo -> map.apply(vo, user));
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, vo -> map.apply(vo, user));
        }
    }
}
