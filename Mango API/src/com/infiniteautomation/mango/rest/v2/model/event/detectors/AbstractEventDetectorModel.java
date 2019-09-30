/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventDetectorDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractEventDetectorModel.DETECTOR_TYPE)
public abstract class AbstractEventDetectorModel<T extends AbstractEventDetectorVO<T>> extends AbstractVoModel<T> {
    public static final String DETECTOR_TYPE = "detectorType";

    //Hack so that we can import a list via CSV, I wasn't able to get the unwrapped ActionAndModel class to deserialize the model fields into 'model'
    @ApiModelProperty("Action to use for CSV import")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected VoAction action;    
    @ApiModelProperty("Original XID for use in CSV import")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String originalXid;
    
    protected int sourceId;
    @ApiModelProperty("Read only description of detector")
    protected TranslatableMessage description;
    @ApiModelProperty("Read only indication if this detector supports return to normal")
    protected boolean rtnApplicable;
    protected AlarmLevels alarmLevel;
    protected String sourceTypeName;
    @ApiModelProperty("Xids for event handlers tied to this detector.  If supplied in the model it will replace any existing mappings.")
    protected List<String> handlerXids;
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.sourceId = vo.getSourceId();
        this.description = vo.getDescription();
        this.rtnApplicable = vo.isRtnApplicable();
        this.alarmLevel = vo.getAlarmLevel();
        this.sourceTypeName = vo.getDetectorSourceType();
        this.handlerXids = vo.getEventHandlerXids();
    }
    
    @Override
    public T toVO() {
        T vo = super.toVO();
        vo.setAlarmLevel(alarmLevel);
        if(handlerXids != null)
            vo.setEventHandlerXids(handlerXids);
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
    @JsonGetter("detectorType")
    public abstract String getDetectorType();
    
    /**
     * @return the sourceTypeName
     */
    public String getSourceTypeName() {
        return sourceTypeName;
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
    
    /**
     * @return the sourceId
     */
    public int getSourceId() {
        return sourceId;
    }
    
    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
    
    /**
     * @return the handlers
     */
    public List<String> getHandlerXids() {
        return handlerXids;
    }
    
    /**
     * @param handlers the handlers to set
     */
    public void setHandlerXids(List<String> handlers) {
        this.handlerXids = handlers;
    }

    public VoAction getAction() {
        return action;
    }

    public void setAction(VoAction action) {
        this.action = action;
    }

    public String getOriginalXid() {
        return originalXid;
    }

    public void setOriginalXid(String originalXid) {
        this.originalXid = originalXid;
    }
    
}
