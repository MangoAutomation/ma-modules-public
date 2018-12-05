/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.ComparisonEnum;
import com.infiniteautomation.mango.db.query.QueryComparison;
import com.infiniteautomation.mango.db.query.pojo.RQLToPagedObjectListQuery;
import com.infiniteautomation.mango.rest.v2.model.realtime.RealTimeDataPointValueModel;
import com.infiniteautomation.mango.rest.v2.model.realtime.RealTimeQueryWithTotal;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.RealTimeDataPointValue;
import com.serotonin.m2m2.rt.dataImage.RealTimeDataPointValueCache;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Api(value="Access to current values combined with data point information.")
@RestController(value="RealTimeDataRestControllerV2")
@RequestMapping("/realtime")
public class RealTimeDataRestController {

    
    /**
     * Query the User's Real Time Data
     * @param request
     * @param limit
     * @return
     */
    @ApiOperation(value = "Query realtime values", 
                  notes = "Check the status member to ensure the point is OK not DISABLED or UNRELIABLE")
    @RequestMapping(method = RequestMethod.GET)
    public RealTimeQueryWithTotal query(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        List<RealTimeDataPointValue> values = RealTimeDataPointValueCache.instance.getUserView(user);
        List<RealTimeDataPointValueModel> models = new ArrayList<>();
        UriComponentsBuilder imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/{ts}_{id}.jpg");
        
        for(RealTimeDataPointValue value : values){
            if(value.getDataTypeId() == DataTypes.IMAGE){
                models.add(new RealTimeDataPointValueModel(value, imageServletBuilder.buildAndExpand(value.getTimestamp(), value.getDataPointId()).toUri()));
            }else{
                models.add(new RealTimeDataPointValueModel(value, value.getValue()));
            }
        }

        RealTimePagedQuery filter = new RealTimePagedQuery();
        models = query.accept(filter, models);
        
        return new RealTimeQueryWithTotal(filter.getUnlimitedSize(), models);
    }
    
    private class RealTimePagedQuery extends RQLToPagedObjectListQuery<RealTimeDataPointValueModel> {

        public RealTimePagedQuery() {
            super();
        }
        
        @Override
        protected QueryComparison createComparison(String field, ComparisonEnum comparison,
                List<Object> args) {
            if(comparison == ComparisonEnum.IN && field.startsWith("tags")) {
                return new DataPointTagsComparison(field, comparison, args);
            }else
                return super.createComparison(field, comparison, args);
        }
    }
    
    private class DataPointTagsComparison extends QueryComparison {
        public DataPointTagsComparison(String field, ComparisonEnum comparison, List<Object> args) {
            super(field, comparison, args);
        }
        
        @Override
        public boolean apply(Object instance){
            
            try {
                Object value = null;
                //Check to see if we are nested
                if(attribute.contains(".")) {
                    String[] attributes = attribute.split("\\.");
                    value = instance;
                    for(String attr : attributes) {
                        if("name".equals(attr))
                            value = PropertyUtils.getProperty(instance, "name");
                        else if("device".equals(attr))
                            value = PropertyUtils.getProperty(instance, "deviceName");
                        else
                            value = PropertyUtils.getProperty(value, attr);
                    }
                }else {
                    value = PropertyUtils.getProperty(instance, attribute);
                }
                return this.compare(value);
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                return false;
            }
            
        }
    }
}
