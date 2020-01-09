/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.MultistateValue;

/**
 * @author Terry Packer
 *
 */
public class MultistateTextRendererModel extends BaseTextRendererModel<MultistateRenderer>{

    @JsonProperty
    private List<MultistateValue> multistateValues = new ArrayList<MultistateValue>();

    public MultistateTextRendererModel(){ }

    public MultistateTextRendererModel(MultistateRenderer renderer){
        fromVO(renderer);
    }

    @Override
    MultistateRenderer newVO() {
        return new MultistateRenderer();
    }

    @Override
    public void fromVO(MultistateRenderer vo) {
        this.multistateValues = vo.getMultistateValues();
    }

    @Override
    public MultistateRenderer toVO() {
        MultistateRenderer vo = newVO();
        vo.setMultistateValues(multistateValues);
        return vo;
    }

    public List<MultistateValue> getMultistateValues() {
        return multistateValues;
    }

    public void setMultistateValues(List<MultistateValue> multistateValues) {
        this.multistateValues = multistateValues;
    }

    @Override
    public String getType() {
        return MultistateRenderer.getDefinition().getName();
    }
}
