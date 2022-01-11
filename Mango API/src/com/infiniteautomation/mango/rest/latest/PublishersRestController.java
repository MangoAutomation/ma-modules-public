/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.spring.service.PublisherService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Mango Publishers")
@RestController
@RequestMapping("/publishers")
public class PublishersRestController {

    private final PublisherService service;
    private final PublishedPointService publishedPointService;
    private final BiFunction<PublisherVO, PermissionHolder, AbstractPublisherModel<?,?>> map;
    private final BiFunction<PublisherVO, PermissionHolder, AbstractPublisherModel<?,?>> mapWithoutPoints;
    private final BiFunction<AbstractPublishedPointModel<?>, PermissionHolder, PublishedPointVO> unmapPoint;
    private final PermissionService permissionService;

    @Autowired
    public PublishersRestController(final PublisherService service,
                                    final PublishedPointService publishedPointService,
                                    final RestModelMapper modelMapper, PermissionService permissionService) {
        this.service = service;
        this.publishedPointService = publishedPointService;
        this.map = (vo, user) -> {
            AbstractPublisherModel<?, ?> model = modelMapper.map(vo, AbstractPublisherModel.class, user);
            for(PublishedPointVO point : publishedPointService.getPublishedPoints(vo.getId())) {
                //noinspection unchecked
                model.addPoint(modelMapper.map(point, AbstractPublishedPointModel.class, user));
            }
            return model;
        };
        this.mapWithoutPoints = (vo, user) -> modelMapper.map(vo, AbstractPublisherModel.class, user);
        this.unmapPoint = (model, user) -> modelMapper.unMap(model, PublishedPointVO.class, user);
        this.permissionService = permissionService;
    }


    @ApiOperation(
            value = "Query Publishers Sources",
            notes = "RQL Formatted Query",
            responseContainer="List"
    )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value = "User", required = true)
            @AuthenticationPrincipal PermissionHolder user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Get publisher by XID"
    )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractPublisherModel<?,?> get(
            @ApiParam(value = "XID of publisher", required = true)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get publisher by ID"
    )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractPublisherModel<?,?> getById(
            @ApiParam(value = "ID of publisher", required = true)
            @PathVariable int id,
            @AuthenticationPrincipal PermissionHolder user) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(value = "Save publisher")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractPublisherModel<?,?>> save(
            @RequestBody() AbstractPublisherModel<?, ?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {

        PublisherVO vo = this.service.insert(model.toVO());
        maybeReplacePoints(vo, model.getPoints(), user);
        URI location = builder.path("/publishers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update publisher")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractPublisherModel<?,?>> update(
            @PathVariable String xid,
            @RequestBody() AbstractPublisherModel<?, ?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {

        PublisherVO vo = this.service.update(xid, model.toVO());
        maybeReplacePoints(vo, model.getPoints(), user);
        URI location = builder.path("/publishers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a publisher",
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
        maybeReplacePoints(vo, model.getPoints(), user);
        URI location = builder.path("/publishers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a publisher",
            response=AbstractPublisherModel.class
    )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public AbstractPublisherModel<?,?> delete(
            @ApiParam(value = "XID of publisher to delete", required = true)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {
        return mapWithoutPoints.apply(service.delete(xid), user);
    }

    @ApiOperation(value = "Enable/disable/restart a publisher")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public void enableDisable(
            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the publisher", required = true)
            @RequestParam() boolean enabled,

            @ApiParam(value = "Restart the publisher, enabled must equal true", defaultValue = "false")
            @RequestParam(required = false, defaultValue = "false") boolean restart) {
        service.restart(xid, enabled, restart);
    }


    @ApiOperation(
            value = "Export formatted for Configuration Import"
    )
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xid}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportDataSource(
            @ApiParam(value = "Valid publisher XID", required = true)
            @PathVariable String xid) {

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
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user) {
        if (permissionService.hasAdminRole(user)) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> map.apply(vo, user));
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> service.hasReadPermission(user, vo), vo -> map.apply(vo, user));
        }
    }

    /**
     * If points are not null they will replace any points on this publisher
     */
    private void maybeReplacePoints(PublisherVO publisherVO, List<? extends AbstractPublishedPointModel<?>> points, PermissionHolder user) {
        if(points != null) {
            List<PublishedPointVO> pointVos = new ArrayList<>();
            int i =0;
            for(AbstractPublishedPointModel<?> pm : points) {
                //Ensure publisher xid is set
                pm.setPublisherXid(publisherVO.getXid());
                //Ensure xid is set
                if(StringUtils.isEmpty(pm.getXid())) {
                    pm.setXid(publishedPointService.generateUniqueXid());
                }
                pm.setName(publisherVO.getName() + " point " + i);
                pm.setEnabled(true);
                pointVos.add(unmapPoint.apply(pm, user));
                i++;
            }
            publishedPointService.replacePoints(publisherVO.getId(), pointVos);
        }
    }

}
