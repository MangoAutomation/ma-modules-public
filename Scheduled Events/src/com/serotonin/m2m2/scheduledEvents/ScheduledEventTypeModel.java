/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.scheduledEvents;

import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

/**
 *
 * @author Terry Packer
 */
public class ScheduledEventTypeModel extends EventTypeModel{

    private int scheduleId;
    private DuplicateHandling duplicateHandling = DuplicateHandling.IGNORE;

    public ScheduledEventTypeModel(){ }

    public ScheduledEventTypeModel(ScheduledEventType type){
        this.scheduleId = type.getReferenceId1();
        this.duplicateHandling = type.getDuplicateHandling();
    }



    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#getTypeName()
     */
    @Override
    public String getTypeName() {
        return ScheduledEventType.TYPE_NAME;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#isRateLimited()
     */
    @Override
    public boolean isRateLimited() {
        return false;
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return this.duplicateHandling;
    }

    public void setDuplicateHandling(DuplicateHandling duplicateHandling){
        this.duplicateHandling = duplicateHandling;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#getEventTypeInstance()
     */
    @Override
    public EventType toEventType() {
        return new ScheduledEventType(scheduleId, duplicateHandling);
    }

}
