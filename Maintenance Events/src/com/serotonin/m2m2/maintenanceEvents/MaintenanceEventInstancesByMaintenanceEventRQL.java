/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.AbstractBasicDao;
import com.serotonin.m2m2.db.dao.SchemaDefinition;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleQueryDefinition;
import com.serotonin.m2m2.vo.User;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;
import net.jazdw.rql.parser.RQLParserException;

/**
 * Make this an RQL on the maint events table
 *
 * @author Terry Packer
 */
public class MaintenanceEventInstancesByMaintenanceEventRQL extends ModuleQueryDefinition {

    public static final String QUERY_TYPE_NAME = "MAINTENANCE_EVENTS_BY_MAINTENANCE_EVENT_RQL";

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
        if(parameters.get("rql") == null)
            result.addContextualMessage("rql", "validate.required");
        else {
            try {
                JsonNode rqlNode = parameters.get("rql");
                ObjectReader reader = this.readerFor(String.class);
                String rql = reader.readValue(rqlNode);
                if (rql != null && !rql.isEmpty()) {
                    RQLParser parser = new RQLParser();
                    parser.parse(rql);
                }
            }catch(IOException | RQLParserException | IllegalArgumentException e) {
                result.addContextualMessage("rql", "validate.invalidValue");
            }
        }
        if(parameters.has("limit")) {
            if(!parameters.get("limit").canConvertToInt())
                result.addContextualMessage("limit", "validate.invalidValue");
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

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getExplainInfo()
     */
    @Override
    public JsonNode getExplainInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("rql", new ParameterInfo("String", true, null, new TranslatableMessage("common.default", "RQL query against maintenance events table")));
        info.put("limit", new ParameterInfo("Integer", false, AbstractBasicDao.DEFAULT_LIMIT, new TranslatableMessage("common.default", "limit number of events returned")));
        info.put("order", new ParameterInfo("String", false, "desc", new TranslatableMessage("common.default", "time order of returned events (asc,desc)")));
        info.put("active", new ParameterInfo("Boolean", false, null, new TranslatableMessage("common.default", "should active, inactive or all events be returned")));
        return JsonNodeFactory.instance.pojoNode(info);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#createQuery(com.serotonin.m2m2.vo.User, com.fasterxml.jackson.databind.JsonNode)
     */
    @Override
    public ASTNode createQuery(User user, JsonNode parameters) throws IOException {
        JsonNode rqlNode = parameters.get("rql");
        ObjectReader reader = this.readerFor(String.class);
        String rql = reader.readValue(rqlNode);

        ASTNode rqlAstNode;
        if (rql == null || rql.isEmpty()) {
            rqlAstNode = new ASTNode("limit", AbstractBasicDao.DEFAULT_LIMIT);
        }

        RQLParser parser = new RQLParser();
        try {
            rqlAstNode = parser.parse(rql);
        } catch (RQLParserException | IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }

        //Lookup data points by tag
        List<Object> args = new ArrayList<>();
        args.add("typeRef1");
        MaintenanceEventDao.getInstance().rqlQuery(rqlAstNode, new MappedRowCallback<MaintenanceEventVO>() {
            @Override
            public void row(MaintenanceEventVO vo, int index) {
                args.add(Integer.toString(vo.getId()));
            }
        });

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
