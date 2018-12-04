/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(subTypes= {EmailEventHandlerModel.class, SetPointEventHandlerModel.class, ProcessEventHandlerModel.class}, discriminator="handlerType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=AbstractEventHandlerModel.HANDLER_TYPE)
public abstract class AbstractEventHandlerModel extends AbstractVoModel<AbstractEventHandlerVO<?>>{

    public static final String HANDLER_TYPE = "handlerType";
    
    private boolean disabled;
    private List<AbstractEventTypeModel<?>> eventTypes;
    
    public AbstractEventHandlerModel() { }
    public AbstractEventHandlerModel(AbstractEventHandlerVO<?> vo) {
        super(vo);
    }
    
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
    public List<AbstractEventTypeModel<?>> getEventTypes() {
        return eventTypes;
    }
    /**
     * @param eventTypes the eventTypes to set
     */
    public void setEventTypes(List<AbstractEventTypeModel<?>> eventTypes) {
        this.eventTypes = eventTypes;
    }
    

    @Override
    public void fromVO(AbstractEventHandlerVO<?> vo) {
        super.fromVO(vo);
        this.disabled = vo.isDisabled();
    }
    
    @Override
    public AbstractEventHandlerVO<?> toVO() {
        AbstractEventHandlerVO<?> vo = super.toVO();
        vo.setDisabled(disabled);
        if(eventTypes != null) {
            for(AbstractEventTypeModel<?> etm : eventTypes) {
                vo.addAddedEventType(etm.toVO());
            }
        }
        return vo;
    }
}
