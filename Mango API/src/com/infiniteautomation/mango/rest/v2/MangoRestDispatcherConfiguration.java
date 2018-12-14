/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.infiniteautomation.mango.rest.v2.converter.ExceptionCsvMessageConverter;
import com.infiniteautomation.mango.rest.v2.model.event.AuditEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataSourceEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.MissingEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.PublisherEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.SystemEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.EmailEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.ProcessEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.SetPointEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.patch.PartialUpdateArgumentResolver;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.util.AbstractRestModelConverter;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.CsvMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.CsvQueryArrayStreamMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.CsvRowMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.HtmlHttpMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.SerotoninJsonMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.converters.SqlMessageConverter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;
import com.serotonin.m2m2.web.mvc.spring.MangoCommonConfiguration;
import com.serotonin.m2m2.web.mvc.spring.security.MangoMethodSecurityConfiguration;

/**
 * @author Terry Packer
 *
 */
@Configuration("MangoV2RestDispatcherConfiguration")
@Import({MangoCommonConfiguration.class, MangoMethodSecurityConfiguration.class, MangoWebSocketConfiguration.class})
@EnableWebMvc
@ComponentScan(basePackages = { "com.infiniteautomation.mango.rest.v2" })
public class MangoRestDispatcherConfiguration implements WebMvcConfigurer {
    

    final ObjectMapper mapper;
    final PartialUpdateArgumentResolver resolver;
    
    @Autowired
    public MangoRestDispatcherConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper,
            PartialUpdateArgumentResolver resolver) {
        this.mapper = mapper;
        this.resolver = resolver;
        mapper.registerSubtypes(
                    //Event Handlers
                    new NamedType(EmailEventHandlerModel.class, "EMAIL"),
                    new NamedType(ProcessEventHandlerModel.class, "PROCESS"),
                    new NamedType(SetPointEventHandlerModel.class, "SET_POINT"),
                    new NamedType(AuditEventTypeModel.class, "AUDIT"),
                    //Event Types
                    new NamedType(DataPointEventTypeModel.class, "DATA_POINT"),
                    new NamedType(DataSourceEventTypeModel.class, "DATA_SOURCE"),
                    new NamedType(MissingEventTypeModel.class, "MISSING"),
                    new NamedType(PublisherEventTypeModel.class, "PUBLISHER"),
                    new NamedType(SystemEventTypeModel.class, "SYSTEM")
                );
        
    }

    /**
     * Create a Path helper that will not URL Decode
     * the context path and request URI but will
     * decode the path variables...
     *
     */
    public UrlPathHelper getUrlPathHelper() {
        UrlPathHelper helper = new UrlPathHelper();
        helper.setUrlDecode(false);
        return helper;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false).setUrlPathHelper(getUrlPathHelper());
    }

    /**
     * Setup Content Negotiation to map url extensions to returned data types
     *
     * @see http
     *      ://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // dont set defaultContentType to text/html, we dont want this for REST
        // it causes Accept: */* headers to map to Accept: text/html
        // which causes hell for finding acceptable content types

        configurer
        .favorPathExtension(false)
        .ignoreAcceptHeader(false)
        .favorParameter(true)
        .useRegisteredExtensionsOnly(true)
        //.mediaType("html", MediaType.TEXT_HTML) TODO should we re-enable this?
        .mediaType("xml", MediaType.APPLICATION_XML)
        .mediaType("json", MediaType.APPLICATION_JSON_UTF8)
        .mediaType("sjson", MediaTypes.SEROTONIN_JSON)
        .mediaType("csv", MediaTypes.CSV_V1)
        .mediaType("csv1", MediaTypes.CSV_V1)
        .mediaType("csv2", MediaTypes.CSV_V2);
    }

    /**
     * Configure the Message Converters for the API for now only JSON
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        // see WebMvcConfigurationSupport.addDefaultHttpMessageConverters()

        converters.add(new ResourceHttpMessageConverter());
        converters.add(new ResourceRegionHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(mapper));
        converters.add(new CsvMessageConverter());
        converters.add(new CsvRowMessageConverter());
        converters.add(new CsvQueryArrayStreamMessageConverter());
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new HtmlHttpMessageConverter());
        converters.add(new SerotoninJsonMessageConverter());
        converters.add(new ExceptionCsvMessageConverter());
        converters.add(new SqlMessageConverter());

        //Now is a good time to register our Sero Json Converter
        Common.JSON_CONTEXT.addConverter(new AbstractRestModelConverter(), AbstractRestModel.class);

    }
    
    /**
     * To inject a singleton into the JSON Import rest controller
     * @return
     */
    @Bean()
    public MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources() {
        return new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
    }
    
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
