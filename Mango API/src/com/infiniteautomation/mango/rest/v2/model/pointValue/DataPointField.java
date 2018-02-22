/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Information that can optionally be returned 
 * in point value query results.
 * @author Terry Packer
 */
public enum DataPointField {
    NAME("name"),
    DEVICE_NAME("deviceName"),
    DATA_SOURCE_NAME("dataSourceName");
    
    private final String fieldName;
    private DataPointField(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * @param vo
     * @return
     */
    public String getFieldValue(DataPointVO vo) {
        switch(this) {
            case NAME:
                return vo.getName();
            case DEVICE_NAME:
                return vo.getDeviceName();
            case DATA_SOURCE_NAME:
                return vo.getDataSourceName();
            default:
                throw new ShouldNeverHappenException("Unknown data point field.");
        }
    }
}
