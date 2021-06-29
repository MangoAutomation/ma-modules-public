/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.view.text.NoneRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class NoneTextRendererModelMapping implements RestModelJacksonMapping<NoneRenderer, NoneTextRendererModel> {

    @Override
    public Class<? extends NoneRenderer> fromClass() {
        return NoneRenderer.class;
    }

    @Override
    public Class<? extends NoneTextRendererModel> toClass() {
        return NoneTextRendererModel.class;
    }

    @Override
    public NoneTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new NoneTextRendererModel((NoneRenderer)from);
    }

    @Override
    public String getTypeName() {
        return NoneRenderer.getDefinition().getName();
    }

}
