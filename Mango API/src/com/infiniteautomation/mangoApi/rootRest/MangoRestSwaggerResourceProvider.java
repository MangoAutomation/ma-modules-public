/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mangoApi.rootRest;

import java.util.ArrayList;
import java.util.List;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * Register your swagger specs here to be exposed via the root view
 *
 * @author Terry Packer
 *
 */
public class MangoRestSwaggerResourceProvider implements SwaggerResourcesProvider {

    private final List<SwaggerResource> resources = new ArrayList<>();

    @Override
    public List<SwaggerResource> get() {
        return resources;
    }

    /**
     * Add a swagger resource definition
     */
    public void add(SwaggerResource resource) {
        this.resources.add(0, resource);
    }

}
