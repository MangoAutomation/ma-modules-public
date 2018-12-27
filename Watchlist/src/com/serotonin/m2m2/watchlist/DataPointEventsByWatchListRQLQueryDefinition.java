/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.watchlist;

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
import com.serotonin.m2m2.rt.event.type.EventType.EventTypeNames;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.WatchListRestController;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;
import net.jazdw.rql.parser.RQLParserException;

/**
 *
 * @author Terry Packer
 */
public class DataPointEventsByWatchListRQLQueryDefinition extends ModuleQueryDefinition {

    public static final String QUERY_TYPE_NAME = "DATA_POINT_EVENTS_BY_WATCHLIST_RQL";

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
        return null; //Don't have any permissions for this query
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getTableName()
     */
    @Override
    public String getTableName() {
        return SchemaDefinition.EVENTS_TABLE;
    }
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#validateImpl(com.fasterxml.jackson.databind.JsonNode)
     */
    @Override
    protected void validateImpl(final User user, final JsonNode parameters, final ProcessResult result) {
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
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#createQuery(com.fasterxml.jackson.databind.JsonNode)
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

        List<Object> args = new ArrayList<>();
        args.add("typeRef1");

        //Find all watchlists that match the RQL
        WatchListDao.getInstance().rqlQuery(rqlAstNode, new MappedRowCallback<WatchListVO>() {
            @Override
            public void row(WatchListVO vo, int index) {

                if(WatchListRestController.hasReadPermission(user, vo)) {
                    WatchListDao.getInstance().getPoints(vo.getId(), new MappedRowCallback<DataPointVO>(){
                        @Override
                        public void row(DataPointVO dp, int index) {
                            if(Permissions.hasDataPointReadPermission(user, dp)){
                                args.add(Integer.toString(dp.getId()));
                            }
                        }
                    });
                }
            }
        });

        if(args.size() > 1) {
            //Create Event Query for these Points
            ASTNode query = new ASTNode("in", args);
            query = addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
            query = addAndRestriction(query, new ASTNode("eq", "typeName", EventTypeNames.DATA_POINT));

            return query;
        }else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.ModuleQueryDefinition#getExplainInfo()
     */
    @Override
    public JsonNode getExplainInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("rql", new ParameterInfo("String", true, null, new TranslatableMessage("common.default", "RQL query for WatchLists to return events for")));
        return JsonNodeFactory.instance.pojoNode(info);
    }
}
