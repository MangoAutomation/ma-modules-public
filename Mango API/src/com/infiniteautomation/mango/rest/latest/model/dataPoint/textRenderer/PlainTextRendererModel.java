/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.view.text.PlainRenderer;

/**
 * @author Terry Packer
 *
 */
public class PlainTextRendererModel extends ConvertingTextRendererModel<PlainRenderer>{

    @JsonProperty
    private String suffix;

    public PlainTextRendererModel(){ }
    public PlainTextRendererModel(PlainRenderer vo){
        fromVO(vo);
    }

    @Override
    public void fromVO(PlainRenderer vo) {
        super.fromVO(vo);
        this.suffix = vo.getSuffix();
    }

    @Override
    public PlainRenderer toVO() {
        PlainRenderer vo = super.toVO();
        vo.setSuffix(suffix);
        return vo;
    }

    @Override
    PlainRenderer newVO() {
        return new PlainRenderer();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String getType() {
        return PlainRenderer.getDefinition().getName();
    }
}
