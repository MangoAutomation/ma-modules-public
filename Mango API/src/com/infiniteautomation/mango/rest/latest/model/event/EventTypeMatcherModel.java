/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.EventTypeMatcher;

public class EventTypeMatcherModel {

    protected String eventType;
    protected String subType;
    protected Integer referenceId1;
    protected Integer referenceId2;

    public EventTypeMatcherModel() { }

    public EventTypeMatcherModel(EventTypeMatcher matcher) {
        fromVO(matcher);
    }

    public void fromVO(EventTypeMatcher matcher) {
        this.eventType = matcher.getEventType();
        this.subType = matcher.getEventSubtype();
        this.referenceId1 = matcher.getReferenceId1();
        this.referenceId2 = matcher.getReferenceId2();
    }

    public EventTypeMatcher toVO() {
        return new EventTypeMatcher(eventType, subType, referenceId1, referenceId2);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Integer getReferenceId1() {
        return referenceId1;
    }

    public void setReferenceId1(Integer referenceId1) {
        this.referenceId1 = referenceId1;
    }

    public Integer getReferenceId2() {
        return referenceId2;
    }

    public void setReferenceId2(Integer referenceId2) {
        this.referenceId2 = referenceId2;
    }
}
