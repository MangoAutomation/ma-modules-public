/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.webapp;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;

/**
 * Forwards GET requests from /file-stores to /rest/latest/file-stores
 * @author Jared Wiltshire
 */
@Component
@ConditionalOnProperty("${rest.enabled:true}")
@WebFilter(
        asyncSupported = true,
        urlPatterns = {"/file-stores/*"},
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ASYNC})
public class FileStoreFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (!"GET".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
        } else {
            String path = requestURI.substring("/file-stores/".length());
            request.getRequestDispatcher("/rest/latest/file-stores/download-file/" + path).forward(request, response);
        }
    }

}
