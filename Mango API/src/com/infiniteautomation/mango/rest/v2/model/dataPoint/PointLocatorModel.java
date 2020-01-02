/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=PointLocatorModel.MODEL_TYPE)
public abstract class PointLocatorModel<T extends PointLocatorVO<T>> {

    public static final String MODEL_TYPE = "modelType";
    
    protected String dataType;
    protected boolean settable;
    protected boolean relinquishable;
    
    public PointLocatorModel() {
        
    }
    
    public PointLocatorModel(T locator) {
        fromVO(locator);
    }
    
    /**
     * Get the type of point locator
     * @return
     */
    @JsonGetter(MODEL_TYPE)
    abstract public  String getTypeName();
    
    /**
     * Convert from a point locator to this model
     * @param locator
     */
    public void fromVO(T locator) {
        this.dataType = DataTypes.CODES.getCode(locator.getDataTypeId());
        this.settable = locator.isSettable();
        this.relinquishable = locator.isRelinquishable();
    }
    
    /**
     * Convert from this model to a point locator
     * @return
     */
    abstract T toVO();

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public boolean isRelinquishable() {
        return relinquishable;
    }

    public void setRelinquishable(boolean relinquishable) {
        this.relinquishable = relinquishable;
    }

    public static String getModelType() {
        return MODEL_TYPE;
    }
    
}
