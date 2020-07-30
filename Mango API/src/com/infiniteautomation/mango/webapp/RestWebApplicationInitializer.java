/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.webapp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.infiniteautomation.mangoApi.rootRest.RootRestDispatcherConfiguration;
import com.infiniteautomation.mangoApi.rootRest.RootSwaggerConfig;

/**
 * @author Jared Wiltshire
 */
@ConditionalOnProperty("${rest.enabled:true}")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RestWebApplicationInitializer implements WebApplicationInitializer {

    private final ApplicationContext parent;
    private final Environment env;

    @Autowired
    private RestWebApplicationInitializer(ApplicationContext parent, Environment env) {
        this.parent = parent;
        this.env = env;
    }

    /**
     * Note: When updating the dispatcher mappings, also update
     * <ul>
     * <li>{@link com.infiniteautomation.mango.rest.latest.MangoRestDispatcherConfiguration#CONTEXT_ID}</li>
     * <li>{@link com.infiniteautomation.mango.rest.latest.MangoRestDispatcherConfiguration#DISPATCHER_NAME}</li>
     * </ul>
     */
    @Override
    public void onStartup(ServletContext context) throws ServletException {
        boolean enableSwagger = env.getProperty("swagger.enabled", Boolean.class, false);

        //The REST configuration has a parent context from which all versions of the API
        // are children. This root rest context is defined here:
        AnnotationConfigWebApplicationContext rootRestContext = new AnnotationConfigWebApplicationContext();
        rootRestContext.setId(RootRestDispatcherConfiguration.CONTEXT_ID);
        rootRestContext.setParent(parent);
        rootRestContext.register(RootRestDispatcherConfiguration.class);

        if (enableSwagger) {
            rootRestContext.register(RootSwaggerConfig.class);
        }

        // Register and map the REST dispatcher servlet
        ServletRegistration.Dynamic rootRestDispatcher = context.addServlet(RootRestDispatcherConfiguration.DISPATCHER_NAME, new DispatcherServlet(rootRestContext));
        rootRestDispatcher.setLoadOnStartup(2);
        rootRestDispatcher.setAsyncSupported(true);
        // does not seem to be needed, leave just in case a Controller is registered in this dispatcher
        rootRestDispatcher.addMapping("/rest/*");

        if (enableSwagger) {
            rootRestDispatcher.addMapping(
                    "/swagger-resources/configuration/ui",
                    "/swagger-resources/configuration/security",
                    "/swagger-resources");
        }

        /**
         * REST latest
         */
        AnnotationConfigWebApplicationContext latestRestContext = new AnnotationConfigWebApplicationContext();
        latestRestContext.setId(com.infiniteautomation.mango.rest.latest.MangoRestDispatcherConfiguration.CONTEXT_ID);
        latestRestContext.setParent(rootRestContext);
        latestRestContext.register(com.infiniteautomation.mango.rest.latest.MangoRestDispatcherConfiguration.class);

        ServletRegistration.Dynamic latestRestDispatcher = context.addServlet(com.infiniteautomation.mango.rest.latest.MangoRestDispatcherConfiguration.DISPATCHER_NAME, new DispatcherServlet(latestRestContext));
        latestRestDispatcher.setLoadOnStartup(3);
        latestRestDispatcher.setAsyncSupported(true);

        latestRestDispatcher.addMapping("/rest/v3/*");
        latestRestDispatcher.addMapping("/rest/latest/*");
    }
}
