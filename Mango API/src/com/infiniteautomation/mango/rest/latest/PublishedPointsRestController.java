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
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.servlet.http.HttpServletRequest;
import net.jazdw.rql.parser.ASTNode;

import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.tables.DataPoints;
import com.infiniteautomation.mango.db.tables.Publishers;
import com.infiniteautomation.mango.rest.latest.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.latest.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.PublishedPointService;
import com.infiniteautomation.mango.util.ConfigurationExportData;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
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
    private final BiFunction<AbstractPublishedPointModel<?>, PermissionHolder, PublishedPointVO> unmap;
    private final PermissionService permissionService;
    private final Map<String, Field<?>> fieldMap;
    private final TemporaryResourceManager<PublishedPointBulkResponse, AbstractRestException> resourceManager;

    @Autowired
    public PublishedPointsRestController(final PublishedPointService service,
                                         final RestModelMapper modelMapper,
                                         PermissionService permissionService,
                                         TemporaryResourceWebSocketHandler websocket,
                                         Environment environment) {
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractPublishedPointModel.class, user);
        };
        this.unmap = (model, user) -> {
            return modelMapper.unMap(model, PublishedPointVO.class, user);
        };

        this.permissionService = permissionService;
        //Setup any exposed special query aliases to map model fields to db columns
        Publishers publishers = Publishers.PUBLISHERS;
        DataPoints dataPoints = DataPoints.DATA_POINTS;
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("publisherXid", publishers.xid);
        this.fieldMap.put("dataPointXid", dataPoints.xid);
        this.fieldMap.put("publisherType", publishers.publisherType);

        this.resourceManager = new MangoTaskTemporaryResourceManager(permissionService, websocket, environment);
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
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, fieldMap, null, vo -> map.apply(vo, user));
    }

    @ApiOperation(
            value = "Get Published Point by XID",
            notes = ""
    )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractPublishedPointModel<?> get(
            @ApiParam(value = "XID of published point", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {
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

    @ApiOperation(value = "Create published point")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractPublishedPointModel<?>> create(
            @RequestBody(required=true) AbstractPublishedPointModel<?> model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {

        PublishedPointVO vo = this.service.insert(unmap.apply(model, user));
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
            UriComponentsBuilder builder) {

        PublishedPointVO vo = this.service.update(xid, unmap.apply(model, user));
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

        PublishedPointVO vo = service.update(xid, unmap.apply(model, user));

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
        export.put(ConfigurationExportData.PUBLISHED_POINTS, new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null));
        return export;
    }

    @ApiOperation(value = "Bulk get/create/update/delete published points", notes = "User be superadmin")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<PublishedPointBulkResponse, AbstractRestException>> bulkPublishedPointOperation(
            @RequestBody
                    PublishedPointBulkRequest requestBody,
            UriComponentsBuilder builder) {

        VoAction defaultAction = requestBody.getAction();
        AbstractPublishedPointModel<?> defaultBody = requestBody.getBody();
        List<PublishedPointIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (requests.isEmpty()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantBeEmpty", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<PublishedPointBulkResponse, AbstractRestException> responseBody = resourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_PUBLISHED_POINT, resourceId, expiration, timeout, (resource) -> {

                    PublishedPointBulkResponse bulkResponse = new PublishedPointBulkResponse();
                    int i = 0;

                    resource.progressOrSuccess(bulkResponse, i++, requests.size());

                    PermissionHolder resourceUser = Common.getUser();

                    for (PublishedPointIndividualRequest request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        PublishedPointIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, resourceUser, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/published-points/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<PublishedPointBulkResponse, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk published point operations",
            notes = "User can only get their own bulk published point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkOperations(
            ASTNode query,
            Translations translations) {

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FilteredStreamWithTotal<>(() -> {
            return resourceManager.list().stream();
        }, query, translations));

        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk published point operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<PublishedPointBulkResponse, AbstractRestException> updateBulkOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
                    TemporaryResourceStatusUpdate body) {

        TemporaryResource<PublishedPointBulkResponse, AbstractRestException> resource = resourceManager.get(id);
        if (body.getStatus() == TemporaryResource.TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk published point operation using its id", notes = "User can only get their own bulk operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<PublishedPointBulkResponse, AbstractRestException> getBulkOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<PublishedPointBulkResponse, AbstractRestException> resource = resourceManager.get(id);
        return resource;
    }

    @ApiOperation(value = "Remove a bulk published point operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
                    "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<PublishedPointBulkResponse, AbstractRestException> resource = resourceManager.get(id);
        resource.remove();
    }

    private PublishedPointIndividualResponse doIndividualRequest(PublishedPointIndividualRequest request, VoAction defaultAction, AbstractPublishedPointModel<?> defaultBody, PermissionHolder user, UriComponentsBuilder builder) {
        PublishedPointIndividualResponse result = new PublishedPointIndividualResponse();

        try {
            String xid = request.getXid();
            result.setXid(xid);

            VoAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            AbstractPublishedPointModel<?> body = request.getBody() == null ? defaultBody : request.getBody();

            switch (action) {
                case GET:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.get(xid, user));
                    break;
                case CREATE:
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.create(body, user, builder).getBody());
                    break;
                case UPDATE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.update(xid, body, user, builder).getBody());
                    break;
                case DELETE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.delete(xid, user, builder));
                    break;
            }
        } catch (Exception e) {
            result.exceptionCaught(e);
        }

        return result;
    }

    private static final String RESOURCE_TYPE_BULK_PUBLISHED_POINT = "BULK_PUBLISHED_POINT";

    public static class PublishedPointIndividualRequest extends VoIndividualRequest<AbstractPublishedPointModel> {
    }

    public static class PublishedPointIndividualResponse extends VoIndividualResponse<AbstractPublishedPointModel> {
    }

    public static class PublishedPointBulkRequest extends BulkRequest<VoAction, AbstractPublishedPointModel, PublishedPointIndividualRequest> {
    }

    public static class PublishedPointBulkResponse extends BulkResponse<PublishedPointIndividualResponse> {
    }
}
