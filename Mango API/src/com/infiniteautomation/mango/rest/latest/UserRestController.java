/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
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
import com.infiniteautomation.mango.rest.latest.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.latest.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.datasource.RuntimeStatusModel;
import com.infiniteautomation.mango.rest.latest.model.permissions.UserRolesDetailsModel;
import com.infiniteautomation.mango.rest.latest.model.user.ApproveUsersModel;
import com.infiniteautomation.mango.rest.latest.model.user.ApprovedUsersModel;
import com.infiniteautomation.mango.rest.latest.model.user.UserActionAndModel;
import com.infiniteautomation.mango.rest.latest.model.user.UserIndividualRequest;
import com.infiniteautomation.mango.rest.latest.model.user.UserIndividualResponse;
import com.infiniteautomation.mango.rest.latest.model.user.UserModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody.PatchIdField;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.TranslatableExceptionI;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSessionRegistry;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 * @author Jared Wiltshire
 */
@Api(value = "Users V2 Controller")
@RestController("UsersV2RestController")
@RequestMapping("/users")
public class UserRestController {

    //Bulk management
    private static final String RESOURCE_TYPE_BULK_USER = "BULK_USER";
    private final TemporaryResourceManager<UserBulkResponse, AbstractRestException> bulkResourceManager;
    private final BiFunction<User, User, UserModel> map = (vo, user) -> new UserModel(vo);
    private final UsersService service;
    private final MangoSessionRegistry sessionRegistry;

    @Autowired
    public UserRestController(UsersService service, TemporaryResourceWebSocketHandler websocket, MangoSessionRegistry sessionRegistry) {
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<>(service.getPermissionService(), websocket);
        this.service = service;
        this.sessionRegistry = sessionRegistry;
    }

    @ApiOperation(
            value = "Get User by username",
            response = UserModel.class,
            responseContainer = "List"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public UserModel getUser(
            @ApiParam(value = "Valid username", required = true)
            @PathVariable String username) {
        return new UserModel(service.get(username));
    }

    @ApiOperation(value = "Get current user",
            notes = "Returns the logged in user")
    @RequestMapping(method = RequestMethod.GET, value = "/current")
    public UserModel getCurrentUser(
            @AuthenticationPrincipal User user) {
        return new UserModel(user);
    }

    @ApiOperation(
            value = "Query Users",
            notes = "Use RQL formatted query",
            response = UserModel.class,
            responseContainer = "List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user, null);
    }

    @ApiOperation(
            value = "Create User",
            notes = "Superadmin permission required",
            response = UserModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserModel> createUser(
            @ApiParam(value = "User", required = true)
            @RequestBody
            UserModel model,
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
            response = UserModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable String username,
            @ApiParam(value = "User", required = true)
            @RequestBody
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
            response = UserModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{username}")
    public ResponseEntity<UserModel> patchUser(
            @PathVariable String username,
            @ApiParam(value = "User", required = true)
            @PatchVORequestBody(
                    service = UsersService.class,
                    modelClass = UserModel.class,
                    idType = PatchIdField.OTHER,
                    urlPathVariableName = "username")
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

    @ApiOperation(value = "Delete a user", notes = "Admin only")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{username}")
    public UserModel deleteUser(
            @ApiParam(value = "Valid username", required = true)
            @PathVariable String username) {
        return new UserModel(service.delete(username));
    }

    @ApiOperation(value = "Update a user's home url")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/homepage")
    public ResponseEntity<UserModel> updateHomeUrl(
            @ApiParam(value = "Username", required = true)
            @PathVariable String username,

            @ApiParam(value = "Home Url", required = true)
            @RequestParam String url,

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
            @ApiParam(value = "Username", required = true)
            @PathVariable String username,

            @ApiParam(value = "Mute", defaultValue = "Toggle the current setting")
            @RequestParam(required = false)
            Boolean mute,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            @AuthenticationPrincipal User user,
            Authentication authentication) {

        User update = service.get(username);
        if (update.getId() == user.getId() && !(authentication instanceof UsernamePasswordAuthenticationToken))
            throw new AccessDeniedException(new TranslatableMessage("rest.error.usernamePasswordOnly"));

        if (mute == null) {
            update.setMuted(!update.isMuted());
        } else {
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
            @ApiParam(value = "Username", required = true)
            @PathVariable String username) {
        service.lockPassword(username);
    }

    @ApiOperation(value = "Get User Permissions Information for all users")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions")
    public Set<UserRolesDetailsModel> getUserPermissions() {
        Set<UserRolesDetailsModel> permissions = new TreeSet<>();
        Set<UserRolesDetails> details = service.getPermissionDetailsForAllUsers();
        for (UserRolesDetails detail : details) {
            permissions.add(new UserRolesDetailsModel(detail));
        }
        return permissions;
    }

    @ApiOperation(value = "Get User Permissions Information for all users, exclude provided groups in query")
    @RequestMapping(method = RequestMethod.GET, value = "/permissions/{query}")
    public Set<UserRolesDetailsModel> getUserPermissions(
            @ApiParam(value = "Query of permissions to show as already added", required = true)
            @PathVariable String query) {

        Set<UserRolesDetailsModel> permissions = new TreeSet<>();
        Set<UserRolesDetails> details = service.getPermissionDetailsForAllUsers(PermissionService.explodeLegacyPermissionGroups(query));
        for (UserRolesDetails detail : details) {
            permissions.add(new UserRolesDetailsModel(detail));
        }
        return permissions;
    }

    @ApiOperation(
            value = "Approve publicly registered User(s)",
            notes = "Superadmin permission required",
            response = UserModel.class
            )
    @RequestMapping(method = RequestMethod.POST, value = "/approve-users")
    @PreAuthorize("isAdmin()")
    public ApprovedUsersModel approveUsers(
            @RequestBody()
            ApproveUsersModel model) {
        ApprovedUsersModel result = new ApprovedUsersModel();
        for (String username : model.getUsernames()) {
            try {
                User approved = service.approveUser(username, model.isSendEmail());
                result.addApproved(approved.getUsername());
            } catch (Exception e) {
                if (e instanceof TranslatableExceptionI) {
                    result.addFailedApproval(username, ((TranslatableExceptionI) e).getTranslatableMessage());
                } else {
                    result.addFailedApproval(username, new TranslatableMessage("common.default", e.getMessage()));
                }
            }
        }
        return result;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        if (!service.getPermissionService().hasAdminRole(user)) {
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
        }
        export.put("users", new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null));
        return export;
    }

    protected StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<UserModel, ?> toModel) {
        final Function<User, Object> transformUser = item -> {
            UserModel model = map.apply(item, user);

            // option to apply a further transformation
            if (toModel != null) {
                return toModel.apply(model);
            }

            return model;
        };
        if (!service.getPermissionService().hasAdminRole(user)) {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
        }
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, transformUser);
    }

    //Bulk operations
    @ApiOperation(value = "Gets a list of users for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsv(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return this.queryCsvPost(rql, user);
    }

    @ApiOperation(value = "Gets a list of users for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.POST, value = "/query", produces = MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsvPost(
            @ApiParam(value = "RQL query AST", required = true)
            @RequestBody ASTNode rql,

            @AuthenticationPrincipal User user) {

        return doQuery(rql, user, userModel -> {
            UserActionAndModel actionAndModel = new UserActionAndModel();
            actionAndModel.setAction(VoAction.UPDATE);
            actionAndModel.setOriginalUsername(userModel.getUsername());
            actionAndModel.setModel(userModel);
            return actionAndModel;
        });
    }

    @ApiOperation(value = "Bulk get/create/update/delete users",
            notes = "User must have read/edit permission for the user",
            consumes = MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value = "/bulk", consumes = MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<UserBulkResponse, AbstractRestException>> bulkUserOperationCSV(
            @RequestBody
            List<UserActionAndModel> users,

            @AuthenticationPrincipal
            User user,
            HttpServletRequest servletRequest,
            UriComponentsBuilder builder,
            Authentication authentication) {

        UserBulkRequest bulkRequest = new UserBulkRequest();

        bulkRequest.setRequests(users.stream().map(actionAndModel -> {
            UserModel u = actionAndModel.getModel();
            VoAction action = actionAndModel.getAction();
            String originalUsername = actionAndModel.getOriginalUsername();
            if (originalUsername == null && u != null) {
                originalUsername = u.getUsername();
            }

            UserIndividualRequest request = new UserIndividualRequest();
            request.setAction(action == null ? VoAction.UPDATE : action);
            request.setUsername(originalUsername);
            request.setBody(u);
            return request;
        }).collect(Collectors.toList()));

        return this.bulkUserOperation(bulkRequest, user, servletRequest, authentication, builder);
    }

    @ApiOperation(value = "Bulk get/create/update/delete users", notes = "User must have read/edit permission for the user")
    @RequestMapping(method = RequestMethod.POST, value = "/bulk")
    public ResponseEntity<TemporaryResource<UserBulkResponse, AbstractRestException>> bulkUserOperation(
            @RequestBody
            UserBulkRequest requestBody,

            @AuthenticationPrincipal
            User user,
            HttpServletRequest servletRequest,
            Authentication authentication,
            UriComponentsBuilder builder) {

        VoAction defaultAction = requestBody.getAction();
        UserModel defaultBody = requestBody.getBody();
        List<UserIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (requests.isEmpty()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantBeEmpty", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<UserBulkResponse, AbstractRestException> responseBody = bulkResourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_USER, resourceId, user.getId(), expiration, timeout, (resource) -> {

                    UserBulkResponse bulkResponse = new UserBulkResponse();
                    int i = 0;

                    resource.progressOrSuccess(bulkResponse, i++, requests.size());

                    for (UserIndividualRequest request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        User resourceUser = (User) Common.getUser();
                        UserIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, resourceUser, servletRequest, authentication, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/users/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk user operations",
            notes = "User can only get their own bulk operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value = "/bulk")
    public MappingJacksonValue getBulkUserOperations(
            @AuthenticationPrincipal
            User user,
            ASTNode query,
            Translations translations) {

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FilteredStreamWithTotal<>(() -> {
            return bulkResourceManager.list().stream()
                    .filter((tr) -> service.getPermissionService().hasAdminRole(user) || user.getId() == tr.getUserId());
        }, query, translations));

        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk user operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value = "/bulk/{id}")
    public TemporaryResource<UserBulkResponse, AbstractRestException> updateBulkUserOperation(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<UserBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk user operation using its id", notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value = "/bulk/{id}")
    public TemporaryResource<UserBulkResponse, AbstractRestException> getBulkUserOperation(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<UserBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        return resource;
    }

    @ApiOperation(value = "Remove a bulk user operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value = "/bulk/{id}")
    public void removeBulkUserOperation(
            @ApiParam(value = "Temporary resource id", required = true)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<UserBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        resource.remove();
    }

    private UserIndividualResponse doIndividualRequest(UserIndividualRequest request,
            VoAction defaultAction, UserModel defaultBody,
            User user, HttpServletRequest servletRequest,
            Authentication authentication, UriComponentsBuilder builder) {
        UserIndividualResponse result = new UserIndividualResponse();

        try {
            String username = request.getUsername();
            result.setUsername(username);

            VoAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            UserModel body = request.getBody() == null ? defaultBody : request.getBody();

            switch (action) {
                case GET:
                    if (username == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.getUser(username));
                    break;
                case CREATE:
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.createUser(body, builder).getBody());
                    break;
                case UPDATE:
                    if (username == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.updateUser(username, body, user, servletRequest, builder, authentication).getBody());
                    break;
                case DELETE:
                    if (username == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.deleteUser(username));
                    break;
            }
        } catch (Exception e) {
            result.exceptionCaught(e);
        }

        return result;
    }

    @ApiOperation(
            value = "Export user(s) formatted for Configuration Import",
            notes = "User must have read permission",
            response = RuntimeStatusModel.class)
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xids}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportDataSource(
            @ApiParam(value = "Usernames to export.")
            @PathVariable String[] usernames) {

        Map<String, Object> export = new HashMap<>();
        List<User> users = new ArrayList<>();
        for (String xid : usernames) {
            User u = service.get(xid);
            users.add(u);
        }
        export.put("users", users);
        return export;
    }

    public static class UserBulkRequest extends BulkRequest<VoAction, UserModel, UserIndividualRequest> {
    }

    public static class UserBulkResponse extends BulkResponse<UserIndividualResponse> {
    }
}
