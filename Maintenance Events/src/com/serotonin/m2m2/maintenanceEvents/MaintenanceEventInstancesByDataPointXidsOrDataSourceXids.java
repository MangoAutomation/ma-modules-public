/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.db.dao.SchemaDefinition;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleQueryDefinition;
import com.serotonin.m2m2.vo.User;

import net.jazdw.rql.parser.ASTNode;

/**
 * Make this an RQL on the maint events table
 *
 * @author Terry Packer
 */
public class MaintenanceEventInstancesByDataPointXidsOrDataSourceXids extends ModuleQueryDefinition {

    public static final String QUERY_TYPE_NAME = "MAINTENANCE_EVENTS_BY_DATA_POINT_XIDS_OR_DATA_SOURCE_XIDS";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getQueryTypeName()
     */
    @Override
    public String getQueryTypeName() {
        return QUERY_TYPE_NAME;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getPermissionTypeName()
     */
    @Override
    protected String getPermissionTypeName() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getTableName()
     */
    @Override
    public String getTableName() {
        return SchemaDefinition.EVENTS_TABLE;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#validateImpl(com.serotonin.m2m2.vo.User, com.fasterxml.jackson.databind.JsonNode, com.infiniteautomation.mango.rest.v2.model.RestValidationResult)
     */
    @Override
    protected void validateImpl(User user, JsonNode parameters, ProcessResult result) {
        
        if(parameters.get("dataPointXids") == null && parameters.get("dataSourceXids") == null) {
            result.addContextualMessage("dataPointXids", "validate.required");
            result.addContextualMessage("dataSourceXids", "validate.required");
        }
        if(parameters.get("dataPointXids") != null) {
            if(!parameters.get("dataPointXids").isArray())
                result.addContextualMessage("dataPointXids", "validate.invalidValue");
        }
        if(parameters.get("dataSourceXids") != null) {
            if(!parameters.get("dataSourceXids").isArray())
                result.addContextualMessage("dataSourceXids", "validate.invalidValue");
        }
        if(parameters.has("limit")) {
            if(!parameters.get("limit").canConvertToInt())
                result.addContextualMessage("limit", "validate.nvalidValue");
        }
        if(parameters.has("order")) {
            String order = parameters.get("order").asText();
            if(!"asc".equals(order) && !"desc".equals(order))
                result.addContextualMessage("order", "validate.invalidValue");
        }
        if(parameters.has("active")) {
            if(!parameters.get("active").isBoolean())
                result.addContextualMessage("active", "validate.invalidValue");
        }
    }

    @Override
    public JsonNode getExplainInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("dataSourceXids", new ParameterInfo("Array", false, null, new TranslatableMessage("common.default", "xids of data sources linked to maintenance events, one of points or sources required")));
        info.put("dataPointXids", new ParameterInfo("Array", false, null, new TranslatableMessage("common.default", "xids of data points linked to maintenance events, one of points or sources required")));
        info.put("limit", new ParameterInfo("Integer", false, AbstractBasicDao.DEFAULT_LIMIT, new TranslatableMessage("common.default", "limit number of events")));
        info.put("order", new ParameterInfo("String", false, "desc", new TranslatableMessage("common.default", "time order of returned events (asc,desc)")));
        info.put("active", new ParameterInfo("Boolean", false, null, new TranslatableMessage("common.default", "should active, inactive or all events be returned")));
        return JsonNodeFactory.instance.pojoNode(info);
    }

    @Override
    public ASTNode createQuery(User user, JsonNode parameters) throws IOException {
        
        List<String> dataSourceXids = new ArrayList<>();
        if(parameters.has("dataSourceXids")) {
            JsonNode dataSourceXidsNode = parameters.get("dataSourceXids");
            dataSourceXidsNode.forEach((e) ->{
                dataSourceXids.add(e.asText());
            });
        }
        List<String> dataPointXids = new ArrayList<>();
        if(parameters.has("dataPointXids")) {
            JsonNode dataPointXidsNode = parameters.get("dataPointXids");
            dataPointXidsNode.forEach((e) ->{
                dataPointXids.add(e.asText());
            });
        }

        Set<Integer> ids = new HashSet<>();
        MappedRowCallback<MaintenanceEventVO> callback = new MappedRowCallback<MaintenanceEventVO>() {
            @Override
            public void row(MaintenanceEventVO vo, int index) {
               ids.add(vo.getId());
            }
        };
        
        for(String xid : dataPointXids) {
            MaintenanceEventDao.getInstance().getForDataPoint(xid, callback);
        }
        
        for(String xid : dataSourceXids) {
            MaintenanceEventDao.getInstance().getForDataSource(xid, callback);
        }

        List<Object> args = new ArrayList<>();
        args.add("typeRef1");
        for(Integer id : ids)
            args.add(Integer.toString(id));
        
        //Create Event Query for these Points
        if(args.size() > 1) {
            ASTNode query = new ASTNode("in", args);
            query = addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
            query = addAndRestriction(query, new ASTNode("eq", "typeName", MaintenanceEventType.TYPE_NAME));
            
            if(parameters.has("active")) {
                query = addAndRestriction(query, new ASTNode("eq", "active", parameters.get("active").asBoolean()));
            }
            if(parameters.has("order")) {
                String order = parameters.get("order").asText();
                if("asc".equals(order))
                    query = addAndRestriction(query, new ASTNode("sort","+activeTs"));
                else
                    query = addAndRestriction(query, new ASTNode("sort","-activeTs"));
            }
            if (parameters.has("limit"))
                query = addAndRestriction(query, new ASTNode("limit", parameters.get("limit").asInt()));
            else
                query = addAndRestriction(query, new ASTNode("limit", AbstractBasicDao.DEFAULT_LIMIT));

            return query;
        }else {
            return null;
        }
    }

}
