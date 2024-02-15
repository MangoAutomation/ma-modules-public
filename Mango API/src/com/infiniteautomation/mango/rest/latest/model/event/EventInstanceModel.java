/*
 * Copyright (C) 2023 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import java.util.List;

import com.infiniteautomation.mango.rest.latest.model.comment.UserCommentModel;
import com.serotonin.m2m2.vo.event.EventInstanceI;

/**
 * @author Terry Packer
 */
public class EventInstanceModel extends BaseEventInstanceModel {

    private AbstractEventTypeModel<?, ?, ?> eventType;
    private List<UserCommentModel> comments;

    //TODO We also have access to comments and handlers if necessary/desired

    public EventInstanceModel() { }


    public EventInstanceModel(EventInstanceI event, AbstractEventTypeModel<?, ?, ?> eventType, List<UserCommentModel> comments) {
        super(event);
        this.eventType = eventType;
        this.comments = comments;
    }

    public AbstractEventTypeModel<?, ?, ?> getEventType() {
        return eventType;
    }

    public void setEventType(AbstractEventTypeModel<?, ?, ?> eventType) {
        this.eventType = eventType;
    }

    public List<UserCommentModel> getComments() {
        return comments;
    }

    public void setComments(List<UserCommentModel> comments) {
        this.comments = comments;
    }
}
