/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.classmate.TypeResolver;
import com.infiniteautomation.mango.rest.swagger.MangoRestSwaggerResourceProvider;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;

import io.swagger.models.auth.In;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
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
public class SwaggerV2Config {
    private final String SECURITY_TOKEN_REFERENCE = "Mango Token";
    
    private final TypeResolver typeResolver;
    private final Set<String> defaultMediaTypes = new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON_UTF8_VALUE));

    @Autowired
    public SwaggerV2Config(TypeResolver typeResolver, MangoRestSwaggerResourceProvider resourceProvider) {
        this.typeResolver = typeResolver;
        
        SwaggerResource v2 = new SwaggerResource();
        v2.setName("Mango API v2");
        String url = Common.envProps.getString("springfox.documentation.swagger.v2.path", "/swagger/v2/api-docs");
        v2.setUrl("/rest/v2" + url);
        v2.setSwaggerVersion("2.0");
        resourceProvider.get().add(0,v2);
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
                .produces(defaultMediaTypes)
                .consumes(defaultMediaTypes)
                .genericModelSubstitutes(ResponseEntity.class);

        docket.alternateTypeRules(
                AlternateTypeRules.newRule(
                        typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                        typeResolver.resolve(WildcardType.class)),
                // Rule to allow Multipart requests to show up as single file input
                AlternateTypeRules.newRule(typeResolver.resolve(MultipartHttpServletRequest.class),
                        typeResolver.resolve(MultipartFile.class)),
                //Setup Translatable Messages to appear as Strings
                AlternateTypeRules.newRule(TranslatableMessage.class, String.class),
                //Setup Lists of Translatable Messages to appear as Lists of Strings
                AlternateTypeRules.newRule(typeResolver.resolve(List.class, TranslatableMessage.class), typeResolver.resolve(List.class, String.class)),
                //Setup Sets of Translatable Messages to appear as Sets of Strings
                AlternateTypeRules.newRule(typeResolver.resolve(Set.class, TranslatableMessage.class), typeResolver.resolve(Set.class, String.class)))
                .useDefaultResponseMessages(false);

        docket.apiInfo(new ApiInfoBuilder().title("Mango REST V2 API").description(
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
