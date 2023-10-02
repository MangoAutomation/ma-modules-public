/*
 * Copyright (C) 2023 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import java.util.Date;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.ReturnCause;
import com.serotonin.m2m2.vo.event.EventInstanceI;

/**
 * @author Mert Cingöz
 */
public class BaseEventInstanceModel {

    private int id;
    private long activeTimestamp;
    private Integer acknowledgedByUserId;
    private String acknowledgedByUsername;
    private Long acknowledgedTimestamp;
    private TranslatableMessage alternateAckSource;
    private boolean rtnApplicable;
    private Long rtnTimestamp;
    private ReturnCause rtnCause;
    private AlarmLevels alarmLevel;
    private TranslatableMessage message;
    private TranslatableMessage rtnMessage;
    private Date activeDate;
    private Date acknowledgedDate;
    private Date rtnDate;

    public BaseEventInstanceModel() { }

    public BaseEventInstanceModel(EventInstanceI event) {
        super();
        this.id = event.getId();
        this.activeTimestamp = event.getActiveTimestamp();
        this.acknowledgedByUserId = event.getAcknowledgedByUserId();
        this.acknowledgedByUsername = event.getAcknowledgedByUsername();
        this.acknowledgedTimestamp = event.getAcknowledgedTimestamp();
        this.alternateAckSource = event.getAlternateAckSource();
        this.rtnApplicable = event.isRtnApplicable();
        this.rtnTimestamp = event.getRtnTimestamp();
        this.rtnCause = event.getRtnCause();
        this.alarmLevel = event.getAlarmLevel();
        this.message = event.getMessage();
        this.rtnMessage = event.getRtnMessage();

        this.activeDate = new Date(activeTimestamp);
        this.acknowledgedDate = acknowledgedTimestamp == null ? null : new Date(acknowledgedTimestamp);
        this.rtnDate = rtnTimestamp == null ? null : new Date(rtnTimestamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getActiveTimestamp() {
        return activeTimestamp;
    }

    public void setActiveTimestamp(long activeTimestamp) {
        this.activeTimestamp = activeTimestamp;
        this.activeDate = new Date(activeTimestamp);
    }

    public Integer getAcknowledgedByUserId() {
        return acknowledgedByUserId;
    }

    public void setAcknowledgedByUserId(Integer acknowledgedByUserId) {
        this.acknowledgedByUserId = acknowledgedByUserId;
    }

    public String getAcknowledgedByUsername() {
        return acknowledgedByUsername;
    }

    public void setAcknowledgedByUsername(String acknowledgedByUsername) {
        this.acknowledgedByUsername = acknowledgedByUsername;
    }

    public Long getAcknowledgedTimestamp() {
        return acknowledgedTimestamp;
    }

    public void setAcknowledgedTimestamp(Long acknowledgedTimestamp) {
        this.acknowledgedTimestamp = acknowledgedTimestamp;
        this.acknowledgedDate = acknowledgedTimestamp == null ? null : new Date(acknowledgedTimestamp);
    }

    public TranslatableMessage getAlternateAckSource() {
        return alternateAckSource;
    }

    public void setAlternateAckSource(TranslatableMessage alternateAckSource) {
        this.alternateAckSource = alternateAckSource;
    }

    public boolean isRtnApplicable() {
        return rtnApplicable;
    }

    public void setRtnApplicable(boolean rtnApplicable) {
        this.rtnApplicable = rtnApplicable;
    }

    public Long getRtnTimestamp() {
        return rtnTimestamp;
    }

    public void setRtnTimestamp(Long rtnTimestamp) {
        this.rtnTimestamp = rtnTimestamp;
        this.rtnDate = rtnTimestamp == null ? null : new Date(rtnTimestamp);
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

    public boolean isActive() {
        return rtnApplicable && rtnTimestamp == null;
    }

    public boolean isAcknowledged() {
        return acknowledgedTimestamp != null;
    }

    public TranslatableMessage getRtnMessage() {
        return rtnMessage;
    }

    public void setRtnMessage(TranslatableMessage rtnMessage) {
        this.rtnMessage = rtnMessage;
    }

    public Date getActiveDate() {
        return activeDate;
    }

    public Date getAcknowledgedDate() {
        return acknowledgedDate;
    }

    public Date getRtnDate() {
        return rtnDate;
    }
}
