/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
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
     * @return
     */
    abstract T newVO();

    /**
     * Get the model type
     * @return
     */
    abstract public String getType();

    /**
     * Convert from VO
     * @param vo
     */
    abstract public void fromVO(T vo);

    /**
     * Convert to VO
     * @return
     */
    abstract public T toVO();
}
