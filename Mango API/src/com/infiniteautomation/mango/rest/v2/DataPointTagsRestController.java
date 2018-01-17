/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
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
        int httpStatus;
        Object body;
        
        public BulkOperationIndividualResponse() {
        }
        
        public BulkOperationIndividualResponse(String xid, int httpStatus, Object body) {
            this.xid = xid;
            this.httpStatus = httpStatus;
            this.body = body;
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

    @ApiOperation(value = "Bulk get/set/add data point tags for a list of XIDs", notes = "User must have read/edit permission for the data point")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<BulkOperationResponse> bulkDataPointTagOperation(
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
        boolean hasError = true;
        
        for (String xid : xids) {
            Object result = null;
            HttpStatus httpStatus = HttpStatus.OK;
            
            // TODO the exceptions and status codes are taken from MangoSpringExceptionHandler
            // we should make it easier to reuse the logic from that class elsewhere
            try {
                switch (action) {
                    case GET:
                        result = this.getTagsForDataPoint(xid, user);
                        break;
                    case SET:
                        result = this.setTagsForDataPoint(xid, tags, user);
                        break;
                    case MERGE:
                        result = this.addTagsForDataPoint(xid, tags, user);
                        break;
                }
                hasError = false;
            } catch(AbstractRestV2Exception e) {
                httpStatus = e.getStatus();
                result = e;
            } catch (PermissionException e) {
                httpStatus = HttpStatus.FORBIDDEN;
                result = new AccessDeniedException(e.getTranslatableMessage(), e);
            } catch (org.springframework.security.access.AccessDeniedException e) {
                httpStatus = HttpStatus.FORBIDDEN;
                result = new AccessDeniedException(e);
            } catch (ValidationException e) {
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
                result = new ValidationFailedRestException(e.getValidationResult());
            } catch (NotFoundException | ResourceNotFoundException e) {
                httpStatus = HttpStatus.NOT_FOUND;
                result = new NotFoundRestException(e);
            } catch (Exception e) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                result = new ServerErrorException(e);
            }
            
            results.add(new BulkOperationIndividualResponse(xid, httpStatus.value(), result));
        }
        
        return new ResponseEntity<BulkOperationResponse>(new BulkOperationResponse(hasError, results), HttpStatus.MULTI_STATUS);
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
