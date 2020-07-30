/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.mbus.MBusPointLocatorVO;

import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.dataframes.MBusMedium;

/**
 *
 * @author Terry Packer
 */
public class MBusPointLocatorModel extends AbstractPointLocatorModel<MBusPointLocatorVO>{

    public static final String TYPE_NAME = "PL.MBUS";

    private MBusPointLocatorVO data;

    public MBusPointLocatorModel() {
        fromVO(new MBusPointLocatorVO());
    }
    public MBusPointLocatorModel(MBusPointLocatorVO vo) {
        fromVO(vo);
    }

    @Override
    public void fromVO(MBusPointLocatorVO vo) {
        super.fromVO(vo);
        this.data = vo;
    }

    @Override
    public MBusPointLocatorVO toVO() {
        return data;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }

    @JsonGetter("address")
    public byte getAddress() {
        return this.data.getAddress();
    }

    @JsonSetter("address")
    public void setAddress(byte address) {
        this.data.setAddress(address);
    }

    @JsonGetter("difCode")
    public String getDifCode() {
        return this.data.getDifCode();
    }

    @JsonSetter("difCode")
    public void setDifCode(String difCode) {
        this.data.setDifCode(difCode);
    }

    @JsonGetter("functionField")
    public String getFunctionField() {
        return this.data.getFunctionField();
    }

    @JsonSetter("functionField")
    public void setFunctionField(String functionField) {
        this.data.setFunctionField(functionField);
    }

    @JsonGetter("subUnit")
    public int getDeviceUnit() {
        return this.data.getSubUnit();
    }

    @JsonSetter("subUnit")
    public void setDeviceUnit(int subUnit) {
        this.data.setSubUnit(subUnit);
    }

    @JsonGetter("tariff")
    public int getTariff() {
        return this.data.getTariff();
    }

    @JsonSetter("tariff")
    public void setTariff(int tariff) {
        this.data.setTariff(tariff);
    }

    @JsonGetter("storageNumber")
    public long getStorageNumber() {
        return this.data.getStorageNumber();
    }

    @JsonSetter("storageNumber")
    public void setStorageNumber(long storageNumber) {
        this.data.setStorageNumber(storageNumber);
    }

    @JsonGetter("vifType")
    public String getVifType() {
        return this.data.getVifType();
    }

    @JsonSetter("vifType")
    public void setVifType(String vifType) {
        this.data.setVifType(vifType);
    }

    @JsonGetter("vifLabel")
    public String getVifLabel() {
        return this.data.getVifLabel();
    }

    @JsonSetter("vifLabel")
    public void setVifLabel(String vifLabel) {
        this.data.setVifLabel(vifLabel);
    }

    @JsonGetter("unitOfMeasurement")
    public String getUnitOfMeasurement() {
        return this.data.getUnitOfMeasurement();
    }

    @JsonSetter("unitOfMeasurement")
    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.data.setUnitOfMeasurement(unitOfMeasurement);
    }

    @JsonGetter("siPrefix")
    public String getSiPrefix() {
        return this.data.getSiPrefix();
    }

    @JsonSetter("siPrefix")
    public void setSiPrefix(String siPrefix) {
        this.data.setSiPrefix(siPrefix);
    }

    @JsonGetter("exponent")
    public Integer getExponent() {
        return this.data.getExponent();
    }

    @JsonSetter("exponent")
    public void setExponent(Integer exponent) {
        this.data.setExponent(exponent);
    }

    @JsonGetter("vifeLabels")
    public String[] getVifeLabels() {
        return this.data.getVifeLabels();
    }

    @JsonSetter("vifeLabels")
    public void setVifeLabels(String[] vifeLabels) {
        this.data.setVifeLabels(vifeLabels);
    }

    @JsonGetter("vifeTypes")
    public String[] getVifeTypes() {
        return this.data.getVifeTypes();
    }

    @JsonSetter("vifeTypes")
    public void setVifeTypes(String[] vifeTypes) {
        this.data.setVifeTypes(vifeTypes);
    }

    @JsonGetter("medium")
    public String getMedium() {
        return this.data.getMedium().label;
    }

    @JsonSetter("medium")
    public void setMedium(String medium) {
        this.data.setMedium(MBusMedium.fromLabel(medium));
    }

    @JsonGetter("responseFrame")
    public String getResponseFrame() {
        return this.data.getResponseFrame();
    }

    @JsonSetter("responseFrame")
    public void setResponseFrame(String responseFrame) {
        this.data.setResponseFrame(responseFrame);
    }

    @JsonGetter("version")
    public byte getVersion() {
        return this.data.getVersion();
    }

    @JsonSetter("version")
    public void setVersion(byte version) {
        this.data.setVersion(version);
    }

    @JsonGetter("identNumber")
    public int getIdentNumber() {
        return this.data.getIdentNumber();
    }

    @JsonSetter("identNumber")
    public void setIdentNumber(int identNumber) {
        this.data.setIdentNumber(identNumber);
    }

    @JsonGetter("manufacturer")
    public String getManufacturer() {
        return this.data.getManufacturer();
    }

    @JsonSetter("manufacturer")
    public void setManufacturer(String manufacturer) {
        this.data.setManufacturer(manufacturer);
    }

    @JsonGetter("addressing")
    public String getAddressing() {
        return this.data.getAddressing().getLabel();
    }

    @JsonSetter("addressing")
    public void setAddressing(String addressing) {
        this.data.setAddressing(MBusAddressing.fromLabel(addressing));
    }

    @JsonGetter("dbIndex")
    public int getDbIndex() {
        return this.data.getDbIndex();
    }

    @JsonSetter("dbIndex")
    public void setDbIndex(int dbIndex) {
        this.data.setDbIndex(dbIndex);
    }

    @JsonGetter("effectiveSiPrefix")
    public String getEffectiveSiPrefix() {
        return this.data.getEffectiveSiPrefix();
    }

    @JsonSetter("effectiveSiPrefix")
    public void setEffectiveSiPrefix(String effectiveSiPrefix) {
        this.data.setEffectiveSiPrefix(effectiveSiPrefix);
    }

}
