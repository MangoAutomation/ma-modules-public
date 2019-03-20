/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.util;

import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ResourceNotFoundException;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.permission.PermissionException;

/**
 * @author Jared Wiltshire
 */
public interface RestExceptionMapper extends ExceptionMapper<AbstractRestV2Exception> {

    // TODO the exceptions and status codes are taken from MangoSpringExceptionHandler
    // we should make it easier to reuse the logic from that class elsewhere
    public default AbstractRestV2Exception mapException(Throwable e) {
        if (e instanceof AbstractRestV2Exception) {
            return (AbstractRestV2Exception) e;
        } else if (e instanceof PermissionException) {
            PermissionException exception = (PermissionException) e;
            return new AccessDeniedException(exception.getTranslatableMessage(), exception);
        } else if (e instanceof org.springframework.security.access.AccessDeniedException) {
            return new AccessDeniedException(e);
        } else if (e instanceof ValidationException) {
            ValidationException exception = (ValidationException) e;
            return new ValidationFailedRestException(exception.getValidationResult());
        } else if (e instanceof NotFoundException || e instanceof ResourceNotFoundException) {
            throw new NotFoundRestException(e);
        } else {
            return new ServerErrorException(e);
        }
    }
}
