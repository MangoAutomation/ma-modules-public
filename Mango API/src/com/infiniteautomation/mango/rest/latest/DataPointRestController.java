/**
 * Copyright (C) 2017  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.infiniteautomation.mango.rest.latest.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.latest.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.ActionAndModel;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.RuntimeStatusModel;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.db.DataSourceTableDefinition;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Data Points")
@RestController(value="DataPointRestControllerV2")
@RequestMapping("/data-points")
public class DataPointRestController {

    private static final String RESOURCE_TYPE_BULK_DATA_POINT = "BULK_DATA_POINT";

    public static class DataPointIndividualRequest extends VoIndividualRequest<DataPointModel> {
    }

    public static class DataPointIndividualResponse extends VoIndividualResponse<DataPointModel> {
    }

    public static class DataPointBulkRequest extends BulkRequest<VoAction, DataPointModel, DataPointIndividualRequest> {
    }

    public static class DataPointBulkResponse extends BulkResponse<DataPointIndividualResponse> {
    }

    private TemporaryResourceManager<DataPointBulkResponse, AbstractRestException> bulkResourceManager;

    private final BiFunction<DataPointVO, User, DataPointModel> map;
    private final Map<String, Function<Object, Object>> valueConverters;
    private final Map<String, Field<?>> fieldMap;
    private final DataPointService service;

    @Autowired
    public DataPointRestController(TemporaryResourceWebSocketHandler websocket, final RestModelMapper modelMapper,
            DataPointService service, DataSourceTableDefinition dataSourceTable, PermissionService permissionService) {
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<DataPointBulkResponse>(permissionService, websocket);
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, DataPointModel.class, user);
        };
        this.valueConverters = new HashMap<>();
        //Setup any exposed special query aliases to map model fields to db columns
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("dataSourceName", dataSourceTable.getAlias("name"));
        this.fieldMap.put("dataSourceTypeName", dataSourceTable.getAlias("typeName"));
        this.fieldMap.put("dataSourceXid", dataSourceTable.getAlias("xid"));
    }

    @ApiOperation(
            value = "Get data point by XID",
            notes = "Only points that user has read permission to are returned"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public DataPointModel getDataPoint(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get data point by ID",
            notes = "Only points that user has read permission to are returned"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
    public DataPointModel getDataPointById(
            @ApiParam(value = "Valid Data Point ID", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal User user) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(value = "Enable/disable/restart a data point")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public ResponseEntity<Void> enableDisable(
            @AuthenticationPrincipal User user,

            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the data point", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,

            @ApiParam(value = "Restart the data point, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart) {

        service.setDataPointState(xid, enabled, restart);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(
            value = "Query Data Points",
            notes = "",
            response=DataPointModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.POST, value = "/query")
    public StreamedArrayWithTotal query(
            @ApiParam(value="Query", required = true)
            @RequestBody(required=true) ASTNode rql,
            @AuthenticationPrincipal User user) {

        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Query Data Points",
            notes = "Use RQL formatted query",
            response=DataPointModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(value = "Gets a list of data points for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.GET, produces=MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsv(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return this.queryCsvPost(rql, user);
    }

    @ApiOperation(value = "Gets a list of data points for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.POST, value = "/query", produces=MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsvPost(
            @ApiParam(value="RQL query AST", required = true)
            @RequestBody ASTNode rql,

            @AuthenticationPrincipal User user) {

        return doQuery(rql, user, dataPointModel -> {
            ActionAndModel<DataPointModel> actionAndModel = new ActionAndModel<>();
            actionAndModel.setAction(VoAction.UPDATE);
            actionAndModel.setOriginalXid(dataPointModel.getXid());
            actionAndModel.setModel(dataPointModel);
            return actionAndModel;
        });
    }

    @ApiOperation(value = "Update an existing data point")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<DataPointModel> updateDataPoint(
            @PathVariable String xid,

            @ApiParam(value = "Updated data point model", required = true)
            @RequestBody(required=true) DataPointModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataPointVO vo = service.update(xid, model.toVO());

        URI location = builder.path("/data-points/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Create a new data point")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<DataPointModel> createDataPoint(
            @ApiParam(value = "Data point model", required = true)
            @RequestBody(required=true) DataPointModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataPointVO vo = service.insert(model.toVO());

        URI location = builder.path("/data-points/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a data point")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public DataPointModel deleteDataPoint(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        DataPointVO vo = service.delete(xid);
        return map.apply(vo, user);
    }

    @ApiOperation(value = "Bulk get/create/update/delete data points",
            notes = "User must have read/edit permission for the data point",
            consumes=MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestException>> bulkDataPointOperationCSV(
            @RequestBody
            List<ActionAndModel<DataPointModel>> points,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        DataPointBulkRequest bulkRequest = new DataPointBulkRequest();

        bulkRequest.setRequests(points.stream().map(actionAndModel -> {
            DataPointModel point = actionAndModel.getModel();
            VoAction action = actionAndModel.getAction();
            String originalXid = actionAndModel.getOriginalXid();
            if (originalXid == null && point != null) {
                originalXid = point.getXid();
            }

            DataPointIndividualRequest request = new DataPointIndividualRequest();
            request.setAction(action == null ? VoAction.UPDATE : action);
            request.setXid(originalXid);
            request.setBody(point);
            return request;
        }).collect(Collectors.toList()));

        return this.bulkDataPointOperation(bulkRequest, user, builder);
    }

    @ApiOperation(value = "Bulk get/create/update/delete data points", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestException>> bulkDataPointOperation(
            @RequestBody
            DataPointBulkRequest requestBody,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        VoAction defaultAction = requestBody.getAction();
        DataPointModel defaultBody = requestBody.getBody();
        List<DataPointIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (requests.isEmpty()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantBeEmpty", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<DataPointBulkResponse, AbstractRestException> responseBody = bulkResourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_DATA_POINT, resourceId, user.getId(), expiration, timeout, (resource) -> {

                    DataPointBulkResponse bulkResponse = new DataPointBulkResponse();
                    int i = 0;

                    resource.progressOrSuccess(bulkResponse, i++, requests.size());

                    for (DataPointIndividualRequest request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        User resourceUser = (User) Common.getUser();
                        DataPointIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, resourceUser, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/data-points/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk data point operations",
            notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointOperations(
            ASTNode query,
            Translations translations) {

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FilteredStreamWithTotal<>(() -> {
            return bulkResourceManager.list().stream();
        }, query, translations));

        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk data point operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<DataPointBulkResponse, AbstractRestException> updateBulkDataPointOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body) {

        TemporaryResource<DataPointBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);
        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk data point operation using its id", notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<DataPointBulkResponse, AbstractRestException> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<DataPointBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);
        return resource;
    }

    @ApiOperation(value = "Remove a bulk data point operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<DataPointBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);
        resource.remove();
    }

    private DataPointIndividualResponse doIndividualRequest(DataPointIndividualRequest request, VoAction defaultAction, DataPointModel defaultBody, User user, UriComponentsBuilder builder) {
        DataPointIndividualResponse result = new DataPointIndividualResponse();

        try {
            String xid = request.getXid();
            result.setXid(xid);

            VoAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            DataPointModel body = request.getBody() == null ? defaultBody : request.getBody();

            switch (action) {
                case GET:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.getDataPoint(xid, user));
                    break;
                case CREATE:
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.createDataPoint(body, user, builder).getBody());
                    break;
                case UPDATE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.updateDataPoint(xid, body, user, builder).getBody());
                    break;
                case DELETE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.deleteDataPoint(xid, user));
                    break;
            }
        } catch (Exception e) {
            result.exceptionCaught(e);
        }

        return result;
    }

    @ApiOperation(
            value = "Export data point(s) formatted for Configuration Import",
            notes = "User must have read permission",
            response=RuntimeStatusModel.class)
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xids}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportDataPoints(
            @ApiParam(value="Data point xids to export.")
            @PathVariable String[] xids,
            @AuthenticationPrincipal User user) {

        Map<String,Object> export = new HashMap<>();
        List<DataPointVO> points = new ArrayList<>();
        for(String xid : xids) {
            DataPointVO dataPoint = service.get(xid);
            points.add(dataPoint);
        }
        export.put("dataPoints", points);
        return export;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        export.put("dataPoints", new StreamedSeroJsonVORqlQuery<>(service, rql, null, this.fieldMap, this.valueConverters));
        return export;
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        return doQuery(rql, user, null);
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<DataPointModel, ?> toModel) {

        final Function<DataPointVO, Object> transformPoint = item -> {
            DataPointModel pointModel = map.apply(item, user);

            // option to apply a further transformation
            if (toModel != null) {
                return toModel.apply(pointModel);
            }

            return pointModel;
        };
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, this.fieldMap, this.valueConverters, transformPoint);
    }

    public class DataSourceSummary {

        private int id;
        private String xid;
        private Set<Role> editRoles;

        public DataSourceSummary(int id, String xid, Set<Role> editRoles){
            this.id = id;
            this.xid = xid;
            this.editRoles = editRoles;
        }

        public int getId() {
            return id;
        }

        public String getXid() {
            return xid;
        }

        public Set<Role> getEditRoles() {
            return editRoles;
        }

    }

}
