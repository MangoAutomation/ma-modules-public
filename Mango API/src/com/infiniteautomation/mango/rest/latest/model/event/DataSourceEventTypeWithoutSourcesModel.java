/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;


/**
 * @author Terry Packer
 *
 */

public class DataSourceEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<DataSourceEventType> {

    private AlarmLevels alarmLevel;

    public DataSourceEventTypeWithoutSourcesModel() {
        super(new DataSourceEventType());
    }

    public DataSourceEventTypeWithoutSourcesModel(DataSourceEventType type) {
        super(type);
        alarmLevel = type.getAlarmLevel();
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    @Override
    public DataSourceEventType toVO() {
        return new DataSourceEventType(referenceId1, referenceId2 , alarmLevel, duplicateHandling);
    }
}
