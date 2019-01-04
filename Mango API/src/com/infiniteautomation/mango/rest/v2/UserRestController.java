/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody.PatchIdField;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionDetails;

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
    
    @Autowired
    public UserRestController(UsersService service) {
        this.service = service;
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
            notes = "Admin Only",
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
            UriComponentsBuilder builder) {
        
        User newUser = service.update(username, model.toVO(), user);
        URI location = builder.path("/users/{username}").buildAndExpand(newUser.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new UserModel(newUser), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a User",
            notes = "Admin or Patch Self onlyy",
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
            UriComponentsBuilder builder) {
        
        User update = service.update(username, model.toVO(), user);
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
    
    @ApiOperation(value = "Locks a user's password", notes = "The user with a locked password cannot login using a username and password. " +
            "However the user's auth tokens will still work and the user can still reset their password using a reset token or email link")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/lock-password")
    public void lockPassword(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {

        currentUser.ensureHasAdminPermission();
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
    
    //TODO Below here can be moved into a service class
    
    public StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, vo -> map.apply(vo, user), false);
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, vo -> map.apply(vo, user), false);
        }
    } 
}
