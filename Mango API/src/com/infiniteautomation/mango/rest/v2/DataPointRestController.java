/**
 * Copyright (C) 2017  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.db.query.ConditionSortLimitWithTagKeys;
import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.v2.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.ActionAndModel;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.v2.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.template.DataPointPropertiesTemplateVO;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;

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

    private TemporaryResourceManager<DataPointBulkResponse, AbstractRestV2Exception> bulkResourceManager;

    public DataPointRestController(@Autowired TemporaryResourceWebSocketHandler websocket) {
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<DataPointBulkResponse>(websocket);
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

        DataPointVO dataPoint = DataPointDao.getInstance().getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        DataPointDao.getInstance().loadPartialRelationalData(dataPoint);

        Permissions.ensureDataPointReadPermission(user, dataPoint);
        return new DataPointModel(dataPoint);
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

        DataPointVO dataPoint = DataPointDao.getInstance().get(id);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        DataPointDao.getInstance().loadPartialRelationalData(dataPoint);

        Permissions.ensureDataPointReadPermission(user, dataPoint);
        return new DataPointModel(dataPoint);
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

        DataPointVO dataPoint = DataPointDao.getInstance().getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }

        Permissions.ensureDataSourcePermission(user, dataPoint.getDataSourceId());

        if (enabled && restart) {
            Common.runtimeManager.restartDataPoint(dataPoint);
        } else {
            Common.runtimeManager.enableDataPoint(dataPoint, enabled);
        }

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

        DataPointVO dataPoint = DataPointDao.getInstance().getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }

        Permissions.ensureDataSourcePermission(user, dataPoint.getDataSourceId());

        // check if they are trying to move it to another data source
        String newDataSourceXid = model.getDataSourceXid();
        if (newDataSourceXid != null && !newDataSourceXid.isEmpty() && !newDataSourceXid.equals(dataPoint.getDataSourceXid())) {
            throw new BadRequestException(new TranslatableMessage("rest.error.pointChangeDataSource"));
        }

        DataPointPropertiesTemplateVO template = null;
        if (model.isTemplateXidWasSet()) {
            if (model.getTemplateXid() != null) {
                template = (DataPointPropertiesTemplateVO) TemplateDao.getInstance().getByXid(model.getTemplateXid());
                if (template == null) {
                    throw new BadRequestException(new TranslatableMessage("invalidTemplateXid"));
                }
            }
        } else if (dataPoint.getTemplateId() != null) {
            template = (DataPointPropertiesTemplateVO) TemplateDao.getInstance().get(dataPoint.getTemplateId());
        }

        DataPointDao.getInstance().loadPartialRelationalData(dataPoint);
        model.copyPropertiesTo(dataPoint);

        // load the template after copying the properties, template properties override the ones in the data point
        if (template != null) {
            dataPoint.withTemplate(template);
        }

        dataPoint.ensureValid();

        // have to load any existing event detectors for the data point as we are about to replace the VO in the runtime manager
        DataPointDao.getInstance().setEventDetectors(dataPoint);
        Common.runtimeManager.saveDataPoint(dataPoint);

        URI location = builder.path("/data-points/{xid}").buildAndExpand(dataPoint.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new DataPointModel(dataPoint), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Create a new data point")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<DataPointModel> createDataPoint(
            @ApiParam(value = "Data point model", required = true)
            @RequestBody(required=true) DataPointModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataSourceVO<?> dataSource = DataSourceDao.getInstance().getByXid(model.getDataSourceXid());
        if (dataSource == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidDataSourceXid"));
        }

        Permissions.ensureDataSourcePermission(user, dataSource);

        DataPointVO dataPoint = new DataPointVO(dataSource);
        model.copyPropertiesTo(dataPoint);

        if (model.getTemplateXid() != null) {
            DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.getInstance().getByXid(model.getTemplateXid());
            if (template == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.invalidTemplateXid"));
            }
            dataPoint.withTemplate(template);
        }

        dataPoint.ensureValid();
        Common.runtimeManager.saveDataPoint(dataPoint);

        URI location = builder.path("/data-points/{xid}").buildAndExpand(dataPoint.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new DataPointModel(dataPoint), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a data point")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public DataPointModel deleteDataPoint(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        DataPointVO dataPoint = DataPointDao.getInstance().getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }

        Permissions.ensureDataSourcePermission(user, dataPoint.getDataSourceId());

        Common.runtimeManager.deleteDataPoint(dataPoint);
        return new DataPointModel(dataPoint);
    }

    @ApiOperation(value = "Bulk get/create/update/delete data points", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> bulkDataPointOperationCSV(
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
    public ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> bulkDataPointOperation(
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
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> responseBody = bulkResourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_DATA_POINT, resourceId, user.getId(), expiration, timeout, (resource, taskUser) -> {

                    DataPointBulkResponse bulkResponse = new DataPointBulkResponse();
                    int i = 0;

                    resource.progress(bulkResponse, i++, requests.size());

                    for (DataPointIndividualRequest request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        DataPointIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, taskUser, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/data-points/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk data point operations",
            notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointOperations(
            @AuthenticationPrincipal
            User user,

            HttpServletRequest request) {

        List<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> preFiltered =
                this.bulkResourceManager.list().stream()
                .filter((tr) -> user.hasAdminPermission() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());

        List<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> results = preFiltered;
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        if (query != null) {
            results = query.accept(new RQLToObjectListQuery<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>>(), preFiltered);
        }

        PageQueryResultModel<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> result =
                new PageQueryResultModel<>(results, preFiltered.size());

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(result);
        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk data point operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> updateBulkDataPointOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminPermission() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk data point operation using its id", notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminPermission() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        return resource;
    }

    @ApiOperation(value = "Remove a bulk data point operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminPermission() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

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
                    result.setBody(this.createDataPoint(body, user, builder).getBody());
                    break;
                case UPDATE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
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

    private static StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        return doQuery(rql, user, null);
    }

    private static StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<DataPointModel, ?> toModel) {
        final Function<DataPointVO, Object> transformPoint = item -> {
            DataPointDao.getInstance().loadPartialRelationalData(item);
            DataPointModel pointModel = new DataPointModel(item);

            // option to apply a further transformation
            if (toModel != null) {
                return toModel.apply(pointModel);
            }

            return pointModel;
        };

        if (user.hasAdminPermission()) {
            return new StreamedVOQueryWithTotal<>(DataPointDao.getInstance(), rql, transformPoint);
        } else {
            // Add some conditions to restrict based on user permissions
            ConditionSortLimitWithTagKeys conditions = DataPointDao.getInstance().rqlToCondition(rql);
            conditions.addCondition(DataPointDao.getInstance().userHasPermission(user));

            DataPointFilter dataPointFilter = new DataPointFilter(user);

            return new StreamedVOQueryWithTotal<>(DataPointDao.getInstance(), conditions, item -> {
                boolean oldFilterMatches = dataPointFilter.hasDataPointReadPermission(item);

                // this is just a double check, permissions should be accounted for via SQL restrictions added by DataPointDao.userHasPermission()
                if (!oldFilterMatches) {
                    throw new RuntimeException("Data point does not match old permission filter");
                }

                return true;
            }, transformPoint);
        }
    }

}
