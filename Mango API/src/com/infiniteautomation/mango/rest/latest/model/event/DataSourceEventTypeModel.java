/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;


/**
 * @author Terry Packer
 *
 */

public class DataSourceEventTypeModel extends AbstractEventTypeModel<DataSourceEventType, AbstractDataSourceModel<?>, String> {

    private AlarmLevels alarmLevel;

    public DataSourceEventTypeModel() {
        super(new DataSourceEventType());
    }

    public DataSourceEventTypeModel(DataSourceEventType type) {
        super(type);
        alarmLevel = type.getAlarmLevel();
    }

    public DataSourceEventTypeModel(DataSourceEventType type, AbstractDataSourceModel<?> reference1) {
        super(type, reference1);
        alarmLevel = type.getAlarmLevel();
    }

    public DataSourceEventTypeModel(DataSourceEventType type, AbstractDataSourceModel<?> reference1, String reference2) {
        super(type, reference1, reference2);
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
