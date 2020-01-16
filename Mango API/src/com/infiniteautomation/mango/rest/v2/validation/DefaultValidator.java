/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * This class is part of a larger project to introduce JSR-303 style validation.  For now
 * any @RequestBody annotated parameters that are also annotated with @Validated will
 * be processed by this validator.
 *
 * Currently this only notifys the ModelMapper to map the vo fields to model fields on a ValidationException
 *
 * @author Terry Packer
 */
public class DefaultValidator implements Validator {

    public static final String VALIDATION_SOURCE = "validationSource";

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(target != null) {
            RequestContextHolder.currentRequestAttributes().setAttribute(VALIDATION_SOURCE, target.getClass(), RequestAttributes.SCOPE_REQUEST);
        }
    }

}
