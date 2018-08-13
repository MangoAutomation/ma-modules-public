/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.converter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        //TODO Better than this.
        if (!canRead(mediaType))
            return false;

        return type instanceof Class && AbstractVoModel.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

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
            Map<String, Object> settersCalled = new HashMap<>();
            //Track any calls to setters with null values
            f.addAdvice(new MethodInterceptor() {

                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    if(invocation.getMethod().getName().startsWith("set")) {
                        Object[] args = invocation.getArguments();
                        if(args.length == 1) {
                            settersCalled.put(invocation.getMethod().getName().substring(3, invocation.getMethod().getName().length()).toLowerCase(), args[0]);
                        }
                    }
                    return invocation.proceed();
                }

            });
            AbstractVoModel<?> model = (AbstractVoModel<?>)f.getProxy();
            //TODO this does not support views (see superclass)
            model = this.objectMapper.readerForUpdating(model).readValue(inputMessage.getBody());
            AbstractVoModel<?> unproxy = (AbstractVoModel<?>)((Advised)model).getTargetSource().getTarget();
            unproxy.setSettersCalled(settersCalled);
            return unproxy;
        } catch (Exception e) {
            throw new ServerErrorException(e);
        }
    }
}
