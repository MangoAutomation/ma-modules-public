/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.QueryAttribute;
import com.infiniteautomation.mango.db.query.TableModel;
import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

/**
 * REST Endpoints for User Event Cache access, All un-acknowledged user events are held in the cache 
 * on a per user basis.
 * 
 * Note that querying an object is different to querying the database.  If a property is queried on 
 * that does not exist in a given object, it will not fail and simply not match that criteria.  Since 
 * the list of user events contains various event types, each item in the list can have different properties.
 *
 * @author Terry Packer
 */
@Api(value="User Events", description="User events are all un-acknowledged events for a user")
@RestController()
@RequestMapping("/user-events")
public class UserEventsV2Controller extends AbstractMangoRestV2Controller{

    
    @ApiOperation(
            value = "Query User Events",
            notes = "Query via rql in url against events for the current user",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "")
    public ResponseEntity<PageQueryResultModel<EventInstanceModel>> query(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        //Parse the RQL Query
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        List<EventInstance> results;
        List<EventInstance> events = Common.eventManager.getAllActiveUserEvents(user.getId());
        if(query != null)
            results = query.accept(new RQLToObjectListQuery<EventInstance>(), events);
        else
            results = events;
        List<EventInstanceModel> models = new ArrayList<>();
        //Convert to models
        for(EventInstance event : results) {
            models.add(new EventInstanceModel(event));
        }
        
        //Query the models
        return new ResponseEntity<>(new PageQueryResultModel<>(models, events.size()), HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Get Explaination For Query",
            notes = "What is Query-able on this model"
            )
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 403, message = "User does not have access")
        })
    @RequestMapping(method = RequestMethod.GET, value = "/explain-query")
    public ResponseEntity<TableModel> getTableModel(HttpServletRequest request) {
        Map<String, QueryAttribute> attributeMap = new HashMap<>();
        
        Field[] inherited = EventInstance.class.getFields();
        for(Field field : inherited) {
            QueryAttribute qa = new QueryAttribute(field.getName(), null, getJdbcTypeCode(field.getGenericType().getTypeName()));
            attributeMap.put(field.getName(), qa);
        }
        Field[] all = EventInstance.class.getDeclaredFields();
        for(Field field : all) {
            QueryAttribute qa = new QueryAttribute(field.getName(), null, getJdbcTypeCode(field.getGenericType().getTypeName()));
            attributeMap.put(field.getName(), qa);
        }
        
        return new ResponseEntity<>(new TableModel("EventInstance.class", new ArrayList<>(attributeMap.values())), HttpStatus.OK);
    }

    /**
     * @param typeName
     * @return
     */
    private int getJdbcTypeCode(String typeName) {
        switch(typeName) {
            case "int":
            case "Integer":
                return Types.INTEGER;
            case "double":
            case "Double":
                return Types.DOUBLE;
            case "boolean":
            case "Boolean":
                return Types.BOOLEAN;
            case "String":
                return Types.VARCHAR;
            case "long":
            case "Long":
                return Types.BIGINT;
            default: 
                if(typeName.startsWith("com") || typeName.startsWith("java"))
                    return Types.JAVA_OBJECT;
                else
                    return Types.OTHER;
        }
    }
    
    
}
