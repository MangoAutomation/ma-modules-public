/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import com.serotonin.m2m2.view.text.BinaryTextRenderer;

/**
 * @author Terry Packer
 *
 */
public class BinaryTextRendererModel extends BaseTextRendererModel<BinaryTextRenderer>{

    private String zeroLabel;
    private String zeroColour;
    private String oneLabel;
    private String oneColour;

    public BinaryTextRendererModel() { }

    public BinaryTextRendererModel(BinaryTextRenderer vo){
        fromVO(vo);
    }

    @Override
    BinaryTextRenderer newVO() {
        return new BinaryTextRenderer();
    }

    @Override
    public void fromVO(BinaryTextRenderer vo) {
        this.zeroLabel = vo.getZeroLabel();
        this.zeroColour = vo.getZeroColour();
        this.oneLabel = vo.getOneLabel();
        this.oneColour = vo.getOneColour();
    }

    @Override
    public BinaryTextRenderer toVO() {
        BinaryTextRenderer vo = newVO();
        vo.setZeroLabel(zeroLabel);
        vo.setZeroColour(zeroColour);
        vo.setOneLabel(oneLabel);
        vo.setOneColour(oneColour);
        return vo;
    }

    public String getZeroLabel() {
        return zeroLabel;
    }

    public void setZeroLabel(String zeroLabel) {
        this.zeroLabel = zeroLabel;
    }

    public String getZeroColour() {
        return zeroColour;
    }

    public void setZeroColour(String zeroColour) {
        this.zeroColour = zeroColour;
    }

    public String getOneLabel() {
        return oneLabel;
    }

    public void setOneLabel(String oneLabel) {
        this.oneLabel = oneLabel;
    }

    public String getOneColour() {
        return oneColour;
    }

    public void setOneColour(String oneColour) {
        this.oneColour = oneColour;
    }

    @Override
    public String getType() {
        return BinaryTextRenderer.getDefinition().getName();
    }
}
