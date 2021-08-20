/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.Map;

import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Mapping to deliver support for converting between classes
 * when transporting payloads in/out of the REST API
 *
 * @author Terry Packer
 */
public interface RestModelMapping<F, T> {

    /**
     * @return the VO class supported by this mapper
     */
    Class<? extends F> fromClass();

    /**
     *
     * @return the model class supported by this mapper
     */
    Class<? extends T> toClass();

    /**
     * Checks if the mapping supports mapping the object to the desired model class.
     *
     * @param voClass vo class
     * @param modelClass model class
     * @return true if the mapping supports mapping the object to the desired model class
     */
    default boolean supports(Class<?> voClass, Class<?> modelClass) {
        return fromClass().isAssignableFrom(voClass) && modelClass.isAssignableFrom(toClass());
    }

    /**
     * Check for reverse mapping support.
     * @param modelClass the model class
     * @param voClass vo class
     * @return
     */
    default boolean unmapSupports(Class<?> modelClass, Class<?> voClass) {
        return toClass().isAssignableFrom(modelClass) && voClass.isAssignableFrom(fromClass());
    }

    /**
     * Performs the mapping of the object to the desired model class. If null is returned other mappings will be tried in order.
     *
     * @param from
     * @param user
     * @param mapper
     * @return The model object or null
     */
    T map(Object from, PermissionHolder user, RestModelMapper mapper);

    /**
     * TODO Remove default and require an implementation for all model mappings
     * Unmap a model by creating a new object
     * @param from
     * @param user
     * @param mapper
     * @return
     * @throws ValidationException
     */
    default F unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * TODO Remove default and require an implementation
     * Unmap a model into an existing object
     * @param from
     * @param into
     * @param user
     * @param mapper
     * @return
     * @throws ValidationException
     */
    default F unmapInto(Object from, F into, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Returns the view to use when serializing the mapped object
     *
     * @param from
     * @param user
     * @return
     */
    default Class<?> view(Object from, PermissionHolder user) {
        return null;
    }

    /**
     * Perform any model to vo field mappings that may be off during validation
     *
     * @param modelClass
     * @param validatedClass
     * @param result
     * @param restModelMapper
     * @return
     */
    default ProcessResult mapValidationErrors(Class<?> modelClass, Class<?> validatedClass,
            ProcessResult result, RestModelMapper restModelMapper) {
        return result;
    }

    /**
     * Helper method for basic mapping
     * @param fieldMap
     * @param result
     * @return
     */
    default ProcessResult mapValidationErrors(Map<String, String> fieldMap, ProcessResult result) {
        ProcessResult mapped = new ProcessResult();
        for(ProcessMessage m : result.getMessages()) {
            String mappedField = fieldMap.get(m.getContextKey());
            if(mappedField != null) {
                mapped.addMessage(new ProcessMessage(
                        m.getLevel(),
                        m.getGenericMessage(),
                        mappedField,
                        m.getContextualMessage()));
            }else {
                mapped.addMessage(m);
            }
        }
        return mapped;
    }

}
