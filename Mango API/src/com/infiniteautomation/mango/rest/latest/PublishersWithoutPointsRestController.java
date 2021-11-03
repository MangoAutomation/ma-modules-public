/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.MediaTypes;

/**
 * This controller currently supports a publisher model without points in it. To access the
 * points use the @see com.infiniteautomation.mango.rest.latest.PublishedPointsRestController
 *
 * TODO: This controller is temporary for the v3 REST api, when moving the v4 this should replace
 *  the current /publishers endpoints
 *
 * NOTE: These endpoints are superadmin only as publishers have no permissions
 * @author Terry Packer
 *
 */
@Api(value="Mango Publishers (points not included in model)")
@RestController
@RequestMapping("/publishers-without-points")
public class PublishersWithoutPointsRestController {

    private final PublisherService service;
    private final PublishedPointService publishedPointService;
    private final BiFunction<PublisherVO, PermissionHolder, AbstractPublisherModel<?,?>> map;
    private final PermissionService permissionService;

    @Autowired
    public PublishersWithoutPointsRestController(final PublisherService service,
                                                 final RestModelMapper modelMapper,
                                                 PermissionService permissionService,
                                                 PublishedPointService publishedPointService
                                    ) {
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractPublisherModel.class, user);
        };

        this.permissionService = permissionService;
        this.publishedPointService = publishedPointService;
    }


    @ApiOperation(
            value = "Query Publishers",
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
        return doQuery(rql, user, this.map);
    }

    @ApiOperation(
            value = "Get publisher by XID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractPublisherModel<?,?> get(
            @ApiParam(value = "XID of publisher", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get publisher by ID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractPublisherModel<?,?> getById(
            @ApiParam(value = "ID of publisher", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(value = "Save publisher, if points are supplied in model they will replace all existing points")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractPublisherModel<?,?>> save(
            @RequestBody(required=true) AbstractPublisherModel<?, ?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        PublisherVO vo = this.service.insert(model.toVO());
        URI location = builder.path("/publishers/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update publisher, if points are supplied in model they will replace all existing points")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractPublisherModel<?,?>> update(
            @PathVariable String xid,
            @RequestBody(required=true) AbstractPublisherModel<?, ?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        PublisherVO vo = this.service.update(xid, model.toVO());
        URI location = builder.path("/publishers/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a publisher, if points are supplied in model they will replace all existing points",
            notes = "Requires edit permission"
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractPublisherModel<?,?>> partialUpdate(
            @PathVariable String xid,
            @ApiParam(value = "Updated data source", required = true)
            @PatchVORequestBody(
                    service=PublisherService.class,
                    modelClass=AbstractPublisherModel.class)
            AbstractPublisherModel<?, ?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {

        PublisherVO vo = service.update(xid, model.toVO());
        URI location = builder.path("/publishers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a publisher",
            notes = "Points are never returned as they don't exist after deleting a publisher",
            response=AbstractPublisherModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public AbstractPublisherModel<?,?> delete(
            @ApiParam(value = "XID of publisher to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
            return map.apply(service.delete(xid), user);
    }

    @ApiOperation(value = "Enable/disable/restart a publisher")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public void enableDisable(
            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the publisher", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,

            @ApiParam(value = "Restart the publisher, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart,

            @AuthenticationPrincipal PermissionHolder user) {
        service.restart(xid, enabled, restart);
    }


    @ApiOperation(
            value = "Export formatted for Configuration Import",
            notes = "")
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xid}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportDataSource(
            @ApiParam(value = "Valid publisher XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {

        PublisherVO vo = service.get(xid);
        Map<String,Object> export = new LinkedHashMap<>();
        export.put("publishers", Collections.singletonList(vo));
        return export;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal PermissionHolder user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        if (permissionService.hasAdminRole(user)) {
            export.put("publishers", new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null));
        }else {
            export.put("publishers", new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null,  vo -> service.hasReadPermission(user, vo)));
        }
        return export;
    }

    /**
     * Perform a query
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user, BiFunction<PublisherVO, PermissionHolder, AbstractPublisherModel<?,?>> map) {
        if (permissionService.hasAdminRole(user)) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> map.apply(vo, user));
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> service.hasReadPermission(user, vo), vo -> map.apply(vo, user));
        }
    }
}
