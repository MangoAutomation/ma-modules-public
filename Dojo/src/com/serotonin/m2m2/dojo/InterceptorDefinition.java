package com.serotonin.m2m2.dojo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.serotonin.m2m2.module.HandlerInterceptorDefinition;

public class InterceptorDefinition extends HandlerInterceptorDefinition {
    @Override
    public HandlerInterceptor getInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                request.setAttribute("dojoURI", getModule().getWebPath() + "/web/dojo-release-1.7.3/");
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                    ModelAndView modelAndView) throws Exception {
                // no op
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                    Exception ex) throws Exception {
                // no op
            }
        };
    }
}
