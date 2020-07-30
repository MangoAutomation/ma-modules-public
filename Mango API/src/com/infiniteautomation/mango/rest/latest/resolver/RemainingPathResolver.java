/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.resolver;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Extracts the portion of the path matched by /**
 *
 * @author Jared Wiltshire
 */
@Component
public class RemainingPathResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return String.class.equals(parameter.getParameterType()) && parameter.hasParameterAnnotation(RemainingPath.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        String path = (String) webRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String bestMatchPattern = (String ) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        AntPathMatcher apm = new AntPathMatcher();
        return URLDecoder.decode(apm.extractPathWithinPattern(bestMatchPattern, path), StandardCharsets.UTF_8.name());

    }

}
