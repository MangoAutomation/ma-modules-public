/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.serotonin.m2m2.i18n.ProcessResult;
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

    public <T> T map(Object vo, Class<T> modelClass, PermissionHolder user) {
        Objects.requireNonNull(vo);
        Objects.requireNonNull(modelClass);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(vo.getClass(), modelClass)) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.map(vo, user, this);
                if (result != null) {
                    return result;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", vo.getClass(), modelClass));
    }

    public <T> MappingJacksonValue mapWithView(Object vo, Class<T> modelClass, PermissionHolder user) {
        Objects.requireNonNull(vo);
        Objects.requireNonNull(modelClass);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(vo.getClass(), modelClass)) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.map(vo, user, this);
                if (result != null) {
                    MappingJacksonValue mappingValue = new MappingJacksonValue(result);
                    mappingValue.setSerializationView(mapping.view(vo, user));
                    return mappingValue;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", vo.getClass(), modelClass));
    }

    public <T> T unMap(Object model, Class<T> voClass, PermissionHolder user) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(voClass);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.unmapSupports(model.getClass(), voClass)) {
                @SuppressWarnings("unchecked")
                T result = (T) mapping.unmap(model, user, this);
                if (result != null) {
                    return result;
                }
            }
        }

        throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", model.getClass(), voClass));
    }

    /**
     */
    public ProcessResult mapValidationErrors(Class<?> modelClass, Class<?> validatedClass, ProcessResult result) {
        Objects.requireNonNull(modelClass);
        Objects.requireNonNull(validatedClass);

        for (RestModelMapping<?,?> mapping : mappings) {
            if (mapping.supports(validatedClass, modelClass)) {
                return mapping.mapValidationErrors(modelClass, validatedClass, result, this);
            }
        }

        return result;
    }
}
