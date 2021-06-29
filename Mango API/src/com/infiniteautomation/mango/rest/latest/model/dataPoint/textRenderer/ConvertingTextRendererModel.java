/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.dataPoint.textRenderer;

import com.serotonin.m2m2.view.text.ConvertingRenderer;

/**
 * @author Terry Packer
 *
 */
public abstract class ConvertingTextRendererModel<T extends ConvertingRenderer> extends BaseTextRendererModel<T>{

    private boolean useUnitAsSuffix;
    public ConvertingTextRendererModel(){ }

    public ConvertingTextRendererModel(T vo) {
        super(vo);
    }

    @Override
    public void fromVO(T vo) {
        this.useUnitAsSuffix = vo.isUseUnitAsSuffix();
    }

    @Override
    public T toVO() {
        T vo = newVO();
        vo.setUseUnitAsSuffix(useUnitAsSuffix);
        return vo;
    }

    public boolean isUseUnitAsSuffix() {
        return useUnitAsSuffix;
    }

    public void setUseUnitAsSuffix(boolean useUnitAsSuffix) {
        this.useUnitAsSuffix = useUnitAsSuffix;
    }
}
