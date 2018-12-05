/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionDetails;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Users", description="Users")
@RestController
@RequestMapping("/users")
public class UserRestController extends MangoVoRestController<User, UserModel, UserDao>{

    private static Log LOG = LogFactory.getLog(UserRestController.class);
    private final MangoSessionRegistry sessionRegistry;

    @Autowired
    public UserRestController(MangoSessionRegistry sessionRegistry) {
        super(UserDao.getInstance());
        this.sessionRegistry = sessionRegistry;
    }

    @ApiOperation(value = "Get all users", notes = "Returns a list of all users")
    @RequestMapping(method = RequestMethod.GET, value="list")
    public ResponseEntity<List<UserModel>> getAll(HttpServletRequest request) {
        RestProcessResult<List<UserModel>> result = new RestProcessResult<List<UserModel>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){

            if(Permissions.hasAdminPermission(user)){
                List<UserModel> userModelList = new ArrayList<UserModel>();
                List<User> users = UserDao.getInstance().getUsers();
                for(User u : users){
                    userModelList.add(new UserModel(u));
                }
                return result.createResponseEntity(userModelList);
            }else{
                LOG.warn("Non admin user: " + user.getUsername() + " attempted to access all users");
                result.addRestMessage(this.getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Get current user", notes = "Returns the logged in user")
    @RequestMapping(method = RequestMethod.GET, value = "/current")
    public ResponseEntity<UserModel> getCurrentUser(HttpServletRequest request) {
        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);

        if (result.isOk()) {
            UserModel model = new UserModel(user);
            return result.createResponseEntity(model);
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Get user by name", notes = "Returns the user specified by the given username")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public ResponseEntity<UserModel> getUser(
            @ApiParam(value = "Valid username", required = true, allowMultiple = false)
            @PathVariable String username, HttpServletRequest request) {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(username);
            if(Permissions.hasAdminPermission(user)){
                if (u == null) {
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }
                UserModel model = new UserModel(u);
                return result.createResponseEntity(model);
            }else{
                if(u.getId() != user.getId()){
                    LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }else{
                    //Allow users to access themselves
                    return result.createResponseEntity(new UserModel(u));
                }
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Get new user", notes = "Returns a new user with default values")
    @RequestMapping(method = RequestMethod.GET, value = "/new/user")
    public ResponseEntity<UserModel> getNewUser(HttpServletRequest request) {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        this.checkUser(request, result);
        if(result.isOk()){
            return result.createResponseEntity(new UserModel(new User()));
        }
        return result.createResponseEntity();
    }


    @ApiOperation(value = "Updates a user")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable String username,
            @RequestBody(required=true) UserModel model,
            UriComponentsBuilder builder,
            HttpServletRequest request,
            Authentication authentication) throws RestValidationFailedException {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(username);

            if(Permissions.hasAdminPermission(user)){
                if (u == null) {
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }

                // Cannot make yourself disabled or not admin
                if (user.getId() == u.getId()) {
                    if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
                        throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
                    }

                    boolean failed = false;
                    if (!model.isAdmin()){
                        model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminInvalid")));
                        failed = true;
                    }
                    if (model.getDisabled()){
                        model.addValidationMessage(new ProcessMessage("disabled", new TranslatableMessage("users.validate.adminDisable")));
                        failed = true;
                    }
                    if(failed){
                        result.addRestMessage(getValidationFailedError());
                        return result.createResponseEntity(model);
                    }
                }

                //Cannot Rename a User to an existing Username
                if(!model.getUsername().equals(username)){
                    User existingUser = UserDao.getInstance().getUser(model.getUsername());
                    if(existingUser != null){
                        model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.usernameInUse")));
                        result.addRestMessage(getValidationFailedError());
                        return result.createResponseEntity(model);
                    }
                }

                //Set the ID for the user for validation
                User newUser = model.getData();
                newUser.setId(u.getId());

                String newPassword = newUser.getPassword();
                if (StringUtils.isBlank(newPassword)) {
                    // just use the old password
                    newUser.setPassword(u.getPassword());
                }

                if(!model.validate()){
                    result.addRestMessage(this.getValidationFailedError());
                }else{
                    UserDao.getInstance().saveUser(newUser);
                    sessionRegistry.userUpdated(request, newUser);
                }
                return result.createResponseEntity(model);
            }else{
                if(u.getId() != user.getId()){
                    LOG.warn("Non admin user: " + user.getUsername() + " attempted to update user : " + u.getUsername());
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }else{
                    if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
                        throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
                    }

                    //Allow users to update themselves
                    User newUser = model.getData();
                    newUser.setId(u.getId());

                    String newPassword = newUser.getPassword();
                    if (StringUtils.isBlank(newPassword)) {
                        // just use the old password
                        newUser.setPassword(u.getPassword());
                    }

                    //If we are not Admin we cannot modify our own privs
                    if(!u.isAdmin()){
                        if(!StringUtils.equals(u.getPermissions(), newUser.getPermissions())){
                            model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.cannotChangePermissions")));
                            result.addRestMessage(this.getValidationFailedError());
                            return result.createResponseEntity(model);
                        }
                    }

                    if(!model.validate()){
                        result.addRestMessage(this.getValidationFailedError());
                    }else{

                        // Cannot make yourself disabled admin or not admin
                        boolean failed = false;
                        if (user.getId() == u.getId()) {
                            if (model.getDisabled()){
                                model.addValidationMessage(new ProcessMessage("disabled", new TranslatableMessage("users.validate.adminDisable")));
                                failed = true;
                            }

                            if(u.isAdmin()){
                                //We were superadmin, so we must still have it
                                if(!model.getData().isAdmin()){
                                    model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminInvalid")));
                                    failed = true;
                                }
                            }else{
                                //We were not superadmin so we must not have it
                                if(model.getData().isAdmin()){
                                    model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminGrantInvalid")));
                                    failed = true;
                                }
                            }

                            if(failed){
                                result.addRestMessage(getValidationFailedError());
                                return result.createResponseEntity(model);
                            }
                        }

                        UserDao.getInstance().saveUser(newUser);
                        sessionRegistry.userUpdated(request, newUser);
                        URI location = builder.path("v1/users/{username}").buildAndExpand(model.getUsername()).toUri();
                        result.addRestMessage(getResourceCreatedMessage(location));
                    }
                    return result.createResponseEntity(model);
                }
            }
        }

        return result.createResponseEntity();
    }

    /**
     * Create a new User
     * @param model
     * @param request
     * @return
     * @throws RestValidationFailedException
     */
    @ApiOperation(
            value = "Create New User",
            notes = "Cannot save existing user"
            )
    @ApiResponses({
        @ApiResponse(code = 201, message = "User Created", response=UserModel.class),
        @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
        @ApiResponse(code = 409, message = "User Already Exists")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserModel> createNewUser(
            @ApiParam( value = "User to save", required = true )
            @RequestBody(required=true)
            UserModel model,
            UriComponentsBuilder builder,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.CREATED);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(model.getUsername());
            if(Permissions.hasAdminPermission(user)){
                if (u == null) {
                    //Create new user
                    model.getData().setId(Common.NEW_ID);
                    if(model.validate()){
                        try{
                            User newUser = model.getData();
                            UserDao.getInstance().saveUser(newUser);

                            URI location = builder.path("v1/users/{username}").buildAndExpand(model.getUsername()).toUri();
                            result.addRestMessage(getResourceCreatedMessage(location));
                            return result.createResponseEntity(model);
                        }catch(Exception e){
                            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                            return result.createResponseEntity();
                        }
                    }else{

                        result.addRestMessage(this.getValidationFailedError());
                        return result.createResponseEntity(model);
                    }
                }else{
                    model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.usernameInUse")));
                    result.addRestMessage(getValidationFailedError());
                    return result.createResponseEntity(model);
                }
            }else{
                LOG.warn("Non admin user: " + user.getUsername() + " attempted to create user : " + model.getUsername());
                result.addRestMessage(this.getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Update a user's home url")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/homepage")
    public ResponseEntity<UserModel> updateHomeUrl(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,

            @ApiParam(value = "Home Url", required = true, allowMultiple = false)
            @RequestParam(required=true)
            String url,
            HttpServletRequest request,
            Authentication authentication) throws RestValidationFailedException {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(username);
            if(Permissions.hasAdminPermission(user)){
                if (u == null) {
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }

                if (u.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken)) {
                    throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
                }

                u.setHomeUrl(url);
                UserModel model = new UserModel(u);
                if(!model.validate()){
                    result.addRestMessage(this.getValidationFailedError());
                }else{
                    UserDao.getInstance().saveHomeUrl(u.getId(), url);
                    sessionRegistry.userUpdated(request, u);
                }
                return result.createResponseEntity(model);
            }else{
                if(u.getId() != user.getId()){
                    LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }else{
                    u.setHomeUrl(url);
                    UserModel model = new UserModel(u);
                    //Allow users to update themselves
                    model.getData().setId(u.getId());
                    if(!model.validate()){
                        result.addRestMessage(this.getValidationFailedError());
                    }else{
                        //We have confirmed that we are the user
                        UserDao.getInstance().saveHomeUrl(u.getId(), url);
                        sessionRegistry.userUpdated(request, u);
                    }
                    return result.createResponseEntity(model);
                }
            }
        }

        return result.createResponseEntity();
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
            Authentication authentication) throws RestValidationFailedException {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(username);
            if(Permissions.hasAdminPermission(user)){
                if (u == null) {
                    result.addRestMessage(getDoesNotExistMessage());
                    return result.createResponseEntity();
                }

                if (u.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken)) {
                    throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));
                }

                if(mute == null){
                    u.setMuted(!u.isMuted());
                }else{
                    u.setMuted(mute);
                }
                UserModel model = new UserModel(u);
                if(!model.validate()){
                    result.addRestMessage(this.getValidationFailedError());
                }else{
                    UserDao.getInstance().saveUser(u);
                    sessionRegistry.userUpdated(request, u);
                }
                return result.createResponseEntity(model);
            }else{
                if(u.getId() != user.getId()){
                    LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
                    result.addRestMessage(this.getUnauthorizedMessage());
                    return result.createResponseEntity();
                }else{
                    if(mute == null){
                        u.setMuted(!u.isMuted()); //Toggle
                    }else{
                        u.setMuted(mute);
                    }
                    UserModel model = new UserModel(u);
                    //Allow users to update themselves
                    model.getData().setId(u.getId());
                    if(!model.validate()){
                        result.addRestMessage(this.getValidationFailedError());
                    }else{
                        UserDao.getInstance().saveUser(u);
                        sessionRegistry.userUpdated(request, u);
                    }
                    return result.createResponseEntity(model);
                }
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(value = "Locks a user's password", notes = "The user with a locked password cannot login using a username and password. " +
            "However the user's auth tokens will still work and the user can still reset their password using a reset token or email link")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/lock-password")
    public ResponseEntity<Void> lockPassword(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,

            @AuthenticationPrincipal User currentUser) {

        if (!currentUser.isAdmin()) {
            throw new AccessDeniedException();
        }

        User user = UserDao.getInstance().getUser(username);
        if (user == null) {
            throw new NotFoundRestException();
        }

        UserDao.getInstance().lockPassword(user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
            value = "Query Users",
            notes = "",
            response=UserModel.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=UserModel.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<User>> queryRQL(HttpServletRequest request) {

        RestProcessResult<QueryDataPageStream<User>> result = new RestProcessResult<QueryDataPageStream<User>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            try{
                //Parse the RQL Query
                ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
                if(!user.isAdmin()){
                    query = RQLUtils.addAndRestriction(query, new ASTNode("eq", "id", user.getId()));
                }
                return result.createResponseEntity(getPageStream(query));
            }catch(InvalidRQLRestException e){
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get User Permissions Information for all users",
            notes = "",
            response=PermissionDetails.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=PermissionDetails.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/permissions")
    public ResponseEntity<List<PermissionDetails>> getUserPermissions(HttpServletRequest request) {

        RestProcessResult<List<PermissionDetails>> result = new RestProcessResult<List<PermissionDetails>>(HttpStatus.OK);

        User currentUser = this.checkUser(request, result);
        if(result.isOk()){

            List<PermissionDetails> ds = new ArrayList<>();
            for (User user : UserDao.getInstance().getActiveUsers()){
                PermissionDetails deets = Permissions.getPermissionDetails(currentUser, null, user);
                if(deets != null)
                    ds.add(deets);
            }
            return result.createResponseEntity(ds);
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get User Permissions Information for all users, exclude provided groups in query",
            notes = "",
            response=PermissionDetails.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=PermissionDetails.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/permissions/{query}")
    public ResponseEntity<List<PermissionDetails>> getUserPermissions(
            @ApiParam(value = "Query of permissions to show as already added", required = true, allowMultiple = false)
            @PathVariable String query,
            HttpServletRequest request) {

        RestProcessResult<List<PermissionDetails>> result = new RestProcessResult<List<PermissionDetails>>(HttpStatus.OK);

        User currentUser = this.checkUser(request, result);
        if(result.isOk()){

            List<PermissionDetails> ds = new ArrayList<>();
            for (User user : UserDao.getInstance().getActiveUsers()){
                PermissionDetails deets = Permissions.getPermissionDetails(currentUser, query, user);
                if(deets != null)
                    ds.add(deets);
            }
            return result.createResponseEntity(ds);
        }

        return result.createResponseEntity();
    }


    @ApiOperation(
            value = "Get All User Groups that a user can 'see'",
            notes = "",
            response=String.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=String.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups")
    public ResponseEntity<Set<String>> getAllUserGroups(HttpServletRequest request) {

        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if (result.isOk()) {
            Set<String> groups = new TreeSet<>();
            if(user.isAdmin()) {
                for (User u : UserDao.getInstance().getActiveUsers())
                    groups.addAll(Permissions.explodePermissionGroups(u.getPermissions()));
            }else {
                groups.addAll(user.getPermissionsSet());
            }

            return result.createResponseEntity(groups);
        }

        return result.createResponseEntity();
    }

    @ApiOperation(
            value = "Get All User Groups that a user can 'see', Optionally excluding groups",
            notes = "",
            response=String.class,
            responseContainer="Array"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response=String.class),
            @ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
    })
    @RequestMapping(method = RequestMethod.GET, value = "/permissions-groups/{exclude}")
    public ResponseEntity<Set<String>> getAllUserGroups(
            @ApiParam(value = "Exclude Groups comma separated", required = false, allowMultiple = false, defaultValue="")
            @PathVariable String exclude,
            HttpServletRequest request) {

        RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if (result.isOk()) {
            Set<String> groups = new TreeSet<>();
            if(user.isAdmin()) {
                for (User u : UserDao.getInstance().getActiveUsers())
                    groups.addAll(Permissions.explodePermissionGroups(u.getPermissions()));
            }else {
                groups.addAll(user.getPermissionsSet());
            }
            if (!StringUtils.isEmpty(exclude)) {
                for (String part : exclude.split(","))
                    groups.remove(part);
            }
            return result.createResponseEntity(groups);
        }

        return result.createResponseEntity();
    }


    @ApiOperation(value = "Delete A User")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{username}")
    public ResponseEntity<UserModel> deleteUser(
            @ApiParam(value = "Username", required = true, allowMultiple = false)
            @PathVariable String username,
            HttpServletRequest request) throws RestValidationFailedException {

        RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if(result.isOk()){
            User u = UserDao.getInstance().getUser(username);
            if (u == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            UserModel model = new UserModel(u);
            if(Permissions.hasAdminPermission(user)){
                if(u.getId() == user.getId()){
                    model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.badDelete")));
                    result.addRestMessage(getValidationFailedError());
                    return result.createResponseEntity(model);
                }
                UserDao.getInstance().deleteUser(u.getId());
                return result.createResponseEntity(model);
            }else{
                LOG.warn("Non admin user: " + user.getUsername() + " attempted to delete user : " + u.getUsername());
                result.addRestMessage(this.getUnauthorizedMessage());
            }
        }

        return result.createResponseEntity();
    }


    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(java.lang.Object)
     */
    @Override
    public UserModel createModel(User vo) {
        return new UserModel(vo);
    }

}
