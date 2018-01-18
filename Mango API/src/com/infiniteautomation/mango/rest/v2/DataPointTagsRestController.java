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

import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.IndividualResponse;
import com.infiniteautomation.mango.rest.v2.temporaryResource.BulkRequest;
import com.infiniteautomation.mango.rest.v2.temporaryResource.BulkResponse;
import com.infiniteautomation.mango.rest.v2.temporaryResource.IndividualRequest;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.exception.NotFoundException;
import com.serotonin.m2m2.vo.exception.ValidationException;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
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
    
    public static final int TEMPORARY_RESOURCE_EXPIRATION_SECONDS = 300;

    // TODO the exceptions and status codes are taken from MangoSpringExceptionHandler
    // we should make it easier to reuse the logic from that class elsewhere
    public static AbstractRestV2Exception exceptionToRestException(Exception e) {
        if (e instanceof AbstractRestV2Exception) {
            return (AbstractRestV2Exception) e;
        } else if (e instanceof PermissionException) {
            PermissionException exception = (PermissionException) e;
            return new AccessDeniedException(exception.getTranslatableMessage(), exception);
        } else if (e instanceof org.springframework.security.access.AccessDeniedException) {
            return new AccessDeniedException(e);
        } else if (e instanceof ValidationException) {
            ValidationException exception = (ValidationException) e;
            return new ValidationFailedRestException(exception.getValidationResult());
        } else if (e instanceof NotFoundException || e instanceof ResourceNotFoundException) {
            throw new NotFoundRestException(e);
        } else {
            return new ServerErrorException(e);
        }
    }
    
    public static enum BulkTagAction {
        GET, SET, MERGE
    }

    public static class BulkTagRequest extends BulkRequest<BulkTagAction, String, Map<String, String>> {
    }
    
    public static class BulkTagIndividualResponse extends IndividualResponse<String, Map<String, String>, AbstractRestV2Exception> {
    }
    
    public static class BulkTagResponse extends BulkResponse<BulkTagIndividualResponse> {
        public BulkTagResponse(int size) {
            super(size);
        }
    }

    private TemporaryResourceManager<BulkTagResponse, AbstractRestV2Exception> bulkTagsTemporaryResourceManager = new TemporaryResourceManager<BulkTagResponse, AbstractRestV2Exception>() {
        @Override
        public AbstractRestV2Exception exceptionToError(Exception e) {
            return exceptionToRestException(e);
        }
    };
    
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
    
    private BulkTagIndividualResponse doIndividualRequest(IndividualRequest<BulkTagAction, String, Map<String, String>> request, BulkTagAction defaultAction, Map<String, String> defaultBody, User user) {
        BulkTagIndividualResponse result = new BulkTagIndividualResponse();
        
        try {
            String xid = request.getId();
            if (xid == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
            }
            result.setId(xid);

            BulkTagAction action = request.getAction() == null ? defaultAction : request.getAction();
            Map<String, String> tags = request.getBody() == null ? defaultBody : request.getBody();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }

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
            AbstractRestV2Exception exception = exceptionToRestException(e);
            result.setError(exception);
            result.setHttpStatus(exception.getStatus().value());
        }
        
        return result;
    }
    
    @ApiOperation(value = "Synchronously bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk-sync")
    public BulkTagResponse bulkDataPointTagOperationSync(
            @RequestBody
            BulkTagRequest requestBody,
            
            @AuthenticationPrincipal
            User user) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<IndividualRequest<BulkTagAction, String, Map<String, String>>> requests = requestBody.getRequests();
        
        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        }

        BulkTagResponse response = new BulkTagResponse(requests.size());
        for (IndividualRequest<BulkTagAction, String, Map<String, String>> request : requests) {
            BulkTagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, user);
            response.addResult(individualResponse);
        }

        return response;
    }

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<BulkTagResponse, AbstractRestV2Exception>> bulkDataPointTagOperation(
            @ApiParam(value = "Expiration in seconds of temporary resource after it completes", defaultValue = "" + TEMPORARY_RESOURCE_EXPIRATION_SECONDS, required = false, allowMultiple = false)
            @RequestParam(required=false) Integer expiration,
            
            @RequestBody
            BulkTagRequest requestBody,
            
            @AuthenticationPrincipal
            User user,
            
            UriComponentsBuilder builder) {

        BulkTagAction defaultAction = requestBody.getAction();
        Map<String, String> defaultBody = requestBody.getBody();
        List<IndividualRequest<BulkTagAction, String, Map<String, String>>> requests = requestBody.getRequests();

        if (expiration == null) {
            expiration = TEMPORARY_RESOURCE_EXPIRATION_SECONDS;
        }
        
        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (expiration < 0) {
            throw new BadRequestException(new TranslatableMessage("rest.error.expirationMustBeGreaterThanZero"));
        }
        
        TemporaryResource<BulkTagResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.executeAsHighPriorityTask(user.getId(), expiration, (r) -> {
            if (!bulkTagsTemporaryResourceManager.progress(r, null, 0, requests.size())) {
                // most likely cancelled or timed out
                return;
            }

            int i = 0;
            BulkTagResponse response = new BulkTagResponse(requests.size());
            for (IndividualRequest<BulkTagAction, String, Map<String, String>> request : requests) {
                BulkTagIndividualResponse individualResponse = doIndividualRequest(request, defaultAction, defaultBody, user);
                response.addResult(individualResponse);

                if (!bulkTagsTemporaryResourceManager.progress(r, null, ++i, requests.size())) {
                    // most likely cancelled or timed out
                    return;
                }
            }

            bulkTagsTemporaryResourceManager.success(r, response);
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/v2/data-point-tags/bulk/{id}").buildAndExpand(resource.getId()).toUri());
        return new ResponseEntity<TemporaryResource<BulkTagResponse, AbstractRestV2Exception>>(resource, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a list of current bulk tag operations", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public List<TemporaryResource<BulkTagResponse, AbstractRestV2Exception>> getBulkDataPointTagOperations(
            @AuthenticationPrincipal
            User user) {
        
        return this.bulkTagsTemporaryResourceManager.list().stream()
                .filter((tr) -> user.isAdmin() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());
    }
    
    @ApiOperation(value = "Get the status of a bulk tag operation using its id", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<BulkTagResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<BulkTagResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
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
    public TemporaryResource<BulkTagResponse, AbstractRestV2Exception> cancelBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @ApiParam(value = "Remove the temporary resource", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required=false, defaultValue = "false") boolean remove,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<BulkTagResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
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
