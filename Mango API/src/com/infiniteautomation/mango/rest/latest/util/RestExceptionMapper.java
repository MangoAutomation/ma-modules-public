/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.util;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.permission.PermissionException;

/**
 * @author Jared Wiltshire
 */
public interface RestExceptionMapper extends ExceptionMapper<AbstractRestException> {

    // TODO the exceptions and status codes are taken from MangoSpringExceptionHandler
    // we should make it easier to reuse the logic from that class elsewhere
    public default AbstractRestException mapException(Throwable e) {
        if (e instanceof AbstractRestException) {
            return (AbstractRestException) e;
        } else if (e instanceof PermissionException) {
            PermissionException exception = (PermissionException) e;
            return new AccessDeniedException(exception.getTranslatableMessage(), exception);
        } else if (e instanceof org.springframework.security.access.AccessDeniedException) {
            return new AccessDeniedException(e);
        } else if (e instanceof ValidationException) {
            ValidationException exception = (ValidationException) e;
            return new ValidationFailedRestException(exception.getValidationResult());
        } else if (e instanceof NotFoundException || e instanceof ResourceNotFoundException) {
            return new NotFoundRestException(e);
        } else {
            return new ServerErrorException(e);
        }
    }
}
