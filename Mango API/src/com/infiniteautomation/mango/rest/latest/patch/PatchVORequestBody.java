/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.patch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for PATCH support 
 * 
 * @author Terry Packer
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PatchVORequestBody {
    
    public enum PatchIdField {
        XID,ID,OTHER
    }

    /**
     * The service for the VO to patch
     * @return
     */
    Class<?> service();
    
    /**
     * Class for model
     * @return
     */
    Class<?> modelClass();
    
    /**
     * The type of ID
     * @return
     */
    PatchIdField idType() default PatchIdField.XID;
    
    /**
     * The name of the path variable mapping in the url
     * @return
     */
    String urlPathVariableName() default "xid";
    
}
