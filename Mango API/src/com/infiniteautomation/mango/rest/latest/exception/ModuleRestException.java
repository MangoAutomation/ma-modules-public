/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 *
 * @author Terry Packer
 */
public class ModuleRestException extends AbstractRestException {
    private static final long serialVersionUID = 1L;

    public ModuleRestException(HttpStatus httpCode, Throwable cause) {
        super(httpCode, cause);
    }

    public ModuleRestException(HttpStatus httpCode, IMangoRestErrorCode mangoCode, Throwable cause) {
        super(httpCode, mangoCode, cause);
        if (mangoCode == null || mangoCode.getCode() >= 1000) {
            throw new IllegalArgumentException ("Module status codes must be < 1000");
        }
    }

    public ModuleRestException(HttpStatus httpCode, IMangoRestErrorCode mangoCode, TranslatableMessage message) {
        super(httpCode, mangoCode, message);
        if (mangoCode == null || mangoCode.getCode() >= 1000) {
            throw new IllegalArgumentException ("Module status codes must be < 1000");
        }
    }

    public ModuleRestException(HttpStatus httpCode, TranslatableMessage message) {
        super(httpCode, null, message);
    }
}
