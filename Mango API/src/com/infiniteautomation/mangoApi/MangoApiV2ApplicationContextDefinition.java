/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.infiniteautomation.mango.rest.v2.MangoRestDispatcherConfiguration;
import com.serotonin.m2m2.module.ApplicationContextDefinition;


/**
 * @author Terry Packer
 *
 */
public class MangoApiV2ApplicationContextDefinition extends ApplicationContextDefinition {

    @Override
    public void configure(ServletContext context,
            AnnotationConfigWebApplicationContext rootWebContext) {
        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext restDispatcherContext = new AnnotationConfigWebApplicationContext();
        restDispatcherContext.setId("restV2DispatcherContext");
        restDispatcherContext.setParent(rootWebContext);
        restDispatcherContext.register(MangoRestDispatcherConfiguration.class);

        // Register and map the REST dispatcher servlet
        ServletRegistration.Dynamic restDispatcher =
                context.addServlet("restV2DispatcherServlet", new DispatcherServlet(restDispatcherContext));
        restDispatcher.setLoadOnStartup(3);
        restDispatcher.addMapping("/rest/v2/*");

    }

}
