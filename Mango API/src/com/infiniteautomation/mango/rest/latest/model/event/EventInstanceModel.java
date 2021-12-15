/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.infiniteautomation.mango.rest.latest.model.comment.UserCommentModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.ReturnCause;

import java.util.Date;
import java.util.List;

/**
 * @author Terry Packer
 */
public class EventInstanceModel {

    private int id;
    private AbstractEventTypeModel<?, ?, ?> eventType;
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
    private List<UserCommentModel> comments;
    private Date activeDate;
    private Date acknowledgedDate;
    private Date rtnDate;

    //TODO We also have access to comments and handlers if necessary/desired

    public EventInstanceModel() {
    }

    /**
     */
    public EventInstanceModel(int id, AbstractEventTypeModel<?, ?, ?> eventType,
                              long activeTimestamp, Integer acknowledgedByUserId, String acknowledgedByUsername,
                              Long acknowledgedTimestamp, TranslatableMessage alternateAckSource, boolean rtnApplicable, Long rtnTimestamp,
                              ReturnCause rtnCause, AlarmLevels alarmLevel, TranslatableMessage message, TranslatableMessage rtnMessage, List<UserCommentModel> comments) {
        super();
        this.id = id;
        this.eventType = eventType;
        this.activeTimestamp = activeTimestamp;
        this.acknowledgedByUserId = acknowledgedByUserId;
        this.acknowledgedByUsername = acknowledgedByUsername;
        this.acknowledgedTimestamp = acknowledgedTimestamp;
        this.alternateAckSource = alternateAckSource;
        this.rtnApplicable = rtnApplicable;
        this.rtnTimestamp = rtnTimestamp;
        this.rtnCause = rtnCause;
        this.alarmLevel = alarmLevel;
        this.message = message;
        this.rtnMessage = rtnMessage;
        this.comments = comments;

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

    public List<UserCommentModel> getComments() {
        return comments;
    }

    public void setComments(List<UserCommentModel> comments) {
        this.comments = comments;
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
