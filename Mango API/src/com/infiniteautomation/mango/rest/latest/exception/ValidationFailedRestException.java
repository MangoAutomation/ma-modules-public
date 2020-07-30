/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infiniteautomation.mango.rest.latest.model.RestValidationResult;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * Exception to provide validation failure information
 * out of the REST api
 * 
 * @author Terry Packer
 */
public class ValidationFailedRestException extends AbstractRestException {

	private static final long serialVersionUID = 1L;
	private final RestValidationResult result;

    public ValidationFailedRestException(ProcessResult validationResult) {
        this(new RestValidationResult(validationResult));
    }
    
	public ValidationFailedRestException(RestValidationResult result) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, MangoRestErrorCode.VALIDATION_FAILED, new TranslatableMessage("validate.validationFailed"));
		this.result = result;
	}

	@JsonProperty
    public RestValidationResult getResult(){
		return result;
	}
}
