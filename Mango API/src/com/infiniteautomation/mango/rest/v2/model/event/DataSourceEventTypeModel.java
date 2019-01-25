/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */

public class DataSourceEventTypeModel extends AbstractEventTypeModel<DataSourceEventType> {
    
    private AlarmLevels alarmLevel;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AbstractDataSourceModel<?> dataSource;
    
    public DataSourceEventTypeModel() {
        super(new DataSourceEventType());
    }
    
    public DataSourceEventTypeModel(DataSourceEventType type) {
        super(type);
        alarmLevel = type.getAlarmLevel();
    }

    /**
     * @return the alarmLevel
     */
    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }
    
    /**
     * @param alarmLevel the alarmLevel to set
     */
    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
    
    /**
     * @return the dataSource
     */
    public AbstractDataSourceModel<?> getDataSource() {
        return dataSource;
    }
    
    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(AbstractDataSourceModel<?> dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public DataSourceEventType toVO() {
        return new DataSourceEventType(referenceId1, referenceId2 , alarmLevel, duplicateHandling);
    }
}
