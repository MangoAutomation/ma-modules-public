/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.view.text.AnalogRenderer;

/**
 * @author Terry Packer
 *
 */
public class AnalogTextRendererModel extends ConvertingTextRendererModel<AnalogRenderer>{

    @JsonProperty
    private String format;
    @JsonProperty
    private String suffix;

    public AnalogTextRendererModel(){ }

    public AnalogTextRendererModel(AnalogRenderer vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(AnalogRenderer vo) {
        super.fromVO(vo);
        this.format = vo.getFormat();
        this.suffix = vo.getSuffix();
    }

    @Override
    public AnalogRenderer toVO() {
        AnalogRenderer vo = super.toVO();
        vo.setFormat(format);
        vo.setSuffix(suffix);
        return vo;
    }

    @Override
    AnalogRenderer newVO() {
        return new AnalogRenderer();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.SuperclassModel#getType()
     */
    @Override
    public String getType() {
        return AnalogRenderer.getDefinition().getName();
    }

}
