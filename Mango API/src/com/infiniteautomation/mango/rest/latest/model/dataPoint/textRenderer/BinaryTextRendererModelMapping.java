/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class BinaryTextRendererModelMapping implements RestModelJacksonMapping<BinaryTextRenderer, BinaryTextRendererModel> {

    @Override
    public Class<? extends BinaryTextRenderer> fromClass() {
        return BinaryTextRenderer.class;
    }

    @Override
    public Class<? extends BinaryTextRendererModel> toClass() {
        return BinaryTextRendererModel.class;
    }

    @Override
    public BinaryTextRendererModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new BinaryTextRendererModel((BinaryTextRenderer)from);
    }

    @Override
    public String getTypeName() {
        return BinaryTextRenderer.getDefinition().getName();
    }

}
