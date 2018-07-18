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

}
