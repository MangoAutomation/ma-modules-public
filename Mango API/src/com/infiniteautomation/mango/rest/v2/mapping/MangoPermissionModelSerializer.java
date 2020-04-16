/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.rest.v2.model.permissions.MangoPermissionModel;
import com.serotonin.m2m2.vo.role.Role;

/**
 * Serialize the MangoPermissionModel as an array of arrays
 * @author Terry Packer
 */
public class MangoPermissionModelSerializer extends JsonSerializer<MangoPermissionModel>{

    @Override
    public void serialize(MangoPermissionModel value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException, JsonProcessingException {
        if(value == null) {
            jgen.writeNull();
        }else {
            Set<Set<Role>> roleSets = value.getPermission().getRoles();

            jgen.writeStartArray();
            for(Set<Role> roleSet : roleSets) {
                if(roleSet.size() > 1) {
                    jgen.writeStartArray();
                }
                for(Role role : roleSet) {
                    jgen.writeString(role.getXid());
                }
                if(roleSet.size() > 1) {
                    jgen.writeEndArray();
                }
            }
            jgen.writeEndArray();
        }
    }

}
