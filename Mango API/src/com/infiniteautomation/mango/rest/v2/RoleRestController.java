/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2;

import java.net.URI;

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

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListWithRecipientsModel;
import com.infiniteautomation.mango.rest.v2.model.role.RoleModel;
import com.infiniteautomation.mango.rest.v2.model.role.RoleModelMapping;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.RoleVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Api(value="Roles Rest Controller")
@RestController()
@RequestMapping("/roles")
public class RoleRestController {

    private final RoleService service;
    private final RoleModelMapping mapping;
    private final RestModelMapper mapper;

    @Autowired
    public RoleRestController(RoleService service, RoleModelMapping mapping,
            RestModelMapper mapper) {
        this.service = service;
        this.mapping = mapping;
        this.mapper = mapper;

    }

    @ApiOperation(
            value = "Query Roles",
            notes = "",
            responseContainer="List",
            response=RoleModel.class
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
            value = "Get a Role",
            notes = "",
            response=RoleModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public RoleModel get(
            @ApiParam(value = "XID of Role to get", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return mapping.map(service.get(xid), user, mapper);
    }

    @ApiOperation(
            value = "Create a Role",
            notes = "Admin only",
            response=RoleModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<RoleModel> create(
            @RequestBody RoleModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        RoleVO vo = service.insert(mapping.unmap(model, user, mapper));
        URI location = builder.path("/roles/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Update a Role List",
            notes = "Admin only",
            response=MailingListWithRecipientsModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<RoleModel> update(
            @ApiParam(value = "XID of Role to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Role List of update", required = true, allowMultiple = false)
            @RequestBody RoleModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        RoleVO vo = service.update(xid, mapping.unmap(model, user, mapper));
        URI location = builder.path("/roles/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a Role",
            notes = "Admin only",
            response=RoleModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<RoleModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated role", required = true)
            @PatchVORequestBody(
                    service=RoleService.class,
                    modelClass=RoleModel.class)
            RoleModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {


        RoleVO vo = service.update(xid, mapping.unmap(model, user, mapper));

        URI location = builder.path("/roles/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a Role",
            notes = "",
            response=RoleModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<RoleModel> delete(
            @ApiParam(value = "XID of Role to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(mapping.map(service.delete(xid), user, mapper));
    }

    /**
     *
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user) {
        if (service.getPermissionService().hasAdminRole(user)) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, vo -> mapping.map(vo, user, mapper));
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, vo -> service.hasReadPermission(user, vo), vo -> mapping.map(vo, user, mapper));
        }
    }

}
