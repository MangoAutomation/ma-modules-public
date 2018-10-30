/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListModel;
import com.infiniteautomation.mango.spring.service.MailingListService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Mailing List Rest Controller")
@RestController("MailingListRestControllerV2")
@RequestMapping("/v2/mailing-lists")
public class MailingListRestController {

    @Autowired
    private MailingListService service;
    
    @ApiOperation(
            value = "Query Mailing Lists",
            notes = "",
            responseContainer="List",
            response=MailingListModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user, transform);
    }

    @ApiOperation(
            value = "Get a Mailing List",
            notes = "Requires Read Permission to see the addresses",
            response=MailingListModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<MailingListModel> get(
            @ApiParam(value = "XID of Mailing List to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(new MailingListModel(service.getFull(xid, user)));
    }
    
    @ApiOperation(
            value = "Create a Mailing List",
            notes = "Requires global Create Mailing List privileges",
            response=MailingListModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MailingListModel> create(
            @RequestBody MailingListModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        MailingList vo = service.insertFull(model.toVO(), user);
        URI location = builder.path("/v2/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new MailingListModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Update a Mailing List",
            notes = "Requires edit permission",
            response=MailingListModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<MailingListModel> update(
            @ApiParam(value = "XID of MailingList to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Mailing List of update", required = true, allowMultiple = false)
            @RequestBody MailingListModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        MailingList vo = service.updateFull(xid, model.toVO(), user);
        URI location = builder.path("/v2/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new MailingListModel(vo), headers, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<MailingListModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MailingListModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MailingList existing = service.getFull(xid, user);
        MailingListModel existingModel = new MailingListModel(existing);
        existingModel.patch(model);
        MailingList vo = existingModel.toVO();
        vo = service.updateFull(existing, vo, user);

        URI location = builder.path("/v2/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MailingListModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Delete a Mailing List",
            notes = "",
            response=MailingListModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<MailingListModel> delete(
            @ApiParam(value = "XID of Mailing List to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(new MailingListModel(service.delete(xid, user)));
    }
    
    @ApiOperation(
            value = "Validate a Mailing List without saving it",
            notes = "Admin Only",
            response=Void.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST, value="/validate")
    public void validate(
            @RequestBody MailingListModel script,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        
        service.ensureValid(script.toVO(), user);
    }
    
    /**
     * 
     * TODO Move to Service
     * @param rql
     * @param user
     * @param transform2
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user,
            Function<MailingList, Object> transformVO) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, transformVO, true);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, transformVO, true);
        }
    }
    
    final Function<MailingList, Object> transform = item -> {
        return new MailingListModel(item);
    };
}
