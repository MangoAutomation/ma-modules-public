/*
 * Copyright (C) 2021 RadixIot LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import javax.servlet.http.HttpServletRequest;
import net.jazdw.rql.parser.ASTNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.web.MediaTypes;

/**
 * Access to Published Points, superadmin only
 *
 * @author Terry Packer
 *
 */
@Api(value="Mango Published Points")
@RestController
@RequestMapping("/published-points")
public class PublishedPointsRestController {

    private final PublishedPointService service;
    private final BiFunction<PublishedPointVO, PermissionHolder, AbstractPublishedPointModel<?>> map;
    private final PermissionService permissionService;

    @Autowired
    public PublishedPointsRestController(final PublishedPointService service, final RestModelMapper modelMapper, PermissionService permissionService) {
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractPublishedPointModel.class, user);
        };
        this.permissionService = permissionService;
    }

    @ApiOperation(
            value = "Query Published Points",
            notes = "RQL Formatted Query",
            responseContainer="List"
    )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        permissionService.ensureAdminRole(user);
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> map.apply(vo, user));
    }

    @ApiOperation(
            value = "Get Published Point by XID",
            notes = ""
    )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractPublishedPointModel<?> get(
            @ApiParam(value = "XID of published point", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get published point by ID",
            notes = ""
    )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractPublishedPointModel<?> getById(
            @ApiParam(value = "ID of published point", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(value = "Save published point")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractPublishedPointModel<?>> save(
            @RequestBody(required=true) AbstractPublishedPointModel<?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        PublishedPointVO vo = this.service.insert(model.toVO());
        URI location = builder.path("/published-points/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update published point")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractPublishedPointModel<?>> update(
            @PathVariable String xid,
            @RequestBody(required=true) AbstractPublishedPointModel<?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        PublishedPointVO vo = this.service.update(xid, model.toVO());
        URI location = builder.path("/published-points/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a published point",
            notes = "Requires edit permission"
    )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractPublishedPointModel<?>> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated published point", required = true)
            @PatchVORequestBody(
                    service= PublishedPointService.class,
                    modelClass=AbstractPublishedPointModel.class)
                    AbstractPublishedPointModel<?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {

        PublishedPointVO vo = service.update(xid, model.toVO());

        URI location = builder.path("/published-points/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a published point",
            notes = "",
            response=AbstractPublishedPointModel.class
    )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public AbstractPublishedPointModel<?> delete(
            @ApiParam(value = "XID of published point to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return map.apply(service.delete(xid), user);
    }

    @ApiOperation(value = "Enable/disable/restart a published point")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public void enableDisable(
            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the published point", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,

            @ApiParam(value = "Restart the published point, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart,

            @AuthenticationPrincipal PermissionHolder user) {
        service.setPublishedPointState(xid, enabled, restart);
    }


    @ApiOperation(
            value = "Export formatted for Configuration Import",
            notes = "")
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xid}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportPublishedPoint(
            @ApiParam(value = "Valid published point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {

        PublishedPointVO vo = service.get(xid);
        Map<String,Object> export = new LinkedHashMap<>();
        export.put("publishedPoints", Collections.singletonList(vo));
        return export;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        Map<String, JsonStreamedArray> export = new HashMap<>();
        export.put("publishedPoints", new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null));
        return export;
    }
}
