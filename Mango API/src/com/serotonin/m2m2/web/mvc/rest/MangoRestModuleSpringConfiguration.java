/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.infiniteautomation.mango.rest.v2.genericcsv.CsvJacksonModule;
import com.infiniteautomation.mango.rest.v2.genericcsv.GenericCSVMessageConverter;
import com.infiniteautomation.mango.rest.v2.mapping.PointValueTimeStreamCsvMessageConverter;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.web.mvc.rest.v1.CsvObjectStreamMessageConverter;

/**
 * Class to configure spring for any module specific REST components.
 *
 * Can also be useful to add functionality that will make it into the core on the next minor release.
 *
 * @author Terry Packer
 *
 */
@Configuration
public class MangoRestModuleSpringConfiguration implements WebMvcConfigurer {

    final ObjectMapper mapper;
    
    @Autowired
    public MangoRestModuleSpringConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        this.mapper = mapper;
        
    }

    @Bean("csvObjectMapper")
    public ObjectMapper csvObjectMapper() {
        return mapper.copy()
                .setDateFormat(GenericCSVMessageConverter.EXCEL_DATE_FORMAT)
                .registerModule(new CsvJacksonModule());
    }

    /**
     * Configure the Message Converters for the API for now only JSON
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new PointValueTimeStreamCsvMessageConverter());
        converters.add(new CsvObjectStreamMessageConverter());
        converters.add(new GenericCSVMessageConverter(csvObjectMapper()));
    }

    @Bean
    public MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources() {
        return new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
    }

}
