/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.v2.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.v2.bulk.IndividualRequest;
import com.infiniteautomation.mango.rest.v2.bulk.RestExceptionIndividualResponse;
import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
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
    
    public static enum BulkTagAction {
        GET, SET, MERGE
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

    private TemporaryResourceManager<TagBulkResponse, AbstractRestV2Exception> bulkTagsTemporaryResourceManager;
    private TemporaryResourceWebSocketHandler websocket;

    public DataPointTagsRestController() {
        this.websocket = (TemporaryResourceWebSocketHandler) ModuleRegistry.getWebSocketHandlerDefinition(TemporaryResourceWebSocketDefinition.TYPE_NAME).getHandlerInstance();
        this.bulkTagsTemporaryResourceManager = new TemporaryResourceManager<TagBulkResponse, AbstractRestV2Exception>(this.websocket) {
            @Override
            public AbstractRestV2Exception exceptionToError(Exception e) {
                return RestExceptionIndividualResponse.exceptionToRestException(e);
            }
        };
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
            Map<String, String> newTags = new HashMap<>();
            newTags.putAll(existingTags);
            newTags.putAll(tags);

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

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> bulkDataPointTagOperation(
            @ApiParam(value = "Expiration in seconds of temporary resource after it completes",
                defaultValue = "" + TemporaryResourceManager.DEFAULT_EXPIRATION_SECONDS,
                required = false,
                allowMultiple = false)
            @RequestParam(required=false) Integer expiration,
            
            @RequestBody
            TagBulkRequest requestBody,
            
            @AuthenticationPrincipal
            User user,
            
            UriComponentsBuilder builder) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<TagIndividualRequest> requests = requestBody.getRequests();

        if (expiration == null) {
            expiration = TemporaryResourceManager.DEFAULT_EXPIRATION_SECONDS;
        }
        
        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (expiration < 0) {
            throw new BadRequestException(new TranslatableMessage("rest.error.expirationMustBeGreaterThanZero"));
        }
        
        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> responseBody = bulkTagsTemporaryResourceManager.executeAsHighPriorityTask(user.getId(), expiration, (resource) -> {
            TagBulkResponse bulkResponse = new TagBulkResponse();
            int i = 0;
            
            if (!bulkTagsTemporaryResourceManager.progress(resource, bulkResponse, i++, requests.size())) {
                // can't update progress, most likely cancelled or timed out
                return;
            }

            for (TagIndividualRequest request : requests) {
                TagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, user);
                bulkResponse.addResponse(individualResponse);

                if (!bulkTagsTemporaryResourceManager.progress(resource, bulkResponse, i++, requests.size())) {
                    // can't update progress, most likely cancelled or timed out
                    return;
                }
            }

            bulkTagsTemporaryResourceManager.success(resource, bulkResponse);
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/v2/data-point-tags/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a list of current bulk tag operations", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public List<TemporaryResource<TagBulkResponse, AbstractRestV2Exception>> getBulkDataPointTagOperations(
            @AuthenticationPrincipal
            User user) {
        
        return this.bulkTagsTemporaryResourceManager.list().stream()
                .filter((tr) -> user.isAdmin() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());
    }
    
    @ApiOperation(value = "Get the status of a bulk tag operation using its id", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }
        
        return resource;
    }
    
    @ApiOperation(value = "Cancel a bulk tag operation using its id",
            notes = "Only cancels if the operation is not already complete." +
                    "May also be used to remove a completed temporary resource by passing remove=true, otherwise the resource is removed when it expires." +
                    "User can only cancel their own bulk tag operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public TemporaryResource<TagBulkResponse, AbstractRestV2Exception> cancelBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @ApiParam(value = "Remove the temporary resource", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required=false, defaultValue = "false") boolean remove,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<TagBulkResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
        if (!user.isAdmin() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }
        
        bulkTagsTemporaryResourceManager.cancel(resource);
        if (remove) {
            bulkTagsTemporaryResourceManager.remove(resource);
        }
        
        return resource;
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
