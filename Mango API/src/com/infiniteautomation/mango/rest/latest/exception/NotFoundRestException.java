/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * Exception for when a REST item cannot be found
 *
 * @author Terry Packer
 */
public class NotFoundRestException extends AbstractRestException {

    private static final long serialVersionUID = 1L;

    public NotFoundRestException() {
        super(HttpStatus.NOT_FOUND, null, new TranslatableMessage("translatableException.notFound"));
    }

    public NotFoundRestException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, null, new TranslatableMessage("translatableException.notFound"), cause);
    }
}
