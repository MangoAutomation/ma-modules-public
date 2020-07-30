/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MultistateTextRendererModelMapping implements RestModelJacksonMapping<MultistateRenderer, MultistateTextRendererModel> {

    @Override
    public Class<? extends MultistateRenderer> fromClass() {
        return MultistateRenderer.class;
    }

    @Override
    public Class<? extends MultistateTextRendererModel> toClass() {
        return MultistateTextRendererModel.class;
    }

    @Override
    public MultistateTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MultistateTextRendererModel((MultistateRenderer)from);
    }

    @Override
    public String getTypeName() {
        return MultistateRenderer.getDefinition().getName();
    }

}
