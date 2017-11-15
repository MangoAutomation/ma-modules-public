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
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@RestController()
@RequestMapping("/v2/data-point-tags")
public class DataPointTagsRestController extends BaseMangoRestController {
    
    @RequestMapping(method = RequestMethod.GET, value="/point/{xid}")
    public Map<String, String> getTagsForDataPoint(@PathVariable String xid) {
        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        return DataPointTagsDao.instance.getTagsForDataPointId(dataPoint.getId());
    }

    @RequestMapping(method = RequestMethod.POST, value="/point/{xid}")
    public Map<String, String> setTagsForDataPoint(
            @PathVariable String xid,
            @RequestBody(required=true) Map<String, String> tags) {

        return DataPointTagsDao.instance.doInTransaction(txStatus -> {
            DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
            if (dataPoint == null) {
                throw new NotFoundRestException();
            }

            // we set the tags on the data point then retrieve them so that the device and name tags are correct
            dataPoint.setTags(tags);

            Map<String, String> updatedTags = dataPoint.getTags();
            DataPointTagsDao.instance.setTagsForDataPointId(dataPoint.getId(), updatedTags);

            DataPointRT rt = Common.runtimeManager.getDataPoint(dataPoint.getId());
            if (rt != null) {
                DataPointVO rtVo = rt.getVO();
                rtVo.setTags(tags);
            }
            
            return updatedTags;
        });
    }
    
    @RequestMapping(method = RequestMethod.PUT, value="/point/{xid}")
    public Map<String, String> addTagsForDataPoint(
            @PathVariable String xid,
            @RequestBody(required=true) Map<String, String> tags) {

        return DataPointTagsDao.instance.doInTransaction(txStatus -> {
            DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
            if (dataPoint == null) {
                throw new NotFoundRestException();
            }
            
            Map<String, String> existingTags = DataPointTagsDao.instance.getTagsForDataPointId(dataPoint.getId());
            Map<String, String> newTags = new HashMap<>();
            newTags.putAll(existingTags);
            newTags.putAll(tags);

            // we set the tags on the data point then retrieve them so that the device and name tags are correct
            dataPoint.setTags(newTags);

            Map<String, String> updatedTags = dataPoint.getTags();
            DataPointTagsDao.instance.setTagsForDataPointId(dataPoint.getId(), updatedTags);
            
            DataPointRT rt = Common.runtimeManager.getDataPoint(dataPoint.getId());
            if (rt != null) {
                DataPointVO rtVo = rt.getVO();
                rtVo.setTags(tags);
            }
            
            return updatedTags;
        });
    }

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
    @RequestMapping(method = RequestMethod.GET, value="/values/{tagKey}")
    public Set<String> getTagValuesForKey(
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
