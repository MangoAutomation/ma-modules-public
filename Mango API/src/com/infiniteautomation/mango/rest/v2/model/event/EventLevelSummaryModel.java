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
    private int count;
    private EventInstanceModel latest;

    public EventLevelSummaryModel(){ }

    public EventLevelSummaryModel(AlarmLevels level, int unsilencedCount, EventInstanceModel latest) {
        this.level = level;
        this.count = unsilencedCount;
        this.latest = latest;
    }

    public AlarmLevels getLevel() {
        return level;
    }

    public void setLevel(AlarmLevels level) {
        this.level = level;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public EventInstanceModel getLatest() {
        return latest;
    }

    public void setLatest(EventInstanceModel latest) {
        this.latest = latest;
    }

}
