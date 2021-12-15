/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
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
public abstract class AbstractEventDetectorModel<T extends AbstractEventDetectorVO> extends AbstractVoModel<T> {
    public static final String DETECTOR_TYPE = "detectorType";

    //Hack so that we can import a list via CSV, I wasn't able to get the unwrapped ActionAndModel class to deserialize the model fields into 'model'
    @ApiModelProperty("Action to use for CSV import")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected VoAction action;
    @ApiModelProperty("Original XID for use in CSV import")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String originalXid;

    protected int sourceId;
    protected MangoPermissionModel readPermission;
    protected MangoPermissionModel editPermission;
    protected JsonNode data;

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
        this.readPermission = new MangoPermissionModel(vo.getReadPermission());
        this.editPermission = new MangoPermissionModel(vo.getEditPermission());

        this.data = vo.getData();
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
        vo.setReadPermission(readPermission != null ? readPermission.getPermission() : new MangoPermission());
        vo.setEditPermission(editPermission != null ? editPermission.getPermission() : new MangoPermission());
        vo.setData(data);
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
     */
    @JsonGetter("detectorType")
    public abstract String getDetectorType();

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public boolean isRtnApplicable() {
        return rtnApplicable;
    }
    public TranslatableMessage getDescription() {
        return description;
    }

    public MangoPermissionModel getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(MangoPermissionModel readPermission) {
        this.readPermission = readPermission;
    }

    public MangoPermissionModel getEditPermission() {
        return editPermission;
    }

    public void setEditPermission(MangoPermissionModel editPermission) {
        this.editPermission = editPermission;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public List<String> getHandlerXids() {
        return handlerXids;
    }

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
