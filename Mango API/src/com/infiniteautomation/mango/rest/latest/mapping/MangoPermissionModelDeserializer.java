/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

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
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.role.Role;

/**
 * @author Terry Packer
 */
public class MangoPermissionModelDeserializer extends StdDeserializer<MangoPermissionModel>{

    private static final long serialVersionUID = 1L;
    private final PermissionService permissionService;

    public MangoPermissionModelDeserializer() {
        super(MangoPermissionModel.class);
        this.permissionService = Common.getBean(PermissionService.class);
    }


    @Override
    public MangoPermissionModel deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode tree = jp.readValueAsTree();
        return nodeToModel(tree, mapper);
    }

    @SuppressWarnings("unchecked")
    public MangoPermissionModel nodeToModel(JsonNode tree, ObjectMapper mapper) throws JsonProcessingException {
        Set<Set<Role>> roles = new HashSet<>();
        if(tree instanceof ArrayNode) {
            Set<Object> outerSet = mapper.treeToValue(tree, Set.class);
            for(Object o : outerSet) {
                Set<Role> innerRoles = new HashSet<>();
                roles.add(innerRoles);
                if(o instanceof Iterable) {
                    for(String xid : (Iterable<String>)o) {
                        Role role = permissionService.getRole(xid);
                        if(role != null) {
                            innerRoles.add(role);
                        }else {
                            //Let validation pick this up
                            innerRoles.add(new Role(Common.NEW_ID, xid));
                        }
                    }
                }else {
                    String xid = (String)o;
                    Role role = permissionService.getRole(xid);
                    if(role != null) {
                        innerRoles.add(role);
                    }else {
                        //Let validation pick this up
                        innerRoles.add(new Role(Common.NEW_ID, xid));
                    }
                }
            }
        }else if(tree instanceof TextNode) {
            Set<String> xids = PermissionService.explodeLegacyPermissionGroups(tree.asText());
            for(String xid : xids) {
                Role role = permissionService.getRole(xid);
                if(role != null) {
                    roles.add(Collections.singleton(role));
                }else {
                    //Let validation pick this up
                    roles.add(Collections.singleton(new Role(Common.NEW_ID, xid)));
                }
            }
        }


        return new MangoPermissionModel(new MangoPermission(roles));
    }

    /**
     * Explode a comma separated group of permissions (roles) from the legacy format (don't strip whitespace)
     */
    public static Set<String> explodeLegacyPermissionGroups(String groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> set = new HashSet<>();
        for (String s : groups.split(",")) {
            if (!s.isEmpty()) {
                set.add(s);
            }
        }
        return Collections.unmodifiableSet(set);
    }

}
