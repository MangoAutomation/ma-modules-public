/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * @author Terry Packer
 *
 */
public class EventTypeVOModel<T extends EventType> {

    protected AbstractEventTypeModel<T> type;
    private TranslatableMessage description;
    private AlarmLevels alarmLevel;

    public EventTypeVOModel(AbstractEventTypeModel<T> type, TranslatableMessage description, AlarmLevels alarmLevel) {
        this.type = type;
        this.description = description;
        this.alarmLevel = alarmLevel;

    }

    /**
     * @return the type
     */
    public AbstractEventTypeModel<T> getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AbstractEventTypeModel<T> type) {
        this.type = type;
    }

    /**
     * @return the description
     */
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(TranslatableMessage description) {
        this.description = description;
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

}
