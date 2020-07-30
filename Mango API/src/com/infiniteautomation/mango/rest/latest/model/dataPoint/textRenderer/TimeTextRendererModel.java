/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.view.text.TimeRenderer;

/**
 * @author Terry Packer
 *
 */
public class TimeTextRendererModel extends BaseTextRendererModel<TimeRenderer>{

    @JsonProperty
    private String format;
    @JsonProperty
    private int conversionExponent;

    public TimeTextRendererModel(){ }

    public TimeTextRendererModel(TimeRenderer vo) {
        fromVO(vo);
    }

    @Override
    TimeRenderer newVO() {
        return new TimeRenderer();
    }

    @Override
    public void fromVO(TimeRenderer vo) {
        this.format = vo.getFormat();
        this.conversionExponent = vo.getConversionExponent();
    }

    @Override
    public TimeRenderer toVO() {
        TimeRenderer vo = newVO();
        vo.setFormat(format);
        vo.setConversionExponent(conversionExponent);
        return vo;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getConversionExponent() {
        return conversionExponent;
    }

    public void setConversionExponent(int conversionExponent) {
        this.conversionExponent = conversionExponent;
    }

    @Override
    public String getType() {
        return TimeRenderer.getDefinition().getName();
    }
}
