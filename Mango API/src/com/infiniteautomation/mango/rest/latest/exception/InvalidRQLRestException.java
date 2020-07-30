/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infiniteautomation.mango.util.exception.InvalidRQLException;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 *
 * @author Terry Packer
 */
public class InvalidRQLRestException extends AbstractRestV2Exception{

    private static final long serialVersionUID = 1L;
    @JsonProperty
    private final String query;

    public InvalidRQLRestException(InvalidRQLException cause) {
        super(HttpStatus.BAD_REQUEST, MangoRestErrorCode.RQL_PARSE_FAILURE, new TranslatableMessage("common.invalidRql"), cause.getCause());
        this.query = cause.getQuery();
    }

    public String getQuery() {
        return this.query;
    }

}
