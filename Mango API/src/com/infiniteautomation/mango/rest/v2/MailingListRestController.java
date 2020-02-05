/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;

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

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListModelMapping;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListWithRecipientsModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
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
@RequestMapping("/mailing-lists")
public class MailingListRestController {

    private final MailingListService service;
    private final MailingListModelMapping mapping;
    private final RestModelMapper mapper;

    @Autowired
    public MailingListRestController(MailingListService service, MailingListModelMapping mapping,
            RestModelMapper mapper) {
        this.service = service;
        this.mapping = mapping;
        this.mapper = mapper;

    }

    @ApiOperation(
            value = "Query Mailing Lists",
            notes = "",
            responseContainer="List",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Get a Mailing List",
            notes = "Requires Read Permission to see the addresses",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<MailingListModel> get(
            @ApiParam(value = "XID of Mailing List to get", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(mapping.map(service.get(xid), user, mapper));
    }

    @ApiOperation(
            value = "Create a Mailing List",
            notes = "Requires global Create Mailing List privileges",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MailingListModel> create(
            @RequestBody MailingListWithRecipientsModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        MailingList vo = service.insert(mapping.unmap(model, user, mapper));
        URI location = builder.path("/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Update a Mailing List",
            notes = "Requires edit permission",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<MailingListModel> update(
            @ApiParam(value = "XID of MailingList to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Mailing List of update", required = true, allowMultiple = false)
            @RequestBody MailingListWithRecipientsModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        MailingList vo = service.update(xid, mapping.unmap(model, user, mapper));
        URI location = builder.path("/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a Mailing List",
            notes = "Requires edit permission",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<MailingListModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated mailing list", required = true)
            @PatchVORequestBody(
                    service=MailingListService.class,
                    modelClass=MailingListWithRecipientsModel.class)
            MailingListWithRecipientsModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {


        MailingList vo = service.update(xid, mapping.unmap(model, user, mapper));

        URI location = builder.path("/mailing-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a Mailing List",
            notes = "",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<MailingListModel> delete(
            @ApiParam(value = "XID of Mailing List to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(mapping.map(service.delete(xid), user, mapper));
    }

    @ApiOperation(
            value = "Validate a Mailing List without saving it",
            notes = "Admin Only",
            response=Void.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST, value="/validate")
    public void validate(
            @RequestBody MailingListWithRecipientsModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        service.ensureValid(mapping.unmap(model, user, mapper), user);
    }
    /**
     *
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user) {
        //We can filter in the database on the roles
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, item -> mapping.map(item, user, mapper));
    }
}
