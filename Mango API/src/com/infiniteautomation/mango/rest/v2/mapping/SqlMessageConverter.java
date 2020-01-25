/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 *
 * @author Terry Packer
 */
public class SqlMessageConverter extends StringHttpMessageConverter {

    public SqlMessageConverter(){
        ArrayList<MediaType> types = new ArrayList<MediaType>();
        types.add(new MediaType("application", "sql"));
        this.setSupportedMediaTypes(types);
    }
}
