/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
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
