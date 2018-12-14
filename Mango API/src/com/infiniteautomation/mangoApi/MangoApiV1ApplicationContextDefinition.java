/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.ApplicationContextDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.MangoRestDispatcherConfiguration;
import com.serotonin.m2m2.web.mvc.rest.v1.SwaggerConfig;


/**
 * @author Terry Packer
 *
 */
public class MangoApiV1ApplicationContextDefinition extends ApplicationContextDefinition {

    @Override
    public void configure(ServletContext context, AnnotationConfigWebApplicationContext rootWebContext, AnnotationConfigWebApplicationContext rootRestContext) {
        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext restDispatcherContext = new AnnotationConfigWebApplicationContext();
        restDispatcherContext.setId("restV1DispatcherContext");
        restDispatcherContext.setParent(rootRestContext);
        restDispatcherContext.register(MangoRestDispatcherConfiguration.class);

        // Register and map the REST dispatcher servlet
        ServletRegistration.Dynamic restDispatcher =
                context.addServlet("restV1DispatcherServlet", new DispatcherServlet(restDispatcherContext));
        restDispatcher.setLoadOnStartup(3);
        restDispatcher.addMapping("/rest/v1/*");

        boolean enableSwagger = Common.envProps.getBoolean("swagger.enabled", false);
        if(enableSwagger)
            restDispatcherContext.register(SwaggerConfig.class);
        
    }

}
