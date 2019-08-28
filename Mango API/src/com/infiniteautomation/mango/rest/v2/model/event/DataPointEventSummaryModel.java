/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import java.util.Map;

import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 * @author Terry Packer
 *
 */
public class DataPointEventSummaryModel {
    private String xid;
    private Map<AlarmLevels, Integer> counts;
    
    public DataPointEventSummaryModel() { }
    
    public DataPointEventSummaryModel(String xid, Map<AlarmLevels, Integer> counts) {
        this.xid = xid;
        this.counts = counts;
    }
    
    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public Map<AlarmLevels, Integer> getCounts() {
        return counts;
    }

    public void setCounts(Map<AlarmLevels, Integer> counts) {
        this.counts = counts;
    }
}
