/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.text.ConvertingRenderer;

/**
 * @author Terry Packer
 *
 */
public abstract class ConvertingTextRendererModel<T extends ConvertingRenderer> extends BaseTextRendererModel<T>{

    private boolean useUnitAsSuffix;
    private String unit;
    private String renderedUnit;

    public ConvertingTextRendererModel(){ }

    public ConvertingTextRendererModel(T vo) {
        super(vo);
    }

    @Override
    public void fromVO(T vo) {
        this.useUnitAsSuffix = vo.isUseUnitAsSuffix();
        this.unit = UnitUtil.formatLocal(vo.getUnit());
        this.renderedUnit = UnitUtil.formatLocal(vo.getRenderedUnit());
    }

    @Override
    public T toVO() {
        T vo = newVO();
        vo.setUseUnitAsSuffix(useUnitAsSuffix);
        vo.setUnit(UnitUtil.parseLocal(unit));
        vo.setRenderedUnit(UnitUtil.parseLocal(renderedUnit));
        return vo;
    }

    public boolean isUseUnitAsSuffix() {
        return useUnitAsSuffix;
    }

    public void setUseUnitAsSuffix(boolean useUnitAsSuffix) {
        this.useUnitAsSuffix = useUnitAsSuffix;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRenderedUnit() {
        return renderedUnit;
    }

    public void setRenderedUnit(String renderedUnit) {
        this.renderedUnit = renderedUnit;
    }
}
