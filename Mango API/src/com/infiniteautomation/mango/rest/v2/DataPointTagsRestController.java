/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.ConditionSortLimitWithTagKeys;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@RestController()
@RequestMapping("/v2/data-point-tags")
public class DataPointTagsRestController {
    
    @RequestMapping(method = RequestMethod.GET, value="/point/{xid}")
    public Map<String, String> getTagsForDataPoint(@PathVariable String xid) {
        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        return DataPointTagsDao.instance.getTagsForDataPointId(dataPoint.getId());
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
        
        ASTNode rql = BaseMangoRestController.parseRQLtoAST(queryString);
        return DataPointTagsDao.instance.getTagValuesForKey(tagKey, rql, user);
    }

    @RequestMapping(method = RequestMethod.GET, value="/stream-query")
    public StreamedArrayWithTotal getDataPointsForTagsStreamed(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        ASTNode rql = BaseMangoRestController.parseRQLtoAST(request.getQueryString());

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
                // this technically should be accounted for via SQL restrictions added by DataPointDao.userHasPermission()
                // just a double check
                return dataPointFilter.hasDataPointReadPermission(item);
            }, item -> {
                DataPointDao.instance.loadPartialRelationalData(item);
                return new DataPointModel(item);
            });
        }
    }
}
