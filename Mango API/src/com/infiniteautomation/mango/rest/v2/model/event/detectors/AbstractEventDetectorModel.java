/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventDetectorDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;

/**
 * @author Terry Packer
 *
 */
//TODO Add subtypes @ApiModel(subTypes= {EmailEventHandlerModel.class, SetPointEventHandlerModel.class, ProcessEventHandlerModel.class}, discriminator="handlerType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=AbstractEventDetectorModel.DETECTOR_TYPE)
public abstract class AbstractEventDetectorModel<T extends AbstractEventDetectorVO<T>> extends AbstractVoModel<T> {
    public static final String DETECTOR_TYPE = "detectorType";

    protected int sourceId;
    protected TranslatableMessage description;
    protected boolean rtnApplicable;
    protected AlarmLevels alarmLevel;
    protected String detectorSourceType;
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.sourceId = vo.getSourceId();
        this.description = vo.getDescription();
        this.rtnApplicable = vo.isRtnApplicable();
        this.alarmLevel = vo.getAlarmLevel();
        this.detectorSourceType = vo.getDetectorSourceType();
    }
    
    @Override
    public T toVO() {
        T vo = super.toVO();
        vo.setAlarmLevel(alarmLevel);
        return vo;
    }
    
    @Override
    protected T newVO() {
        EventDetectorDefinition<T> def = getDefinition();
        return def.baseCreateEventDetectorVO(sourceId);
    }
    
    @JsonIgnore
    public EventDetectorDefinition<T> getDefinition() {
        EventDetectorDefinition<T> definition = ModuleRegistry.getEventDetectorDefinition(getDetectorType());
        if(definition == null)
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("rest.exception.modelNotFound", getDetectorType()));
        return definition;
    }    
    
    /**
     * The type name of our module element definition
     * @return
     */
    public abstract String getDetectorType();
    
    /**
     * Our source type name
     * @return
     */
    public String getDetectorSourceType(){
        return detectorSourceType;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
    
    public int getSourceId() {
        return sourceId;
    }
    
    public boolean isRtnApplicable() {
        return rtnApplicable;
    }
    public TranslatableMessage getDescription() {
        return description;
    }

    /**
     * @return the alarmLevel
     */
    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    /**
     * @param alarmLevel the alarmLevel to set
     */
    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
    
    //TODO List of Event Handlers
}
