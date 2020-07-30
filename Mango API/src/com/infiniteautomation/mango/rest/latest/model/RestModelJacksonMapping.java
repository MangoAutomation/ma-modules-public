/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

/**
 * Used to ensure Jackson is able to de-serialize the model (type T) in this mapping
 * 
 * @author Terry Packer
 *
 */
public interface RestModelJacksonMapping<F, T> extends RestModelMapping<F, T> {

    
    /**
     * Return the type name that maps the T class (Model) to a Type in Jackson
     */
    public String getTypeName();
    
}
