/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * @author Terry Packer
 *
 */
public class EventTypeVOModel<T extends EventType, SOURCE_ONE, SOURCE_TWO> {

    protected AbstractEventTypeModel<T, SOURCE_ONE, SOURCE_TWO> type;
    private TranslatableMessage description;
    private AlarmLevels alarmLevel;

    private boolean supportsSubtype;
    private boolean supportsReferenceId1;
    private boolean supportsReferenceId2;
    
    public EventTypeVOModel(AbstractEventTypeModel<T, SOURCE_ONE, SOURCE_TWO> type, TranslatableMessage description, AlarmLevels alarmLevel, boolean supportsSubtype, boolean supportsTypeRef1, boolean supportsTypeRef2) {
        this.type = type;
        this.description = description;
        this.alarmLevel = alarmLevel;
        this.supportsSubtype = supportsSubtype;
        this.supportsReferenceId1 = supportsTypeRef1;
        this.supportsReferenceId2 = supportsTypeRef2;
    }
    
    public EventTypeVOModel(AbstractEventTypeModel<T, SOURCE_ONE, SOURCE_TWO> type, TranslatableMessage description, boolean supportsSubtype, boolean supportsTypeRef1, boolean supportsTypeRef2) {
        this.type = type;
        this.description = description;
        this.supportsSubtype = supportsSubtype; 
        this.supportsReferenceId1 = supportsTypeRef1;
        this.supportsReferenceId2 = supportsTypeRef2;
    }

    /**
     * @return the type
     */
    public AbstractEventTypeModel<T, SOURCE_ONE, SOURCE_TWO> getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AbstractEventTypeModel<T, SOURCE_ONE, SOURCE_TWO> type) {
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

    /**
     * @return the supportsSubtype
     */
    public boolean isSupportsSubtype() {
        return supportsSubtype;
    }
    
    /**
     * @param supportsSubtype the supportsSubtype to set
     */
    public void setSupportsSubtype(boolean supportsSubtype) {
        this.supportsSubtype = supportsSubtype;
    }
    
    /**
     * @return the supportsReferenceId1
     */
    public boolean isSupportsReferenceId1() {
        return supportsReferenceId1;
    }

    /**
     * @param supportsReferenceId1 the supportsReferenceId1 to set
     */
    public void setSupportsReferenceId1(boolean supportsReferenceId1) {
        this.supportsReferenceId1 = supportsReferenceId1;
    }

    /**
     * @return the supportsReferenceId2
     */
    public boolean isSupportsReferenceId2() {
        return supportsReferenceId2;
    }

    /**
     * @param supportsReferenceId2 the supportsReferenceId2 to set
     */
    public void setSupportsReferenceId2(boolean supportsReferenceId2) {
        this.supportsReferenceId2 = supportsReferenceId2;
    }
    
}
