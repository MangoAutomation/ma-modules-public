/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
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

    @ApiOperation(value = "Add data point tags by data point XID", notes = "User must have edit permission for the data point's data source")
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
