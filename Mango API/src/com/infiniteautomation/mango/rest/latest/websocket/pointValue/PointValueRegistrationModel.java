/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.pointValue;

import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class PointValueRegistrationModel {

    @JsonProperty("xid")
    private String dataPointXid; //Data point to register against

    @JsonProperty("eventTypes")
    private Set<PointValueEventType> eventTypes; //Events to listen for


    public PointValueRegistrationModel(){
    }

    public String getDataPointXid() {
        return dataPointXid;
    }

    public void setDataPointXid(String dataPointXid) {
        this.dataPointXid = dataPointXid;
    }

    public Set<PointValueEventType> getEventTypes() {
        return eventTypes == null ? EnumSet.noneOf(PointValueEventType.class) : eventTypes;
    }

    public void setEventTypes(Set<PointValueEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

}
