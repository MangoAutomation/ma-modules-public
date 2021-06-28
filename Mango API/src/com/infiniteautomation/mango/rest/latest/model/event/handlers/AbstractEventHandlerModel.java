/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.latest.model.event.EventTypeMatcherModel;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractEventHandlerModel.HANDLER_TYPE)
public abstract class AbstractEventHandlerModel<T extends AbstractEventHandlerVO> extends AbstractVoModel<T> {

    public static final String HANDLER_TYPE = "handlerType";

    private boolean disabled;
    private List<EventTypeMatcherModel> eventTypes;
    protected MangoPermissionModel readPermission;
    protected MangoPermissionModel editPermission;

    public AbstractEventHandlerModel() { }

    public AbstractEventHandlerModel(T vo) {
        fromVO(vo);
    }

    /**
     * The type info for the model
     * @return
     */
    public abstract String getHandlerType();

    /**
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    /**
     * @return the eventTypes
     */
    public List<EventTypeMatcherModel> getEventTypes() {
        return eventTypes;
    }
    /**
     * @param eventTypes the eventTypes to set
     */
    public void setEventTypes(List<EventTypeMatcherModel> eventTypes) {
        this.eventTypes = eventTypes;
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

    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.disabled = vo.isDisabled();
        this.readPermission = new MangoPermissionModel(vo.getReadPermission());
        this.editPermission = new MangoPermissionModel(vo.getEditPermission());
        this.eventTypes = vo.getEventTypes().stream()
                .map(EventTypeMatcherModel::new)
                .collect(Collectors.toList());
    }

    @Override
    public void readInto(T vo) {
        super.readInto(vo);
        vo.setDisabled(disabled);
        vo.setReadPermission(readPermission != null ? readPermission.getPermission() : new MangoPermission());
        vo.setEditPermission(editPermission != null ? editPermission.getPermission() : new MangoPermission());
        if (eventTypes != null) {
            vo.setEventTypes(eventTypes.stream()
                    .map(EventTypeMatcherModel::toVO)
                    .collect(Collectors.toList()));
        }
    }
}
