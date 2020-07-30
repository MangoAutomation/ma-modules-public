/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.Map;

import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.ShouldNeverHappenException;
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

    public Class<? extends F> fromClass();
    public Class<? extends T> toClass();

    /**
     * Checks if the mapping supports mapping the object to the desired model class.
     *
     * @param from
     * @param toClass
     * @return true if the mapping supports mapping the object to the desired model class
     */
    public default boolean supports(Class<?> from, Class<?> toClass) {
        return this.fromClass().isAssignableFrom(from) &&
                toClass.isAssignableFrom(this.toClass());
    }

    /**
     * Performs the mapping of the object to the desired model class. If null is returned other mappings will be tried in order.
     *
     * @param from
     * @param user
     * @param mapper
     * @return The model object or null
     */
    public T map(Object from, PermissionHolder user, RestModelMapper mapper);

    /**
     * TODO Mango 4.0 remove default and require an implementation
     * Unmap a model by creating a new object
     * @param from
     * @param user
     * @param mapper
     * @return
     */
    default public F unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        throw new ShouldNeverHappenException("Unimplemented");
    }

    /**
     * TODO Mango 4.0 remove default and require an implementation
     * Unmap a model into an existing object
     * @param from
     * @param into
     * @param user
     * @param mapper
     * @return
     * @throws ValidationException
     */
    default public F unmapInto(Object from, F into, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        throw new ShouldNeverHappenException("Unimplemented");
    }

    /**
     * Returns the view to use when serializing the mapped object
     *
     * @param from
     * @param user
     * @return
     */
    public default Class<?> view(Object from, PermissionHolder user) {
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
    default public ProcessResult mapValidationErrors(Class<?> modelClass, Class<?> validatedClass,
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
