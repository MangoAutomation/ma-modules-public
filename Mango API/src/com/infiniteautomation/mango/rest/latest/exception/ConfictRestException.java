/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Jared Wiltshire
 */
public class ConfictRestException extends AbstractRestException {
    private static final long serialVersionUID = 1L;

    public ConfictRestException() {
        super(HttpStatus.CONFLICT, MangoRestErrorCode.GENERIC_CONFILICT);
    }

    public ConfictRestException(Throwable cause) {
        super(HttpStatus.CONFLICT, MangoRestErrorCode.GENERIC_CONFILICT, cause);
    }

    public ConfictRestException(TranslatableMessage message) {
        super(HttpStatus.CONFLICT, MangoRestErrorCode.GENERIC_CONFILICT, message);
    }

    public ConfictRestException(TranslatableMessage message, Throwable cause) {
        super(HttpStatus.CONFLICT, MangoRestErrorCode.GENERIC_CONFILICT, message, cause);
    }

    public ConfictRestException(MangoRestErrorCode code, TranslatableMessage message, Throwable cause) {
        super(HttpStatus.CONFLICT, code, message, cause);
    }
}
