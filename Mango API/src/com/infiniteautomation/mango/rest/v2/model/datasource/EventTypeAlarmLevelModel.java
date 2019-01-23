/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;

import io.swagger.annotations.ApiModelProperty;

/**
 * Container to allow adjusting the alarm levels for a given data source's 
 * alarms.
 * 
 * @author Terry Packer
 *
 */
public class EventTypeAlarmLevelModel {
    
    @ApiModelProperty("Sub-type of DATA_SOURCE type event")
    private String eventType;
    
    @ApiModelProperty("How are duplicate events handled in order")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DuplicateHandling duplicateHandling;
    
    @ApiModelProperty("Alarm level to raise alarm")
    private AlarmLevels level;
    
    @ApiModelProperty("Description of this event")
    private TranslatableMessage description;

    public EventTypeAlarmLevelModel() { }
    
    /**
     * @param dataSourceXid
     * @param eventType
     * @param duplicateHandling
     * @param level
     * @param description
     */
    public EventTypeAlarmLevelModel(String dataSourceXid, String eventType,
            DuplicateHandling duplicateHandling, AlarmLevels level,
            TranslatableMessage description) {
        super();
        this.eventType = eventType;
        this.duplicateHandling = duplicateHandling;
        this.level = level;
        this.description = description;
    }

    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the duplicateHandling
     */
    public DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    /**
     * @param duplicateHandling the duplicateHandling to set
     */
    public void setDuplicateHandling(DuplicateHandling duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    /**
     * @return the level
     */
    public AlarmLevels getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(AlarmLevels level) {
        this.level = level;
    }

    /**
     * @return the description
     */
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(TranslatableMessage description) {
        this.description = description;
    }    
}
