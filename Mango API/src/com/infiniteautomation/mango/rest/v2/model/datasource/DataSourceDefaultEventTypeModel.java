/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 * A defined Event Type for a data source, with default alarm level
 * @author Terry Packer
 *
 */
public class DataSourceDefaultEventTypeModel {

    private int referenceId2;
    private String code;
    private String descriptionKey;
    private TranslatableMessage description;
    private AlarmLevels defaultAlarmLevel;
    /**
     * @return the referenceId2
     */
    public int getReferenceId2() {
        return referenceId2;
    }
    /**
     * @param referenceId2 the referenceId2 to set
     */
    public void setReferenceId2(int referenceId2) {
        this.referenceId2 = referenceId2;
    }
    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the descriptionKey
     */
    public String getDescriptionKey() {
        return descriptionKey;
    }
    /**
     * @param descriptionKey the descriptionKey to set
     */
    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
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
    /**
     * @return the defaultAlarmLevel
     */
    public AlarmLevels getDefaultAlarmLevel() {
        return defaultAlarmLevel;
    }
    /**
     * @param defaultAlarmLevel the defaultAlarmLevel to set
     */
    public void setDefaultAlarmLevel(AlarmLevels defaultAlarmLevel) {
        this.defaultAlarmLevel = defaultAlarmLevel;
    }
    
}
