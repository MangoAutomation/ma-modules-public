/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
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
