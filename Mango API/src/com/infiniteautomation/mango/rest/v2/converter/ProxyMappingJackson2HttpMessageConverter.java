/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.converter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;

/**
 * 
 * Create proxies for incoming models
 *
 * @author Terry Packer
 */
public class ProxyMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter{

    public ProxyMappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }
    
    
    /*
     * (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canRead(java.lang.reflect.Type, java.lang.Class, org.springframework.http.MediaType)
     */
    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        //TODO Better than this.
        if (!canRead(mediaType))
            return false;

        return type instanceof Class && AbstractVoModel.class.isAssignableFrom((Class<?>) type);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
     */
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    
    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#read(java.lang.reflect.Type, java.lang.Class, org.springframework.http.HttpInputMessage)
     */
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }
    
    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) throws IOException{
        try {
            Constructor<?> c = javaType.getRawClass().getConstructor();
            Object o = c.newInstance();
            ProxyFactory f = new ProxyFactory(o);
            Set<String> nullSettersCalled = new HashSet<>();
            //Track any calls to setters with null values
            f.addAdvice(new MethodInterceptor() {

                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    if(invocation.getMethod().getName().startsWith("set")) {
                        Object[] args = invocation.getArguments();
                        if(args.length == 1 && args[0] == null)
                            nullSettersCalled.add(invocation.getMethod().getName().substring(3, invocation.getMethod().getName().length()).toLowerCase());
                    }
                    return invocation.proceed();
                }
                
            });
            AbstractVoModel<?> model = (AbstractVoModel<?>)f.getProxy();
            //TODO this does not support views (see superclass)
            model = this.objectMapper.readerForUpdating(model).readValue(inputMessage.getBody());
            AbstractVoModel<?> unproxy = (AbstractVoModel<?>)((Advised)model).getTargetSource().getTarget();
            unproxy.setNullSettersCalled(nullSettersCalled);
            return unproxy;
        } catch (Exception e) {
            throw new ServerErrorException(e);
        }
    }
}
