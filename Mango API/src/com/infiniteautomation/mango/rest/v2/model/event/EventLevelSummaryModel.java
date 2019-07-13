/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 * @author Terry Packer
 *
 */
public class EventLevelSummaryModel {

    private AlarmLevels level;
    private int unsilencedCount;
    private EventInstanceModel mostRecentUnsilenced;
    
    public EventLevelSummaryModel(){ }

    public EventLevelSummaryModel(AlarmLevels level, int unsilencedCount, EventInstanceModel mostRecentUnsilenced) {
        this.level = level;
        this.unsilencedCount = unsilencedCount;
        this.mostRecentUnsilenced = mostRecentUnsilenced;
    }

    public AlarmLevels getLevel() {
        return level;
    }

    public void setLevel(AlarmLevels level) {
        this.level = level;
    }

    public int getUnsilencedCount() {
        return unsilencedCount;
    }

    public void setUnsilencedCount(int unsilencedCount) {
        this.unsilencedCount = unsilencedCount;
    }

    public EventInstanceModel getMostRecentUnsilenced() {
        return mostRecentUnsilenced;
    }

    public void setMostRecentUnsilenced(EventInstanceModel mostRecentUnsilenced) {
        this.mostRecentUnsilenced = mostRecentUnsilenced;
    }
    
}
