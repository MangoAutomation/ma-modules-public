/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogTextRendererModelMapping implements RestModelJacksonMapping<AnalogRenderer, AnalogTextRendererModel> {

    @Override
    public Class<? extends AnalogRenderer> fromClass() {
        return AnalogRenderer.class;
    }

    @Override
    public Class<? extends AnalogTextRendererModel> toClass() {
        return AnalogTextRendererModel.class;
    }

    @Override
    public AnalogTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AnalogTextRendererModel((AnalogRenderer)from);
    }

    @Override
    public String getTypeName() {
        return AnalogRenderer.getDefinition().getName();
    }

}
