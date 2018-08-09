/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.function.Function;

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

import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.exception.NotFoundException;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

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
@RequestMapping("/v2/users")
public class UserRestController {

    @Autowired
    private UserDao dao;
    private final Function<User, Object> transform = user -> {return new UserModel(user);};
    
    @ApiOperation(
            value = "Query Users",
            notes = "Use RQL formatted query",
            response=UserModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{username}")
    public UserModel getUser(
            @ApiParam(value = "Valid username", required = true, allowMultiple = false)
            @PathVariable String username,
            @AuthenticationPrincipal User user) {
        return new UserModel(get(username, user));
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
        return doQuery(rql, user, transform);
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
        User newUser = insert(model.toVO(), user);
        URI location = builder.path("/v2/users/{username}").buildAndExpand(newUser.getUsername()).toUri();
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
        
        
        User newUser = update(username, model.toVO(), user);
        URI location = builder.path("/v2/users/{username}").buildAndExpand(newUser.getUsername()).toUri();
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
            @RequestBody(required=true)
            UserModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        
        User existing = get(username, user);
        UserModel existingModel = new UserModel(existing);
        existingModel.patch(model);
        User update = existingModel.toVO();
        update = update(existing, update, user);
        URI location = builder.path("/v2/users/{username}").buildAndExpand(update.getUsername()).toUri();
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
        return new UserModel(delete(username, user));
    }
    
    //TODO Below here can be moved into a service class
    
    public StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<User, Object> transformVO) {
        if (user.hasAdminPermission()) {
            return new StreamedVOQueryWithTotal<>(dao, rql, transformVO);
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            ConditionSortLimit conditions = dao.rqlToCondition(rql);
            return new StreamedVOQueryWithTotal<>(dao, conditions, item -> true, transformVO);
        }
    } 
    
    public User get(String username, User reader) throws NotFoundException, PermissionException {
        User u = dao.getUser(username);
        if(u == null)
            throw new NotFoundException();
        ensurePermission(u, reader);
        return u;
    }
    
    public User insert(User toInsert, PermissionHolder inserter) throws PermissionException{
        inserter.ensureHasAdminPermission();
        toInsert.ensureValid();
        dao.saveUser(toInsert);
        return toInsert;
    }
    
    public User update(String username, User update, User updater) {
        return update(get(username, updater), update, updater);
    }
    
    public User update(User existing, User update, User updater) throws PermissionException {
        ensurePermission(existing, updater);
        update.setId(existing.getId());
        update.ensureValid();
        dao.saveUser(update);
        return update;
    }
    
    public User delete(String username, PermissionHolder inserter) throws NotFoundException, PermissionException {
        inserter.ensureHasAdminPermission();
        User user = dao.getUser(username);
        if(user == null)
            throw new NotFoundException();
        dao.deleteUser(user.getId());
        return user;
    }
    
    public void ensurePermission(User toRead, User reader) throws PermissionException {
        if(reader.hasAdminPermission())
            return;
        if(toRead.getId() != reader.getId())
            throw new PermissionException(new TranslatableMessage("permission.exception.doesNotHaveRequiredPermission", reader.getUsername()), reader);
    }
}
