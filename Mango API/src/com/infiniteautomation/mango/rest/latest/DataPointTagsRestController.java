/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.ConditionSortLimitWithTagKeys;
import com.infiniteautomation.mango.rest.latest.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.latest.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.latest.bulk.IndividualRequest;
import com.infiniteautomation.mango.rest.latest.bulk.RestExceptionIndividualResponse;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Data point tags", description="Get and set data point tags")
@RestController()
@RequestMapping("/data-point-tags")
public class DataPointTagsRestController {

    private static final String RESOURCE_TYPE_BULK_DATA_POINT_TAGS = "BULK_DATA_POINT_TAGS";

    public static enum BulkTagAction {
        GET, SET, MERGE, UPDATE // synonym for MERGE
    }

    public static class TagIndividualRequest extends IndividualRequest<BulkTagAction, Map<String, String>> {
        String xid;

        public String getXid() {
            return xid;
        }

        public void setXid(String xid) {
            this.xid = xid;
        }
    }

    public static class TagIndividualResponse extends RestExceptionIndividualResponse<BulkTagAction, Map<String, String>> {
        String xid;

        public String getXid() {
            return xid;
        }

        public void setXid(String xid) {
            this.xid = xid;
        }
    }

    public static class TagBulkRequest extends BulkRequest<BulkTagAction, Map<String, String>, TagIndividualRequest> {
    }

    public static class TagBulkResponse extends BulkResponse<TagIndividualResponse> {
    }

    private final TemporaryResourceManager<TagBulkResponse, AbstractRestException> bulkResourceManager;
    private final DataPointService dataPointService;
    private final DataPointTagsDao dataPointTagsDao;
    private final DataPointDao dataPointDao;
    private final PermissionService permissionService;

    @Autowired
    public DataPointTagsRestController(TemporaryResourceWebSocketHandler websocket, DataPointService dataPointService,
            DataPointTagsDao dataPointTagsDao, DataPointDao dataPointDao, PermissionService permissionService, Environment environment) {
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<TagBulkResponse>(permissionService, websocket, environment);
        this.dataPointService = dataPointService;
        this.dataPointTagsDao = dataPointTagsDao;
        this.dataPointDao = dataPointDao;
        this.permissionService = permissionService;
    }

    @ApiOperation(value = "Query for data point tags using RQL", notes = "User must have read permission for the data points")
    @RequestMapping(method = RequestMethod.GET, value="/points")
    public StreamedArrayWithTotal queryTagsForDataPoint(
            HttpServletRequest request,
            @AuthenticationPrincipal PermissionHolder user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        ConditionSortLimitWithTagKeys conditions = (ConditionSortLimitWithTagKeys) dataPointDao.rqlToCondition(rql, null, null, null);

        return new StreamedVORqlQueryWithTotal<>(dataPointService, conditions, dataPoint -> {
            Map<String, String> tags = dataPointTagsDao.getTagsForDataPointId(dataPoint.getId());

            // we set the tags on the data point then retrieve them so that the device and name tags are removed
            dataPoint.setTags(tags);

            TagIndividualRequest individualRequest = new TagIndividualRequest();
            individualRequest.setAction(BulkTagAction.MERGE);
            individualRequest.setXid(dataPoint.getXid());
            individualRequest.setBody(dataPoint.getTags());

            return individualRequest;
        });
    }

    @ApiOperation(value = "Query for data point tags using RQL", notes = "User must have read permission for the data points")
    @RequestMapping(method = RequestMethod.GET, value="/points", produces=MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryTagsForDataPointCSV(
            HttpServletRequest request,
            @AuthenticationPrincipal PermissionHolder user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        ConditionSortLimitWithTagKeys conditions = (ConditionSortLimitWithTagKeys) dataPointDao.rqlToCondition(rql, null, null, null);

        return new StreamedVORqlQueryWithTotal<>(dataPointService, conditions, dataPoint -> {
            Map<String, String> tags = dataPointTagsDao.getTagsForDataPointId(dataPoint.getId());

            // we set the tags on the data point then retrieve them so that the device and name tags are removed
            dataPoint.setTags(tags);

            ActionAndTags individualRequest = new ActionAndTags();
            individualRequest.setAction(BulkTagAction.MERGE);
            individualRequest.setXid(dataPoint.getXid());
            individualRequest.setTags(dataPoint.getTags());

            return individualRequest;
        });
    }

    @ApiOperation(value = "Set data point tags for multiple points specified by a query", notes = "Only data points that the user has edit permission for will be modified." +
            "Replaces all tags for all points matched by the query")
    @RequestMapping(method = RequestMethod.POST, value="/points")
    public int setTagsForMultiplePoints(
            @RequestBody
            Map<String, String> tags,

            HttpServletRequest request,

            @AuthenticationPrincipal
            User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        ConditionSortLimitWithTagKeys conditions = (ConditionSortLimitWithTagKeys) dataPointDao.rqlToCondition(rql, null, null, null);

        AtomicInteger count = new AtomicInteger();

        dataPointDao.customizedEditQuery(conditions, user, (dataPoint) -> {
            dataPoint.setTags(tags);
            dataPointTagsDao.saveDataPointTags(dataPoint);
            count.incrementAndGet();
        });

        return count.get();
    }

    @ApiOperation(value = "Merge data point tags into a multiple points specified by a query", notes = "Only data points that the user has edit permission for will be modified." +
            "Adds/deletes a tag or replaces the tag value for each tag key. Any other existing tags will be kept. Set the tag value to null to delete the tag.")
    @RequestMapping(method = RequestMethod.PUT, value="/points")
    public int mergeTagsIntoMultiplePoints(
            @RequestBody
            Map<String, String> tags,

            HttpServletRequest request,

            @AuthenticationPrincipal
            User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        ConditionSortLimitWithTagKeys conditions = (ConditionSortLimitWithTagKeys) dataPointDao.rqlToCondition(rql, null, null, null);

        AtomicInteger count = new AtomicInteger();

        dataPointDao.customizedEditQuery(conditions, user, (dataPoint) -> {
            dataPointTagsDao.doInTransaction(txStatus -> {
                Map<String, String> existingTags = dataPointTagsDao.getTagsForDataPointId(dataPoint.getId());

                Map<String, String> newTags = new HashMap<>(existingTags);
                for (Entry<String, String> entry : tags.entrySet()) {
                    String tagKey = entry.getKey();
                    String tagVal = entry.getValue();

                    if (tagVal == null) {
                        newTags.remove(tagKey);
                    } else {
                        newTags.put(tagKey, tagVal);
                    }
                }

                dataPoint.setTags(newTags);
                dataPointTagsDao.saveDataPointTags(dataPoint);
                count.incrementAndGet();
            });
        });

        return count.get();
    }

    @ApiOperation(value = "Get data point tags by data point XID", notes = "User must have read permission for the data point")
    @RequestMapping(method = RequestMethod.GET, value="/point/{xid}")
    public Map<String, String> getTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid) {

        DataPointVO dataPoint = dataPointService.get(xid);

        Map<String, String> tags = dataPointTagsDao.getTagsForDataPointId(dataPoint.getId());
        dataPoint.setTags(tags);

        // we set the tags on the data point then retrieve them so that the device and name tags are removed
        return dataPoint.getTags();
    }

    @ApiOperation(value = "Set data point tags by data point XID", notes = "User must have edit permission for the data points")
    @RequestMapping(method = RequestMethod.POST, value="/point/{xid}")
    public Map<String, String> setTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @RequestBody
            Map<String, String> tags) {

        return dataPointTagsDao.doInTransaction(txStatus -> {
            DataPointVO dataPoint = dataPointService.get(xid);

            PermissionHolder user = Common.getUser();
            permissionService.ensurePermission(user, dataPoint.getEditPermission());

            dataPoint.setTags(tags);
            dataPointTagsDao.saveDataPointTags(dataPoint);

            return dataPoint.getTags();
        });
    }

    @ApiOperation(value = "Merge data point tags by data point XID", notes = "User must have edit permission for the data points." +
            "Adds/deletes a tag or replaces the tag value for each tag key. Any other existing tags will be kept. Set the tag value to null to delete the tag.")
    @RequestMapping(method = RequestMethod.PUT, value="/point/{xid}")
    public Map<String, String> mergeTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @RequestBody
            Map<String, String> tags) {

        return dataPointTagsDao.doInTransaction(txStatus -> {
            DataPointVO dataPoint = dataPointService.get(xid);
            PermissionHolder user = Common.getUser();
            permissionService.ensurePermission(user, dataPoint.getEditPermission());

            Map<String, String> existingTags = dataPointTagsDao.getTagsForDataPointId(dataPoint.getId());

            Map<String, String> newTags = new HashMap<>(existingTags);
            for (Entry<String, String> entry : tags.entrySet()) {
                String tagKey = entry.getKey();
                String tagVal = entry.getValue();

                if (tagVal == null) {
                    newTags.remove(tagKey);
                } else {
                    newTags.put(tagKey, tagVal);
                }
            }

            dataPoint.setTags(newTags);
            dataPointTagsDao.saveDataPointTags(dataPoint);

            // we set the tags on the data point then retrieve them so that the device and name tags are removed
            return dataPoint.getTags();
        });
    }

    private TagIndividualResponse doIndividualRequest(TagIndividualRequest request, BulkTagAction defaultAction, Map<String, String> defaultBody) {
        TagIndividualResponse result = new TagIndividualResponse();

        try {
            String xid = request.getXid();
            if (xid == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
            }
            result.setXid(xid);

            BulkTagAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            Map<String, String> tags = request.getBody() == null ? defaultBody : request.getBody();

            switch (action) {
                case GET:
                    result.setBody(this.getTagsForDataPoint(xid));
                    break;
                case SET:
                    if (tags == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(this.setTagsForDataPoint(xid, tags));
                    break;
                case MERGE:
                case UPDATE:
                    if (tags == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(this.mergeTagsForDataPoint(xid, tags));
                    break;
            }
        } catch (Exception e) {
            result.exceptionCaught(e);
        }

        return result;
    }

    @ApiOperation(value = "Synchronously bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk-sync")
    public TagBulkResponse bulkDataPointTagOperationSync(
            @RequestBody
            TagBulkRequest requestBody) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<TagIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        }

        TagBulkResponse response = new TagBulkResponse();
        for (TagIndividualRequest request : requests) {
            TagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody);
            response.addResponse(individualResponse);
        }

        return response;
    }

    public static class ActionAndTags {
        BulkTagAction action;
        String xid;
        Map<String, String> tags;

        public BulkTagAction getAction() {
            return action;
        }
        public void setAction(BulkTagAction action) {
            this.action = action;
        }
        public String getXid() {
            return xid;
        }
        public void setXid(String xid) {
            this.xid = xid;
        }
        public Map<String, String> getTags() {
            return tags;
        }
        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }
    }

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs for CSV", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestException>> bulkDataPointTagOperationCSV(
            @RequestBody
            List<ActionAndTags> requestBody,
            UriComponentsBuilder builder) {

        TagBulkRequest bulkRequest = new TagBulkRequest();

        bulkRequest.setRequests(requestBody.stream().map(actionAndModel -> {
            Map<String, String> tags = actionAndModel.getTags();
            BulkTagAction action = actionAndModel.getAction();
            String xid = actionAndModel.getXid();

            TagIndividualRequest request = new TagIndividualRequest();
            request.setAction(action == null ? BulkTagAction.MERGE : action);
            request.setXid(xid);
            request.setBody(tags);
            return request;
        }).collect(Collectors.toList()));

        return this.bulkDataPointTagOperation(bulkRequest, builder);
    }

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestException>> bulkDataPointTagOperation(
            @RequestBody
            TagBulkRequest requestBody,
            UriComponentsBuilder builder) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<TagIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<TagBulkResponse, AbstractRestException> responseBody =
                bulkResourceManager.newTemporaryResource(RESOURCE_TYPE_BULK_DATA_POINT_TAGS, resourceId, expiration, timeout, (resource) -> {
                    TagBulkResponse bulkResponse = new TagBulkResponse();

                    int i = 0;
                    resource.progress(bulkResponse, i++, requests.size());

                    for (TagIndividualRequest request : requests) {
                        TagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/data-point-tags/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a list of current bulk tag operations", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointTagOperations(
            ASTNode query,
            Translations translations) {

        List<TemporaryResource<TagBulkResponse, AbstractRestException>> preFiltered = this.bulkResourceManager.list();

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FilteredStreamWithTotal<>(preFiltered, query, translations));
        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk tag operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestException> updateBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body) {

        TemporaryResource<TagBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk tag operation using its id", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestException> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        return bulkResourceManager.get(id);
    }

    @ApiOperation(value = "Remove a bulk tag operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk tag operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<TagBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);
        resource.remove();
    }

    @ApiOperation(value = "Gets all available tags keys", notes = "Only returns tag keys which are present on data points the user has access to")
    @RequestMapping(method = RequestMethod.GET, value="/keys")
    public Set<String> getTagKeys(@AuthenticationPrincipal PermissionHolder user) {
        return dataPointTagsDao.getTagKeys(user);
    }

    /**
     * Lists possible values for a tag key. Restrictions for other tag keys can be given via RQL
     */
    @ApiOperation(value = "Gets tag values for a given tag key", notes = "Only returns tag values which are present on data points the user has access to")
    @RequestMapping(method = RequestMethod.GET, value="/values/{tagKey}")
    public Set<String> getTagValuesForKey(
            @ApiParam(value = "Tag key", required = true, allowMultiple = false)
            @PathVariable String tagKey,

            @AuthenticationPrincipal PermissionHolder user,

            HttpServletRequest request) {

        String queryString = request.getQueryString();

        if (queryString == null || queryString.isEmpty()) {
            return dataPointTagsDao.getTagValuesForKey(tagKey, user);
        }

        ASTNode rql = RQLUtils.parseRQLtoAST(queryString);
        return dataPointTagsDao.getTagValuesForKey(tagKey, rql, user);
    }
}
