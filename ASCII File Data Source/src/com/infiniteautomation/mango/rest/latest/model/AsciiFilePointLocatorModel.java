/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.DataTypes;

/**
 *
 * @author Terry Packer
 */
public class AsciiFilePointLocatorModel extends AbstractPointLocatorModel<AsciiFilePointLocatorVO> {

    public static final String TYPE_NAME = "PL.ASCII_FILE";

    private String pointIdentifier;
    private int pointIdentifierIndex;
    private int valueIndex;
    private String valueRegex;
    private boolean hasTimestamp;
    private int timestampIndex;
    private String timestampFormat;

    public AsciiFilePointLocatorModel() { }
    public AsciiFilePointLocatorModel(AsciiFilePointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(AsciiFilePointLocatorVO vo) {
        super.fromVO(vo);
        this.pointIdentifier = vo.getPointIdentifier();
        this.pointIdentifierIndex = vo.getPointIdentifierIndex();
        this.valueIndex = vo.getValueIndex();
        this.valueRegex = vo.getValueRegex();
        this.hasTimestamp = vo.getHasTimestamp();
        this.timestampIndex = vo.getTimestampIndex();
        this.timestampFormat = vo.getTimestampFormat();
    }

    @Override
    public AsciiFilePointLocatorVO toVO() {
        AsciiFilePointLocatorVO vo = new AsciiFilePointLocatorVO();
        vo.setDataType(DataTypes.CODES.getId(dataType));
        vo.setPointIdentifier(pointIdentifier);
        vo.setPointIdentifierIndex(pointIdentifierIndex);
        vo.setValueIndex(valueIndex);
        vo.setValueRegex(valueRegex);
        vo.setHasTimestamp(hasTimestamp);
        vo.setTimestampIndex(timestampIndex);
        vo.setTimestampFormat(timestampFormat);
        return vo;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }
    public String getPointIdentifier() {
        return pointIdentifier;
    }
    public void setPointIdentifier(String pointIdentifier) {
        this.pointIdentifier = pointIdentifier;
    }
    public int getPointIdentifierIndex() {
        return pointIdentifierIndex;
    }
    public void setPointIdentifierIndex(int pointIdentifierIndex) {
        this.pointIdentifierIndex = pointIdentifierIndex;
    }
    public int getValueIndex() {
        return valueIndex;
    }
    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }
    public String getValueRegex() {
        return valueRegex;
    }
    public void setValueRegex(String valueRegex) {
        this.valueRegex = valueRegex;
    }
    public boolean isHasTimestamp() {
        return hasTimestamp;
    }
    public void setHasTimestamp(boolean hasTimestamp) {
        this.hasTimestamp = hasTimestamp;
    }
    public int getTimestampIndex() {
        return timestampIndex;
    }
    public void setTimestampIndex(int timestampIndex) {
        this.timestampIndex = timestampIndex;
    }
    public String getTimestampFormat() {
        return timestampFormat;
    }
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

}
