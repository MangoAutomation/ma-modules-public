/*
 * Copyright (C) 2020 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Jared Wiltshire
 */
@Component
public class RequestUtils {

    /**
     * Used to extract the remaining path for REST controllers that have a
     * {@link org.springframework.web.bind.annotation.RequestMapping @RequestMapping} like <code>/**</code>
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    public String extractRemainingPath(HttpServletRequest request) throws UnsupportedEncodingException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        AntPathMatcher apm = new AntPathMatcher();
        return URLDecoder.decode(apm.extractPathWithinPattern(bestMatchPattern, path), StandardCharsets.UTF_8.name());
    }

}
