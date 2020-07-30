/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.view.text.TimeRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class TimeTextRendererModelMapping implements RestModelJacksonMapping<TimeRenderer, TimeTextRendererModel> {

    @Override
    public Class<? extends TimeRenderer> fromClass() {
        return TimeRenderer.class;
    }

    @Override
    public Class<? extends TimeTextRendererModel> toClass() {
        return TimeTextRendererModel.class;
    }

    @Override
    public TimeTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new TimeTextRendererModel((TimeRenderer)from);
    }

    @Override
    public String getTypeName() {
        return TimeRenderer.getDefinition().getName();
    }

}
