/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi.rootRest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * <ul>
 * <li>Swagger UI is provided via a webjar (defined in Core pom.xml as a dependency) and is somehow statically mapped to /swagger-ui.html</li>
 * <li>Swagger UI hits various hardcoded /swagger-resources/* URLs to retrieve information about which api-docs are available</li>
 * <li>/swagger-resources/* is provided via {@link springfox.documentation.swagger.web.ApiResourceController ApiResourceController} which is loaded via the root REST context</li>
 * <li>Each SwaggerConfig adds a SwaggerResource to the {@link com.infiniteautomation.mangoApi.rootRest.MangoRestSwaggerResourceProvider SwaggerResourceProvider} which
 * is where the ApiResourceController retrieves the list of resources from.</li>
 * <li>Each SwaggerConfig creates a {@link springfox.documentation.swagger2.web.Swagger2Controller Swagger2Controller} which serves up the swagger JSON</li>
 * <li>Each Swagger2Controller creates a swagger endpoint under the current dispatcher (e.g. /rest/latest/swagger/v2/api-docs). This is controlled via the
 * <code>springfox.documentation.swagger.v2.path</code> environment property.</li>
 * </ul>
 * @author Terry Packer
 * @author Jared Wiltshire
 */
@Configuration
@ConditionalOnProperty("${swagger.enabled:false}")
@EnableSwagger2
public class RootSwaggerConfig {

    @Primary
    @Bean
    public MangoRestSwaggerResourceProvider getMangoRestSwaggerResourceProvider() {
        return new MangoRestSwaggerResourceProvider();
    }
}
