/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.DataType;

/**
 *
 * @author Terry Packer
 */
public class SerialPointLocatorModel extends AbstractPointLocatorModel<SerialPointLocatorVO> {

    public static final String TYPE_NAME = "PL.SERIAL";

    private String pointIdentifier;
    private String valueRegex;
    private int valueIndex;

    public SerialPointLocatorModel() { }
    public SerialPointLocatorModel(SerialPointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public SerialPointLocatorVO toVO() {
        SerialPointLocatorVO vo = new SerialPointLocatorVO();
        vo.setDataType(DataType.fromName(dataType));
        vo.setPointIdentifier(pointIdentifier);
        vo.setValueRegex(valueRegex);
        vo.setValueIndex(valueIndex);

        return vo;
    }

    @Override
    public void fromVO(SerialPointLocatorVO locator) {
        super.fromVO(locator);
        this.pointIdentifier = locator.getPointIdentifier();
        this.valueRegex = locator.getValueRegex();
        this.valueIndex = locator.getValueIndex();
    }

    public String getPointIdentifier() {
        return pointIdentifier;
    }

    public void setPointIdentifier(String pointIdentifier) {
        this.pointIdentifier = pointIdentifier;
    }

    public String getValueRegex() {
        return valueRegex;
    }

    public void setValueRegex(String valueRegex) {
        this.valueRegex = valueRegex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }

}
