/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.classmate.TypeResolver;
import com.serotonin.m2m2.web.mvc.rest.swagger.MangoRestSwaggerResourceProvider;

import io.swagger.models.auth.In;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Terry Packer
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private final String SECURITY_TOKEN_REFERENCE = "Mango Token";
    
    private final TypeResolver typeResolver;
    
    @Autowired
    public SwaggerConfig(TypeResolver typeResolver, MangoRestSwaggerResourceProvider resourceProvider) {
        this.typeResolver = typeResolver;

        SwaggerResource v1 = new SwaggerResource();
        v1.setName("v1");
        v1.setUrl("/rest/v1/swagger/v2/api-docs");
        v1.setSwaggerVersion("2.0");
        resourceProvider.add(v1);
    }
    
    @Bean
    public Docket describe() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(AuthenticationPrincipal.class).select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()).build()
                .securitySchemes(Arrays.asList(new ApiKey(SECURITY_TOKEN_REFERENCE,
                        HttpHeaders.AUTHORIZATION, In.HEADER.name())))
                .securityContexts(Arrays.asList(securityContext()))
                //.pathProvider(new BasePathAwareRelativePathProvider("/rest"))
                .genericModelSubstitutes(ResponseEntity.class);

        docket.alternateTypeRules(
                new AlternateTypeRule(
                        typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                        typeResolver.resolve(WildcardType.class)),
                // Rule to allow Multipart requests to show up as single file input
                new AlternateTypeRule(typeResolver.resolve(MultipartHttpServletRequest.class),
                        typeResolver.resolve(MultipartFile.class)))

                .useDefaultResponseMessages(false);

        docket.apiInfo(new ApiInfoBuilder().title("Mango REST V1 API").description(
                "Support: <a href='http://infiniteautomation.com/forum'>Forum</a> or <a href='https://help-infinite-automation.squarespace.com/explore-the-api/'>Help</a>")
                .version("2.0").termsOfServiceUrl("https://infiniteautomation.com/terms/")
                .contact(new Contact("IAS", "https://infiniteautomation.com",
                        "support@infiniteautomation.com"))
                .license("Apache 2.0").licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build());
        return docket;
    }

    /**
     * Setup the security context to allow Tokens to test the API
     * 
     * @return
     */
    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth())
                .forPaths(PathSelectors.any()).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authScopes = new AuthorizationScope[0];
        SecurityReference securityReference = SecurityReference.builder()
                .reference(SECURITY_TOKEN_REFERENCE).scopes(authScopes).build();
        return Arrays.asList(securityReference);
    }
}
