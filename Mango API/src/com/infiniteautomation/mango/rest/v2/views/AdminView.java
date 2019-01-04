/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.views;

/**
 * Marker to indicate this field is only viewable by admin.
 * 
 * To enable, use this with @JacksonView in a model and then return a MappingJacksonValue from 
 * your controller.
 * 
 * @author Terry Packer
 *
 */
public interface AdminView {

}
