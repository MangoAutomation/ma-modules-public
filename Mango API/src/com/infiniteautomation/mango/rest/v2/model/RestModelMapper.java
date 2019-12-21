/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Gets a list of RestModelMapping beans from the Spring context and uses them to convert an object to its model.
 * The RestModelMapping beans can be annotated with @Order to specify their priority.
 *
 * @author Terry Packer
 */
@Component
public class RestModelMapper {

    private final List<RestModelMapping<?,?>> mappings;

    @Autowired
    public RestModelMapper(Optional<List<RestModelMapping<?,?>>> mappings) {
        this.mappings = mappings.orElseGet(Collections::emptyList);
    }

    public void addMappings(ObjectMapper objectMapper) {
        //Load in the mappings for Jackson
        for(RestModelMapping<?,?> mapping : this.mappings) {
            if(mapping instanceof RestModelJacksonMapping)
                objectMapper.registerSubtypes(new NamedType(mapping.toClass(), ((RestModelJacksonMapping<?,?>)mapping).getTypeName()));
        }        
    }
    
    public <T> T map(Object from, Class<T> model, PermissionHolder user) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(model);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(from.getClass(), model)) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.map(from, user, this);
                if (result != null) {
                    return result;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", from.getClass(), model));
    }

    public <T> MappingJacksonValue mapWithView(Object from, Class<T> model, PermissionHolder user) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(model);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(from.getClass(), model)) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.map(from, user, this);
                if (result != null) {
                    MappingJacksonValue mappingValue = new MappingJacksonValue(result);
                    mappingValue.setSerializationView(mapping.view(from, user));
                    return mappingValue;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", from.getClass(), model));
    }
    
    public <T> T unMap(Object from, Class<T> model, PermissionHolder user) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(model);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(model, from.getClass())) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.unmap(from, user, this);
                if (result != null) {
                    return result;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", from.getClass(), model));
    }
}
