/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infiniteautomation.mango.rest.RestApiJacksonModuleDefinition;
import com.infiniteautomation.mango.rest.latest.JsonEmportController.ImportStatusProvider;
import com.infiniteautomation.mango.rest.latest.genericcsv.CsvJacksonModule;
import com.infiniteautomation.mango.rest.latest.genericcsv.GenericCSVMessageConverter;
import com.infiniteautomation.mango.rest.latest.mapping.JScienceModule;
import com.infiniteautomation.mango.rest.latest.mapping.MangoRestJacksonModule;
import com.infiniteautomation.mango.rest.latest.mapping.PermissionConverter;
import com.infiniteautomation.mango.rest.latest.mapping.SingleMintermPermissionConverter;
import com.infiniteautomation.mango.rest.latest.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.spring.MangoCommonConfiguration;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.webapp.RestWebApplicationInitializer;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.m2m2.web.mvc.spring.MangoLocaleResolver;
import com.serotonin.m2m2.web.mvc.spring.security.MangoMethodSecurityConfiguration;

/**
 * @author Terry Packer
 *
 */
@Configuration
@Import({MangoCommonConfiguration.class, MangoMethodSecurityConfiguration.class, MangoWebSocketConfiguration.class})
@EnableWebMvc
@ComponentScan(basePackages = { "com.infiniteautomation.mango.rest.latest" }, excludeFilters = {})
public class MangoRestDispatcherConfiguration implements WebMvcConfigurer {
    /**
     * Note: Dispatcher mapping is set here
     * <ul>
     * <li>{@link RestWebApplicationInitializer#onStartup(javax.servlet.ServletContext)}</li>
     * </ul>
     */
    public static final String CONTEXT_ID = "restV3Context";
    public static final String DISPATCHER_NAME = "restV3DispatcherServlet";

    final List<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers = new ArrayList<>();
    final List<HttpMessageConverter<?>> converters = new ArrayList<>();
    final List<HandlerInterceptor> interceptors = new ArrayList<>();
    Environment env;
    PermissionService permissionService;

    /**
     * Should be supplied by
     * com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration.taskExecutor(ExecutorService)
     */
    AsyncTaskExecutor asyncTaskExecutor;

    @Autowired
    public void configureMangoRestDispatcherConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME) ObjectMapper mapper,
            List<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers,
            List<HandlerInterceptor> interceptors, AsyncTaskExecutor asyncTaskExecutor,
            List<HttpMessageConverter<?>> injectedConverters,
            Environment env,
            PermissionService permissionService) {

        this.handlerMethodArgumentResolvers.addAll(handlerMethodArgumentResolvers);
        this.interceptors.addAll(interceptors);
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.env = env;
        this.permissionService = permissionService;

        // register Jackson modules from API module
        mapper.registerModule(new MangoRestJacksonModule())
            .registerModule(new JScienceModule());

        this.converters.addAll(injectedConverters);
        this.converters.add(new ResourceHttpMessageConverter());
        this.converters.add(new ResourceRegionHttpMessageConverter());
        this.converters.add(new MappingJackson2HttpMessageConverter(mapper));
        this.converters.add(new ByteArrayHttpMessageConverter());
        this.converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
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
     * @see <a href="http://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc">Spring article</a>
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
        .mediaType("xml", MediaType.APPLICATION_XML)
        .mediaType("json", MediaType.APPLICATION_JSON_UTF8)
        .mediaType("sjson", MediaTypes.SEROTONIN_JSON)
        .mediaType("csv", MediaTypes.CSV_V2)
        .mediaType("csv2", MediaTypes.CSV_V2)
        .mediaType("txt", MediaType.TEXT_PLAIN);
    }

    @Bean("csvObjectMapper")
    public ObjectMapper csvObjectMapper(@Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME) ObjectMapper mapper) {
        return mapper.copy()
                .setDateFormat(GenericCSVMessageConverter.EXCEL_DATE_FORMAT)
                .registerModule(new CsvJacksonModule());
    }

    @Bean("csvMapper")
    public CsvMapper csvMapper(RestApiJacksonModuleDefinition jacksonModuleDef) {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        csvMapper.configure(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS, false);
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // drops any properties/columns not registered in the schema when serializing
        csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.registerModule(new Jdk8Module());
        csvMapper.registerModule(new CsvJacksonModule());
        for (var jacksonModule : jacksonModuleDef.getJacksonModules()) {
            csvMapper.registerModule(jacksonModule);
        }
        csvMapper.setTimeZone(TimeZone.getDefault()); //Set to system tz
        return csvMapper;
    }

    /**
     * Configure the Message Converters for the API, this will override any defaults
     *  added by Spring.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(this.converters);
    }

    /**
     * To inject a singleton into the JSON Import rest controller
     */
    @Bean()
    public MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources() {
        return new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(handlerMethodArgumentResolvers);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new SingleMintermPermissionConverter(permissionService));
        registry.addConverter(new PermissionConverter(permissionService));
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor);
        configurer.setDefaultTimeout(env.getProperty("web.async.timeout", Long.TYPE, 120000L));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (HandlerInterceptor interceptor : interceptors) {
            registry.addInterceptor(interceptor);
        }
    }

    /**
     * Not inherited from {@link com.serotonin.m2m2.web.mvc.spring.MangoRootWebContextConfiguration}, must redefine.
     */
    @Bean(name="localeResolver")
    public MangoLocaleResolver getSessionLocaleResolver() {
        return new MangoLocaleResolver();
    }
}
