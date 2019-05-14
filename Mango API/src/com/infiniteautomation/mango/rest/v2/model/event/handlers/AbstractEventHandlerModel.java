/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property=AbstractEventHandlerModel.HANDLER_TYPE)
public abstract class AbstractEventHandlerModel<T extends AbstractEventHandlerVO<T>> extends AbstractVoModel<T> {

    public static final String HANDLER_TYPE = "handlerType";
    
    private boolean disabled;
    private List<AbstractEventTypeModel<?,?, ?>> eventTypes;
    
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
    

    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.disabled = vo.isDisabled();
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
        return vo;
    }
}
