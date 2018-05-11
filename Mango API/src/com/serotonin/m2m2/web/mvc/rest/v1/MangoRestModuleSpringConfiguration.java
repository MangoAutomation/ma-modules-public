/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.mapping.DataPointCsvMessageConverter;
import com.infiniteautomation.mango.rest.v2.mapping.GenericCSVMessageConverter;
import com.infiniteautomation.mango.rest.v2.mapping.PointValueTimeStreamCsvMessageConverter;

/**
 * Class to configure spring for any module specific REST components.
 *
 * Can also be useful to add functionality that will make it into the core on the next minor release.
 *
 * @author Terry Packer
 *
 */
@Configuration
public class MangoRestModuleSpringConfiguration extends WebMvcConfigurerAdapter{

    @Autowired
    ObjectMapper mapper;

    /**
     * Configure the Message Converters for the API for now only JSON
     */
    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        converters.add(new PointValueTimeStreamCsvMessageConverter());
        converters.add(new DataPointCsvMessageConverter());
        converters.add(new CsvObjectStreamMessageConverter());
        converters.add(new GenericCSVMessageConverter(mapper));
    }
}
