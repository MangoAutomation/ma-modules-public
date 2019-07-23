/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;

/**
 * @author Terry Packer
 *
 */
public class DataPointEventSummaryModel {
    private String xid;
    private Map<AlarmLevels, Integer> counts;
    
    public DataPointEventSummaryModel() { }
    
    public DataPointEventSummaryModel(String xid) {
        this.xid = xid;
        this.counts = new HashMap<>();
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

    public void update(EventInstance instance) {
        counts.compute(instance.getAlarmLevel(), (level, count) ->{
            if(count == null) {
                return 1;
            }else {
                return count++;
            }
        });
    }
}
