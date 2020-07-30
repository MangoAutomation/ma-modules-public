/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.latest.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.latest.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractEventHandlerModel.HANDLER_TYPE)
public abstract class AbstractEventHandlerModel<T extends AbstractEventHandlerVO> extends AbstractVoModel<T> {

    public static final String HANDLER_TYPE = "handlerType";

    private boolean disabled;
    private List<AbstractEventTypeModel<?,?, ?>> eventTypes;
    protected MangoPermissionModel readPermission;
    protected MangoPermissionModel editPermission;

    public AbstractEventHandlerModel() { }

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
    public List<AbstractEventTypeModel<?,?, ?>> getEventTypes() {
        return eventTypes;
    }
    /**
     * @param eventTypes the eventTypes to set
     */
    public void setEventTypes(List<AbstractEventTypeModel<?,?, ?>> eventTypes) {
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
    }

    @Override
    public T toVO() {
        T vo = super.toVO();
        vo.setDisabled(disabled);
        if(eventTypes != null) {
            List<EventType> types = new ArrayList<>();
            for(AbstractEventTypeModel<?,?, ?> etm : eventTypes) {
                types.add(etm.toVO());
            }
            vo.setEventTypes(types);
        }
        vo.setReadPermission(readPermission != null ? readPermission.getPermission() : new MangoPermission());
        vo.setEditPermission(editPermission != null ? editPermission.getPermission() : new MangoPermission());

        return vo;
    }
}
