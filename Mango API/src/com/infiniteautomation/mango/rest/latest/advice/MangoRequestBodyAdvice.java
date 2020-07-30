/**
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.advice;

import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
/**
 * Class to place the incoming model class into the request scope so
 *  we can use it to find the model mapper at a later date outside of the controller
 *  that is receiving the model.  i.e. validation errors
 *
 * @author Jared Wiltshire
 */
@ControllerAdvice
public class MangoRequestBodyAdvice extends RequestBodyAdviceAdapter {

    public static final String MODEL_CLASS = "modelClass";

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
            MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if(body != null) {
            RequestContextHolder.currentRequestAttributes().setAttribute(MODEL_CLASS, body.getClass(), RequestAttributes.SCOPE_REQUEST);
        }
        return body;
    }
}
