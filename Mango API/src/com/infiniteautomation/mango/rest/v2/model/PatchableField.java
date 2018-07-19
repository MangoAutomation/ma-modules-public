/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that this field can be used in a PATCH/Partial update call
 * @author Terry Packer
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PatchableField {

    /**
     * Is this field enabled to be patchable, by default all fields in the model are even without this annotation
     * @return
     */
    boolean enabled() default true;
    
    /**
     * Allow values that are specifically set to null by Jackson i.e. the JSON had a null value set into it
     * @return
     */
    boolean allowNull() default true;
    
}
