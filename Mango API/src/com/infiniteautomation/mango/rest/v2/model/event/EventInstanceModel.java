/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.ReturnCause;

/**
 * @author Terry Packer
 *
 */
public class EventInstanceModel {

    private int id;
    private AbstractEventTypeModel<?,?,?> eventType;
    private long activeTimestamp;
    private int acknowledgedByUserId;
    private String acknowledgedByUsername;
    private long acknowledgedTimestamp;
    private boolean rtnApplicable;
    private long rtnTimestamp;
    private ReturnCause rtnCause;
    private AlarmLevels alarmLevel;
    private TranslatableMessage message;

    //TODO We also have access to comments and handlers if necessary/desired

    public EventInstanceModel() { }

    /**
     * @param id
     * @param eventType
     * @param activeTimestamp
     * @param acknowledgedByUserId
     * @param acknowledgedByUsername
     * @param acknowledgedTimestamp
     * @param rtnApplicable
     * @param rtnTimestamp
     * @param rtnCause
     * @param alarmLevel
     * @param message
     */
    public EventInstanceModel(int id, AbstractEventTypeModel<?, ?, ?> eventType,
            long activeTimestamp, int acknowledgedByUserId, String acknowledgedByUsername,
            long acknowledgedTimestamp, boolean rtnApplicable, long rtnTimestamp,
            ReturnCause rtnCause, AlarmLevels alarmLevel, TranslatableMessage message) {
        super();
        this.id = id;
        this.eventType = eventType;
        this.activeTimestamp = activeTimestamp;
        this.acknowledgedByUserId = acknowledgedByUserId;
        this.acknowledgedByUsername = acknowledgedByUsername;
        this.acknowledgedTimestamp = acknowledgedTimestamp;
        this.rtnApplicable = rtnApplicable;
        this.rtnTimestamp = rtnTimestamp;
        this.rtnCause = rtnCause;
        this.alarmLevel = alarmLevel;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AbstractEventTypeModel<?, ?, ?> getEventType() {
        return eventType;
    }

    public void setEventType(AbstractEventTypeModel<?, ?, ?> eventType) {
        this.eventType = eventType;
    }

    public long getActiveTimestamp() {
        return activeTimestamp;
    }

    public void setActiveTimestamp(long activeTimestamp) {
        this.activeTimestamp = activeTimestamp;
    }

    public int getAcknowledgedByUserId() {
        return acknowledgedByUserId;
    }

    public void setAcknowledgedByUserId(int acknowledgedByUserId) {
        this.acknowledgedByUserId = acknowledgedByUserId;
    }

    public String getAcknowledgedByUsername() {
        return acknowledgedByUsername;
    }

    public void setAcknowledgedByUsername(String acknowledgedByUsername) {
        this.acknowledgedByUsername = acknowledgedByUsername;
    }

    public long getAcknowledgedTimestamp() {
        return acknowledgedTimestamp;
    }

    public void setAcknowledgedTimestamp(long acknowledgedTimestamp) {
        this.acknowledgedTimestamp = acknowledgedTimestamp;
    }

    public boolean isRtnApplicable() {
        return rtnApplicable;
    }

    public void setRtnApplicable(boolean rtnApplicable) {
        this.rtnApplicable = rtnApplicable;
    }

    public long getRtnTimestamp() {
        return rtnTimestamp;
    }

    public void setRtnTimestamp(long rtnTimestamp) {
        this.rtnTimestamp = rtnTimestamp;
    }

    public ReturnCause getRtnCause() {
        return rtnCause;
    }

    public void setRtnCause(ReturnCause rtnCause) {
        this.rtnCause = rtnCause;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public TranslatableMessage getMessage() {
        return message;
    }

    public void setMessage(TranslatableMessage message) {
        this.message = message;
    }
}
