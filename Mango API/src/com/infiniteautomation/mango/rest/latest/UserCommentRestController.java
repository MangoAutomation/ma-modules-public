/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.tables.UserComments;
import com.infiniteautomation.mango.db.tables.Users;
import com.infiniteautomation.mango.rest.latest.model.ListWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedBasicVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.comment.UserCommentModel;
import com.infiniteautomation.mango.spring.service.UserCommentService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 *
 * @author Terry Packer
 *
 */
@Api(value="User Comments")
@RestController()
@RequestMapping("/comments")
public class UserCommentRestController {

    private final UserCommentService service;
    private final Map<String, Function<Object, Object>> valueConverterMap;
    private final Map<String, Field<?>> fieldMap;
    private final BiFunction<UserCommentVO, PermissionHolder, UserCommentModel> map = (vo, user) -> {return new UserCommentModel(vo);};

    @Autowired
    public UserCommentRestController(UserCommentService service){
        this.service = service;
        this.valueConverterMap = new HashMap<>();
        this.valueConverterMap.put("commentType", (toConvert) -> {
            return UserCommentVO.COMMENT_TYPE_CODES.getId((String)toConvert);
        });
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("username", Users.USERS.username);
        this.fieldMap.put("referenceId", UserComments.USER_COMMENTS.typeKey);
        this.fieldMap.put("timestamp", UserComments.USER_COMMENTS.ts);
    }

    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface UserCommentQueryResult extends ListWithTotal<UserCommentModel> {
    }

    @ApiOperation(
            value = "Query User Comments",
            response=UserCommentQueryResult.class)
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(query, user);
    }

    /**
     * Create a new User Comment
     *
     * The timestamp and UserID are optional
     * Username is not used for input
     */
    @ApiOperation(
            value = "Create New User Comment"
            )
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserCommentModel> createNewUserComment(
            @ApiParam( value = "User Comment to save", required = true )
            @RequestBody(required=true)
            UserCommentModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        if(model.getTimestamp() <= 0) {
            model.setTimestamp(System.currentTimeMillis());
        }

        //Assign a userId if there isn't one
        if(model.getUserId() <= 0){
            model.setUserId(user.getId());
            model.setUsername(user.getUsername());
        }
        UserCommentVO vo = service.insert(model.toVO());
        URI location = builder.path("/comments/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete A User Comment by XID")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public UserCommentModel deleteUserComment(
            @ApiParam(value = "xid", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return map.apply(service.delete(xid), user);
    }

    @ApiOperation(value = "Get user comment by xid", notes = "Returns the user comment specified by the given xid")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public UserCommentModel getUserComment(
            @ApiParam(value = "Valid xid", required = true, allowMultiple = false)
            @PathVariable String xid, @AuthenticationPrincipal User user) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(value = "Updates a user comment")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<UserCommentModel> updateUserComment(
            @PathVariable String xid,
            @RequestBody(required=true) UserCommentModel model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        //Change the owner
        if(model.getUserId() == 0){
            model.setUserId(user.getId());
            model.setUsername(user.getUsername());
        }
        UserCommentVO updated = service.update(xid, model.toVO());
        URI location = builder.path("/users/{username}").buildAndExpand(updated.getUsername()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(updated, user), headers, HttpStatus.OK);
    }

    protected StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        return new StreamedBasicVORqlQueryWithTotal<>(service, rql, null, fieldMap, valueConverterMap, vo -> service.hasReadPermission(user, vo), vo -> map.apply(vo, user));
    }
}
