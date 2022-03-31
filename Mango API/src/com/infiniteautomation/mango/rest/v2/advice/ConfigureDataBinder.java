/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.advice;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author Jared Wiltshire
 */
@ControllerAdvice
public class ConfigureDataBinder {

    private static final String[] DENY_LIST = new String[] { "class.*", "Class.*", "*.class.*", "*.Class.*" };

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields(DENY_LIST);
    }

}
