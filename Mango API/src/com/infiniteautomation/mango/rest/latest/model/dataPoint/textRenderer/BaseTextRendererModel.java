/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.view.text.BaseTextRenderer;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=BaseTextRendererModel.TYPE)
public abstract class BaseTextRendererModel<T extends BaseTextRenderer> {

    public static final String TYPE = "type";

    public BaseTextRendererModel() {

    }

    public BaseTextRendererModel(T vo) {
        fromVO(vo);
    }

    /**
     * Create a new VO
     */
    abstract T newVO();

    /**
     * Get the model type
     */
    abstract public String getType();

    /**
     * Convert from VO
     */
    abstract public void fromVO(T vo);

    /**
     * Convert to VO
     */
    abstract public T toVO();
}
