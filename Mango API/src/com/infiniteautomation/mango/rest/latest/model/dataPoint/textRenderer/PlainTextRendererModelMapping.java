/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.view.text.PlainRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class PlainTextRendererModelMapping implements RestModelJacksonMapping<PlainRenderer, PlainTextRendererModel> {

    @Override
    public Class<? extends PlainRenderer> fromClass() {
        return PlainRenderer.class;
    }

    @Override
    public Class<? extends PlainTextRendererModel> toClass() {
        return PlainTextRendererModel.class;
    }

    @Override
    public PlainTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new PlainTextRendererModel((PlainRenderer)from);
    }

    @Override
    public String getTypeName() {
        return PlainRenderer.getDefinition().getName();
    }

}
