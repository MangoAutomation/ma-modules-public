/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;

/**
 * @author Terry Packer
 *
 */

public class DataSourceEventTypeModel extends AbstractEventTypeModel<DataSourceEventType> {
    
    //TODO Do we need this?
    private AlarmLevels alarmLevel;
    
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
    
    @Override
    public DataSourceEventType toVO() {
        return new DataSourceEventType(referenceId1, referenceId2 , alarmLevel, duplicateHandling);
    }
}
