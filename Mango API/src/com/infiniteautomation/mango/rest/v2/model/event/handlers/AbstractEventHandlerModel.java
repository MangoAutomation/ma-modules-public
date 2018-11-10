/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(subTypes= {EmailEventHandlerModel.class, SetPointEventHandlerModel.class, ProcessEventHandlerModel.class}, discriminator="handlerType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="handlerType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmailEventHandlerModel.class, name="EMAIL"),
    @JsonSubTypes.Type(value = SetPointEventHandlerModel.class, name="SET_POINT"),
    @JsonSubTypes.Type(value = ProcessEventHandlerModel.class, name="PROCESS")
})
public abstract class AbstractEventHandlerModel extends AbstractVoModel<AbstractEventHandlerVO<?>>{

    public AbstractEventHandlerModel() { }
    public AbstractEventHandlerModel(AbstractEventHandlerVO<?> vo) {
        super(vo);
    }
    
}
