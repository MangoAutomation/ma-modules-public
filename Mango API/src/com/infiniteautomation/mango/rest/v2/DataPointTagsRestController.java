/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
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

    private TemporaryResourceManager<BulkOperationResponse, AbstractRestV2Exception> bulkTagsTemporaryResourceManager = new TemporaryResourceManager<BulkOperationResponse, AbstractRestV2Exception>() {
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
    
    public static enum BulkOperationAction {
        GET, SET, MERGE
    }
    
    public static class BulkOperationRequest {
        BulkOperationAction action;
        List<String> xids;
        Map<String, String> tags;

        public BulkOperationAction getAction() {
            return action;
        }
        public void setAction(BulkOperationAction action) {
            this.action = action;
        }
        public List<String> getXids() {
            return xids;
        }
        public void setXids(List<String> xids) {
            this.xids = xids;
        }
        public Map<String, String> getTags() {
            return tags;
        }
        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }
    }
    
    public static class BulkOperationIndividualResponse {
        String xid;
        int httpStatus = HttpStatus.OK.value();
        Object body;
        
        public BulkOperationIndividualResponse() {
        }
        public BulkOperationIndividualResponse(String xid) {
            this.xid = xid;
        }
        public String getXid() {
            return xid;
        }
        public void setXid(String xid) {
            this.xid = xid;
        }
        public int getHttpStatus() {
            return httpStatus;
        }
        public void setHttpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
        }
        public Object getBody() {
            return body;
        }
        public void setBody(Object body) {
            this.body = body;
        }
    }
    
    public static class BulkOperationResponse {
        boolean hasError;
        List<BulkOperationIndividualResponse> results;
        
        public BulkOperationResponse() {
        }
        
        public BulkOperationResponse(boolean hasError, List<BulkOperationIndividualResponse> results) {
            this.hasError = hasError;
            this.results = results;
        }

        public boolean isHasError() {
            return hasError;
        }

        public void setHasError(boolean hasError) {
            this.hasError = hasError;
        }

        public List<BulkOperationIndividualResponse> getResults() {
            return results;
        }

        public void setResults(List<BulkOperationIndividualResponse> results) {
            this.results = results;
        }
    }
    
    @ApiOperation(value = "Synchronously bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk-sync")
    public BulkOperationResponse bulkDataPointTagOperationSync(
            @RequestBody
            BulkOperationRequest requestBody,
            
            @AuthenticationPrincipal
            User user) {

        BulkOperationAction action = requestBody.getAction();
        List<String> xids = requestBody.getXids();
        Map<String, String> tags = requestBody.getTags();
        
        if (action == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
        } else if (xids == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xids"));
        } else if (tags == null && (action == BulkOperationAction.SET || action == BulkOperationAction.MERGE)) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "tags"));
        }

        List<BulkOperationIndividualResponse> results = new ArrayList<>(xids.size());
        boolean hasError = false;

        for (String xid : xids) {
            BulkOperationIndividualResponse xidResult = new BulkOperationIndividualResponse(xid);

            try {
                switch (action) {
                    case GET:
                        xidResult.setBody(this.getTagsForDataPoint(xid, user));
                        break;
                    case SET:
                        xidResult.setBody(this.setTagsForDataPoint(xid, tags, user));
                        break;
                    case MERGE:
                        xidResult.setBody(this.addTagsForDataPoint(xid, tags, user));
                        break;
                }
            } catch (Exception e) {
                AbstractRestV2Exception exception = exceptionToRestException(e);
                hasError = true;
                xidResult.setBody(exception);
                xidResult.setHttpStatus(exception.getStatus().value());
            }
            
            results.add(xidResult);
        }

        return new BulkOperationResponse(hasError, results);
    }

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<BulkOperationResponse, AbstractRestV2Exception>> bulkDataPointTagOperation(
            @ApiParam(value = "Expiration in seconds of temporary resource after it completes", defaultValue = "" + TEMPORARY_RESOURCE_EXPIRATION_SECONDS, required = false, allowMultiple = false)
            @RequestParam(required=false) Integer expiration,
            
            @RequestBody
            BulkOperationRequest requestBody,
            
            @AuthenticationPrincipal
            User user,
            
            UriComponentsBuilder builder) {

        BulkOperationAction action = requestBody.getAction();
        List<String> xids = requestBody.getXids();
        Map<String, String> tags = requestBody.getTags();

        if (expiration == null) {
            expiration = TEMPORARY_RESOURCE_EXPIRATION_SECONDS;
        }
        
        if (action == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
        } else if (xids == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xids"));
        } else if (tags == null && (action == BulkOperationAction.SET || action == BulkOperationAction.MERGE)) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "tags"));
        } else if (expiration < 0) {
            throw new BadRequestException(new TranslatableMessage("rest.error.expirationMustBeGreaterThanZero"));
        }
        
        TemporaryResource<BulkOperationResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.executeAsHighPriorityTask(user.getId(), expiration, (r) -> {
            // most likely cancelled or timed out
            if (!bulkTagsTemporaryResourceManager.progress(r, null, 0, xids.size())) {
                return;
            }
            
            List<BulkOperationIndividualResponse> results = new ArrayList<>(xids.size());
            boolean hasError = false;
            
            int i = 0;
            for (String xid : xids) {
                BulkOperationIndividualResponse xidResult = new BulkOperationIndividualResponse(xid);

                try {
                    switch (action) {
                        case GET:
                            xidResult.setBody(this.getTagsForDataPoint(xid, user));
                            break;
                        case SET:
                            xidResult.setBody(this.setTagsForDataPoint(xid, tags, user));
                            break;
                        case MERGE:
                            xidResult.setBody(this.addTagsForDataPoint(xid, tags, user));
                            break;
                    }
                } catch (Exception e) {
                    AbstractRestV2Exception exception = exceptionToRestException(e);
                    hasError = true;
                    xidResult.setBody(exception);
                    xidResult.setHttpStatus(exception.getStatus().value());
                }
                
                results.add(xidResult);

                // most likely cancelled or timed out
                if (!bulkTagsTemporaryResourceManager.progress(r, null, ++i, xids.size())) {
                    return;
                }
            }

            BulkOperationResponse result = new BulkOperationResponse(hasError, results);
            bulkTagsTemporaryResourceManager.success(r, result);
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/v2/data-point-tags/bulk/{id}").buildAndExpand(resource.getId()).toUri());
        return new ResponseEntity<TemporaryResource<BulkOperationResponse, AbstractRestV2Exception>>(resource, headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a list of current bulk tag operations", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public List<TemporaryResource<BulkOperationResponse, AbstractRestV2Exception>> getBulkDataPointTagOperations(
            @AuthenticationPrincipal
            User user) {
        
        return this.bulkTagsTemporaryResourceManager.list().stream()
                .filter((tr) -> user.isAdmin() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());
    }
    
    @ApiOperation(value = "Get the status of a bulk tag operation using its id", notes = "User can only get their own bulk tag operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<BulkOperationResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<BulkOperationResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
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
    public TemporaryResource<BulkOperationResponse, AbstractRestV2Exception> cancelBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,
            
            @ApiParam(value = "Remove the temporary resource", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required=false, defaultValue = "false") boolean remove,
            
            @AuthenticationPrincipal
            User user) {
        
        TemporaryResource<BulkOperationResponse, AbstractRestV2Exception> resource = bulkTagsTemporaryResourceManager.get(id);
        
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
