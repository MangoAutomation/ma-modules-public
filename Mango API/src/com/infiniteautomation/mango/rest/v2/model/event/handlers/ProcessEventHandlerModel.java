/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.ProcessEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.event.ProcessEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="PROCESS", parent=AbstractEventHandlerModel.class)
@JsonTypeName("PROCESS")
public class ProcessEventHandlerModel extends AbstractEventHandlerModel {

    private String activeProcessCommand;
    private Integer activeProcessTimeout;
    private String inactiveProcessCommand;
    private Integer inactiveProcessTimeout;
    
    public ProcessEventHandlerModel() { }

    public ProcessEventHandlerModel(ProcessEventHandlerVO vo) {
        fromVO(vo);
    }
    
    /**
     * @return the activeProcessCommand
     */
    public String getActiveProcessCommand() {
        return activeProcessCommand;
    }

    /**
     * @param activeProcessCommand the activeProcessCommand to set
     */
    public void setActiveProcessCommand(String activeProcessCommand) {
        this.activeProcessCommand = activeProcessCommand;
    }

    /**
     * @return the activeProcessTimeout
     */
    public Integer getActiveProcessTimeout() {
        return activeProcessTimeout;
    }

    /**
     * @param activeProcessTimeout the activeProcessTimeout to set
     */
    public void setActiveProcessTimeout(Integer activeProcessTimeout) {
        this.activeProcessTimeout = activeProcessTimeout;
    }

    /**
     * @return the inactiveProcessCommand
     */
    public String getInactiveProcessCommand() {
        return inactiveProcessCommand;
    }

    /**
     * @param inactiveProcessCommand the inactiveProcessCommand to set
     */
    public void setInactiveProcessCommand(String inactiveProcessCommand) {
        this.inactiveProcessCommand = inactiveProcessCommand;
    }

    /**
     * @return the inactiveProcessTimeout
     */
    public Integer getInactiveProcessTimeout() {
        return inactiveProcessTimeout;
    }

    /**
     * @param inactiveProcessTimeout the inactiveProcessTimeout to set
     */
    public void setInactiveProcessTimeout(Integer inactiveProcessTimeout) {
        this.inactiveProcessTimeout = inactiveProcessTimeout;
    }

    @Override
    public AbstractEventHandlerVO<?> toVO() {
        ProcessEventHandlerVO vo = (ProcessEventHandlerVO)super.toVO();
        vo.setActiveProcessCommand(activeProcessCommand);
        if(activeProcessTimeout != null)
            vo.setActiveProcessTimeout(activeProcessTimeout);
        vo.setInactiveProcessCommand(inactiveProcessCommand);
        if(inactiveProcessTimeout != null)
            vo.setInactiveProcessTimeout(inactiveProcessTimeout);
        return vo;
    }
    
    @Override
    public void fromVO(AbstractEventHandlerVO<?> vo) {
        super.fromVO(vo);
        ProcessEventHandlerVO handler = (ProcessEventHandlerVO)vo;
        this.activeProcessCommand = handler.getActiveProcessCommand();
        this.activeProcessTimeout = handler.getActiveProcessTimeout();
        this.inactiveProcessCommand = handler.getInactiveProcessCommand();
        this.inactiveProcessTimeout = handler.getInactiveProcessTimeout();
    }
    
    @Override
    protected AbstractEventHandlerVO<?> newVO() {
        ProcessEventHandlerVO handler = new ProcessEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(ProcessEventHandlerDefinition.TYPE_NAME));
        return handler;
    }
}
