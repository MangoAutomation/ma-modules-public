/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.infiniteautomation.mango.rest.RestModelMapper;
import com.infiniteautomation.mango.rest.RestModelMapping;

/**
 * 
 * @author Terry Packer
 *
 */
public class RestModelTypeIdResolver implements TypeIdResolver {

    @Autowired
    private RestModelMapper modelMapper;
    private JavaType baseType;
    
    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        RestModelMapping<?,?> mapping = modelMapper.getMappingUsingToClass(aClass);
        if(mapping != null)
            return mapping.getTypeId();
        return null;
    }

    @Override
    public JavaType typeFromId(DatabindContext databindContext, String typeId) throws IOException {
        RestModelMapping<?,?> mapping = modelMapper.getMappingUsingTypeId(typeId);
        if(mapping != null) {
            return TypeFactory.defaultInstance().constructSpecializedType(baseType, mapping.toClass());
        }
        throw new IOException("Cannot find class for type id \"" + typeId + "\"");
    }

    @Override
    public String getDescForKnownTypeIds() {
        return null;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
