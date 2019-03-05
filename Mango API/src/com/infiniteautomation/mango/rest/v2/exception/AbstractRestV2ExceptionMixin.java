/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.infiniteautomation.mango.rest.v2.views.AdminView;

/**
 * @author Terry Packer
 *
 */
public interface AbstractRestV2ExceptionMixin {

    @JsonProperty
    int getMangoStatusCode();
    
    @JsonProperty
    String getMangoStatusName();
    
    @JsonView(AdminView.class)
    @JsonProperty("cause")
    String getCauseMessage();
}
