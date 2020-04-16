/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.v2.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.RoleDao;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.vo.role.RoleVO;

/**
 * TODO AutoWire in RoleDao
 * @author Terry Packer
 */
public class MangoPermissionModelDeserializer extends StdDeserializer<MangoPermissionModel>{

    private static final long serialVersionUID = 1L;
    private final RoleDao dao;

    protected MangoPermissionModelDeserializer() {
        super(MangoPermissionModel.class);
        this.dao = RoleDao.getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MangoPermissionModel deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode tree = jp.readValueAsTree();

        Set<Set<Role>> roles = new HashSet<>();
        if(tree instanceof ArrayNode) {
            Set<Object> outerSet = mapper.treeToValue(tree, Set.class);
            for(Object o : outerSet) {
                Set<Role> innerRoles = new HashSet<>();
                roles.add(innerRoles);
                if(o instanceof Iterable) {
                    for(String xid : (Iterable<String>)o) {
                        RoleVO role = dao.getByXid(xid);
                        if(role != null) {
                            innerRoles.add(role.getRole());
                        }else {
                            //Let validation pick this up
                            innerRoles.add(new Role(Common.NEW_ID, xid));
                        }
                    }
                }else {
                    String xid = (String)o;
                    RoleVO role = dao.getByXid(xid);
                    if(role != null) {
                        innerRoles.add(role.getRole());
                    }else {
                        //Let validation pick this up
                        innerRoles.add(new Role(Common.NEW_ID, xid));
                    }
                }
            }
        }else if(tree instanceof TextNode) {
            Set<String> xids = PermissionService.explodeLegacyPermissionGroups(tree.asText());
            for(String xid : xids) {
                RoleVO role = dao.getByXid(xid);
                if(role != null) {
                    roles.add(Collections.singleton(role.getRole()));
                }else {
                    //Let validation pick this up
                    roles.add(Collections.singleton(new Role(Common.NEW_ID, xid)));
                }
            }
        }


        return new MangoPermissionModel(new MangoPermission(roles));
    }

}
