/**
 * Copyright (C) 2017  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.infiniteautomation.mango.db.query.ConditionSortLimitWithTagKeys;
import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.v2.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.v2.bulk.RestExceptionIndividualResponse;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.template.DataPointPropertiesTemplateVO;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.TemporaryResourceWebSocketDefinition;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Data Points", description="Data points")
@RestController(value="DataPointRestControllerV2")
@RequestMapping("/v2/data-points")
public class DataPointRestController extends BaseMangoRestController {

    private static Log LOG = LogFactory.getLog(DataPointRestController.class);
    private static final String RESOURCE_TYPE_BULK_DATA_POINT = "BULK_DATA_POINT";
    
    public static class DataPointIndividualRequest extends VoIndividualRequest<DataPointModel> {
    }
    
    public static class DataPointIndividualResponse extends VoIndividualResponse<DataPointModel> {
    }

    public static class DataPointBulkRequest extends BulkRequest<VoAction, DataPointModel, DataPointIndividualRequest> {
    }
    
    public static class DataPointBulkResponse extends BulkResponse<DataPointIndividualResponse> {
    }

    private TemporaryResourceManager<DataPointBulkResponse, AbstractRestV2Exception> dataPointTemporaryResourceManager;
    private TemporaryResourceWebSocketHandler websocket;

    public DataPointRestController() {
        LOG.info("Creating Data Point v2 Rest Controller.");
        
        this.websocket = (TemporaryResourceWebSocketHandler) ModuleRegistry.getWebSocketHandlerDefinition(TemporaryResourceWebSocketDefinition.TYPE_NAME).getHandlerInstance();
        this.dataPointTemporaryResourceManager = new TemporaryResourceManager<DataPointBulkResponse, AbstractRestV2Exception>(this.websocket) {
            @Override
            public AbstractRestV2Exception exceptionToError(Exception e) {
                return RestExceptionIndividualResponse.exceptionToRestException(e);
            }
        };
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

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        DataPointDao.instance.loadPartialRelationalData(dataPoint);

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

        DataPointVO dataPoint = DataPointDao.instance.get(id);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        DataPointDao.instance.loadPartialRelationalData(dataPoint);

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

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
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

        ASTNode rql = parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(value = "Update an existing data point")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<DataPointModel> updateDataPoint(
            @PathVariable String xid,

            @ApiParam(value = "Updated data point model", required = true)
            @RequestBody(required=true) DataPointModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
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
                template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
                if (template == null) {
                    throw new BadRequestException(new TranslatableMessage("invalidTemplateXid"));
                }
            }
        } else if (dataPoint.getTemplateId() != null) {
            template = (DataPointPropertiesTemplateVO) TemplateDao.instance.get(dataPoint.getTemplateId());
        }

        DataPointDao.instance.loadPartialRelationalData(dataPoint);
        model.copyPropertiesTo(dataPoint);

        // load the template after copying the properties, template properties override the ones in the data point
        if (template != null) {
            dataPoint.withTemplate(template);
        }
        
        dataPoint.ensureValid();
        
        // have to load any existing event detectors for the data point as we are about to replace the VO in the runtime manager
        DataPointDao.instance.setEventDetectors(dataPoint);
        Common.runtimeManager.saveDataPoint(dataPoint);

        URI location = builder.path("/v2/data-points/{xid}").buildAndExpand(xid).toUri();
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

        DataSourceVO<?> dataSource = DataSourceDao.instance.getByXid(model.getDataSourceXid());
        if (dataSource == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.invalidDataSourceXid"));
        }

        Permissions.ensureDataSourcePermission(user, dataSource);

        DataPointVO dataPoint = new DataPointVO(dataSource);
        model.copyPropertiesTo(dataPoint);

        if (model.getTemplateXid() != null) {
            DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
            if (template == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.invalidTemplateXid"));
            }
            dataPoint.withTemplate(template);
        }
        
        dataPoint.ensureValid();
        Common.runtimeManager.saveDataPoint(dataPoint);

        URI location = builder.path("/v2/data-points/{xid}").buildAndExpand(dataPoint.getXid()).toUri();
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

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        
        Permissions.ensureDataPointReadPermission(user, dataPoint);
        
        Common.runtimeManager.deleteDataPoint(dataPoint);
        return new DataPointModel(dataPoint);
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

        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> responseBody =
                dataPointTemporaryResourceManager.newTemporaryResource(RESOURCE_TYPE_BULK_DATA_POINT, resourceId, user, expiration, timeout, (resource) -> {
            DataPointBulkResponse bulkResponse = new DataPointBulkResponse();
            int i = 0;
            
            if (!dataPointTemporaryResourceManager.progress(resource, bulkResponse, i++, requests.size())) {
                // can't update progress, most likely cancelled or timed out
                return;
            }

            for (DataPointIndividualRequest request : requests) {
                DataPointIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, user, builder);
                bulkResponse.addResponse(individualResponse);

                if (!dataPointTemporaryResourceManager.progressOrSuccess(resource, bulkResponse, i++, requests.size())) {
                    // can't update progress, most likely cancelled or timed out
                    return;
                }
            }

            // this shouldn't do anything, its a check only, progressOrSuccess() should have set the resource to SUCCESS already
            dataPointTemporaryResourceManager.success(resource, bulkResponse);
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/v2/data-points/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Get a list of current bulk data point operations", notes = "User can only get their own bulk data point operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public PageQueryResultModel<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> getBulkDataPointOperations(
            @AuthenticationPrincipal
            User user,
            
            HttpServletRequest request) {
        
        List<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> preFiltered =
                this.dataPointTemporaryResourceManager.list().stream()
                        .filter((tr) -> user.isAdmin() || user.getId() == tr.getUserId())
                        .collect(Collectors.toList());
        
        List<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>> results = preFiltered;
        ASTNode query = BaseMangoRestController.parseRQLtoAST(request.getQueryString());
        if (query != null) {
            results = query.accept(new RQLToObjectListQuery<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>>(), preFiltered);
        }
        
        return new PageQueryResultModel<TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception>>(results, preFiltered.size());
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
        
        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = dataPointTemporaryResourceManager.get(id);
        
        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }
        
        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            if (!dataPointTemporaryResourceManager.cancel(resource)) {
                throw new BadRequestException(new TranslatableMessage("rest.error.cancelFailed"));
            };
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
        
        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = dataPointTemporaryResourceManager.get(id);
        
        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }
        
        return resource;
    }

    @ApiOperation(value = "Remove a bulk data point operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
                    "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<DataPointBulkResponse, AbstractRestV2Exception> resource = dataPointTemporaryResourceManager.get(id);
        
        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }
        
        if (!dataPointTemporaryResourceManager.remove(resource)) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantDeleteIncompleteResource"));
        }
        
        return resource;
    }
    
    private DataPointIndividualResponse doIndividualRequest(DataPointIndividualRequest request, VoAction defaultAction, DataPointModel defaultBody, User user, UriComponentsBuilder builder) {
        DataPointIndividualResponse result = new DataPointIndividualResponse();
        
        try {
            String xid = request.getXid();
            
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
        if (user.isAdmin()) {
            return new StreamedVOQueryWithTotal<>(DataPointDao.instance, rql, item -> {
                DataPointDao.instance.loadPartialRelationalData(item);
                return new DataPointModel(item);
            });
        } else {
            // Add some conditions to restrict based on user permissions
            ConditionSortLimitWithTagKeys conditions = DataPointDao.instance.rqlToCondition(rql);
            conditions.addCondition(DataPointDao.instance.userHasPermission(user));

            DataPointFilter dataPointFilter = new DataPointFilter(user);

            return new StreamedVOQueryWithTotal<>(DataPointDao.instance, conditions, item -> {
                boolean oldFilterMatches = dataPointFilter.hasDataPointReadPermission(item);

                // this is just a double check, permissions should be accounted for via SQL restrictions added by DataPointDao.userHasPermission()
                if (!oldFilterMatches) {
                    throw new RuntimeException("Data point does not match old permission filter");
                }

                return true;
            }, item -> {
                DataPointDao.instance.loadPartialRelationalData(item);
                return new DataPointModel(item);
            });
        }
    }

}
