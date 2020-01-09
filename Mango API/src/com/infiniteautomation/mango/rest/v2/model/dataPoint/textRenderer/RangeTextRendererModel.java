/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.view.text.RangeRenderer;
import com.serotonin.m2m2.view.text.RangeValue;

/**
 * @author Terry Packer
 *
 */
public class RangeTextRendererModel extends ConvertingTextRendererModel<RangeRenderer>{

    @JsonProperty
    private String format;
    @JsonProperty
    private List<RangeValue> rangeValues;

    public RangeTextRendererModel(){ }
    public RangeTextRendererModel(RangeRenderer vo){
        fromVO(vo);
    }

    @Override
    public void fromVO(RangeRenderer vo) {
        super.fromVO(vo);
        this.format = vo.getFormat();
        this.rangeValues = vo.getRangeValues();
    }

    @Override
    public RangeRenderer toVO() {
        RangeRenderer vo = super.toVO();
        vo.setFormat(format);
        vo.setRangeValues(rangeValues);
        return vo;
    }

    @Override
    RangeRenderer newVO() {
        return new RangeRenderer();
    }

    public List<RangeValue> getRangeValues() {
        return rangeValues;
    }

    public void setRangeValues(List<RangeValue> rangeValues) {
        this.rangeValues = rangeValues;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getType() {
        return RangeRenderer.getDefinition().getName();
    }
}
