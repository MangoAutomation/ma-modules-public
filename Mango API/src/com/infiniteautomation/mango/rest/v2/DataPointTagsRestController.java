/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.v2.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.v2.bulk.IndividualRequest;
import com.infiniteautomation.mango.rest.v2.bulk.RestExceptionIndividualResponse;
import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.TemporaryResourceWebSocketDefinition;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Data point tags", description="Get and set data point tags")
@RestController()
@RequestMapping("/v2/data-point-tags")
public class DataPointTagsRestController extends BaseMangoRestController {

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

    private TemporaryResourceManager<TagBulkResponse, AbstractRestV2Exception> bulkResourceManager;
    private TemporaryResourceWebSocketHandler websocket;

    public DataPointTagsRestController() {
        this.websocket = (TemporaryResourceWebSocketHandler) ModuleRegistry.getWebSocketHandlerDefinition(TemporaryResourceWebSocketDefinition.TYPE_NAME).getHandlerInstance();
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<TagBulkResponse>(this.websocket);
    }

    @ApiOperation(value = "Get data point tags by data point XID", notes = "User must have read permission for the data point")
    @RequestMapping(method = RequestMethod.GET, value="/point/{xid}")
    public Map<String, String> getTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @AuthenticationPrincipal User user) {

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        Permissions.ensureDataPointReadPermission(user, dataPoint);

        Map<String, String> tags = DataPointTagsDao.instance.getTagsForDataPointId(dataPoint.getId());
        dataPoint.setTags(tags);

        // we set the tags on the data point then retrieve them so that the device and name tags are removed
        return dataPoint.getTags();
    }

    @ApiOperation(value = "Set data point tags by data point XID", notes = "User must have edit permission for the data point's data source")
    @RequestMapping(method = RequestMethod.POST, value="/point/{xid}")
    public Map<String, String> setTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @RequestBody
            Map<String, String> tags,

            @AuthenticationPrincipal
            User user) {

        return DataPointTagsDao.instance.doInTransaction(txStatus -> {
            DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
            if (dataPoint == null) {
                throw new NotFoundRestException();
            }
            Permissions.ensureDataSourcePermission(user, dataPoint.getDataSourceId());

            dataPoint.setTags(tags);
            DataPointTagsDao.instance.saveDataPointTags(dataPoint);

            return dataPoint.getTags();
        });
    }

    @ApiOperation(value = "Merge data point tags by data point XID", notes = "User must have edit permission for the data point's data source." +
            "Adds a tag or replaces the tag value for each tag key. Any other existing tags will be kept.")
    @RequestMapping(method = RequestMethod.PUT, value="/point/{xid}")
    public Map<String, String> addTagsForDataPoint(
            @ApiParam(value = "Data point XID", required = true, allowMultiple = false)
            @PathVariable String xid,

            @RequestBody
            Map<String, String> tags,

            @AuthenticationPrincipal
            User user) {

        return DataPointTagsDao.instance.doInTransaction(txStatus -> {
            DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
            if (dataPoint == null) {
                throw new NotFoundRestException();
            }
            Permissions.ensureDataSourcePermission(user, dataPoint.getDataSourceId());

            Map<String, String> existingTags = DataPointTagsDao.instance.getTagsForDataPointId(dataPoint.getId());

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
            DataPointTagsDao.instance.saveDataPointTags(dataPoint);

            // we set the tags on the data point then retrieve them so that the device and name tags are removed
            return dataPoint.getTags();
        });
    }

    private TagIndividualResponse doIndividualRequest(TagIndividualRequest request, BulkTagAction defaultAction, Map<String, String> defaultBody, User user) {
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
                    result.setBody(this.getTagsForDataPoint(xid, user));
                    break;
                case SET:
                    if (tags == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(this.setTagsForDataPoint(xid, tags, user));
                    break;
                case MERGE:
                case UPDATE:
                    if (tags == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(this.addTagsForDataPoint(xid, tags, user));
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
            TagBulkRequest requestBody,

            @AuthenticationPrincipal
            User user) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<TagIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        }

        TagBulkResponse response = new TagBulkResponse();
        for (TagIndividualRequest request : requests) {
            TagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, user);
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
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes="text/csv")
    public ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> bulkDataPointTagOperationCsv(
            @RequestBody
            List<ActionAndTags> requestBody,

            @AuthenticationPrincipal
            User user,

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

        return this.bulkDataPointTagOperation(bulkRequest, user, builder);
    }

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> bulkDataPointTagOperation(
            @RequestBody
            TagBulkRequest requestBody,

            @AuthenticationPrincipal
            User user,

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

        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> responseBody =
                bulkResourceManager.newTemporaryResource(RESOURCE_TYPE_BULK_DATA_POINT_TAGS, resourceId, user.getId(), expiration, timeout, (resource, taskUser) -> {
                    TagBulkResponse bulkResponse = new TagBulkResponse();

                    int i = 0;
                    resource.progress(bulkResponse, i++, requests.size());

                    for (TagIndividualRequest request : requests) {
                        TagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, taskUser);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/v2/data-point-tags/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a list of current bulk tag operations", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointTagOperations(
            @AuthenticationPrincipal
            User user,

            HttpServletRequest request) {

        List<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> preFiltered = this.bulkResourceManager.list().stream()
                .filter((tr) -> user.isAdmin() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());

        List<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> results = preFiltered;
        ASTNode query = BaseMangoRestController.parseRQLtoAST(request.getQueryString());
        if (query != null) {
            results = query.accept(new RQLToObjectListQuery<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>>(), preFiltered);
        }

        PageQueryResultModel<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> result = new PageQueryResultModel<>(results, preFiltered.size());

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(result);
        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk tag operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestV2Exception> updateBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk tag operation using its id", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        return resource;
    }

    @ApiOperation(value = "Remove a bulk tag operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk tag operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        resource.remove();
    }

    @ApiOperation(value = "Gets all available tags keys", notes = "Only returns tag keys which are present on data points the user has access to")
    @RequestMapping(method = RequestMethod.GET, value="/keys")
    public Set<String> getTagKeys(@AuthenticationPrincipal User user) {
        return DataPointTagsDao.instance.getTagKeys(user);
    }

    /**
     * Lists possible values for a tag key. Restrictions for other tag keys can be given via RQL
     * @param tagKey
     * @param restrictions
     * @return
     */
    @ApiOperation(value = "Gets tag values for a given tag key", notes = "Only returns tag values which are present on data points the user has access to")
    @RequestMapping(method = RequestMethod.GET, value="/values/{tagKey}")
    public Set<String> getTagValuesForKey(
            @ApiParam(value = "Tag key", required = true, allowMultiple = false)
            @PathVariable String tagKey,

            @AuthenticationPrincipal User user,

            HttpServletRequest request) {

        String queryString = request.getQueryString();

        if (queryString == null || queryString.isEmpty()) {
            return DataPointTagsDao.instance.getTagValuesForKey(tagKey, user);
        }

        ASTNode rql = parseRQLtoAST(queryString);
        return DataPointTagsDao.instance.getTagValuesForKey(tagKey, rql, user);
    }
}
