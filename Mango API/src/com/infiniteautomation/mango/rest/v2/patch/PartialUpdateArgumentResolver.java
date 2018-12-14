/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.patch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.service.AbstractVOService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;

/**
 * PATCH support via the PatchVORequestBody annotation.
 * 
 * NOTE that the models must be in the model mapper for this to work
 * 
 * @author Terry Packer
 *
 */
@Component
public class PartialUpdateArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String XID = "xid";
    private static final String ID = "id";
    
    private final ObjectMapper objectMapper;
    private final ApplicationContext context;
    private final RestModelMapper modelMapper;
    
    @Autowired 
    public PartialUpdateArgumentResolver(@Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME) ObjectMapper objectMapper, 
            ApplicationContext context, 
            RestModelMapper modelMapper) {
        this.objectMapper = objectMapper;
        this.context = context;
        this.modelMapper = modelMapper;
    }
    

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        RequestMapping methodAnot = parameter.getMethodAnnotation(RequestMapping.class);
        if( methodAnot == null ) return false;

        if( !Arrays.asList(methodAnot.method()).contains(RequestMethod.PATCH) )
            return false;

        return parameter.hasParameterAnnotation(PatchVORequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        ServletServerHttpRequest req = createInputMessage(webRequest);

        PatchVORequestBody patch = parameter.getParameterAnnotation(PatchVORequestBody.class);
        
        Class<?> serviceClass = patch.service();
        AbstractVOService<?,?> service = (AbstractVOService<?,?>)context.getBean(serviceClass);

        User user = Common.getHttpUser();
        Object vo;
        switch(patch.idType()) {
            case ID:
              Integer id = Integer.parseInt(getPathVariables(webRequest).get(ID));
              vo = service.getFull(id, user);
              if(vo == null)
                  throw new NotFoundRestException();
              else {
                  Object model = modelMapper.map(vo, patch.modelClass(), user);
                  return readJavaType(model, req);
              }
            case XID:
                String xid = getPathVariables(webRequest).get(XID);
                vo = service.getFull(xid, user);
                if(vo == null)
                    throw new NotFoundRestException();
                else {
                    Object model = modelMapper.map(vo, patch.modelClass(), user);
                    return readJavaType(model, req);
                }
                default:
            case OTHER:
                String other = getPathVariables(webRequest).get(patch.urlPathVariableName());
                vo = service.getFull(other, user);
                if(vo == null)
                    throw new NotFoundRestException();
                else {
                    Object model = modelMapper.map(vo, patch.modelClass(), user);
                    return readJavaType(model, req);
                }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getPathVariables(NativeWebRequest webRequest) {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        return (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    private ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        return new ServletServerHttpRequest(servletRequest);
    }

    private Object readJavaType(Object object, HttpInputMessage inputMessage) {
        try {
            return this.objectMapper.readerForUpdating(object).readValue(inputMessage.getBody());
        }
        catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document: " + ex.getMessage(), ex);
        }
    }

}
