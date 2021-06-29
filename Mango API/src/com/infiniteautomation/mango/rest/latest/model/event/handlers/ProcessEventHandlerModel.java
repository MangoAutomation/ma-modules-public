/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.ProcessEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.ProcessEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=ProcessEventHandlerDefinition.TYPE_NAME, parent=AbstractEventHandlerModel.class)
@JsonTypeName(ProcessEventHandlerDefinition.TYPE_NAME)
public class ProcessEventHandlerModel extends AbstractEventHandlerModel<ProcessEventHandlerVO> {

    private String activeProcessCommand;
    private Integer activeProcessTimeout;
    private String inactiveProcessCommand;
    private Integer inactiveProcessTimeout;
    private boolean interpolateCommands;
    
    public ProcessEventHandlerModel() { }

    public ProcessEventHandlerModel(ProcessEventHandlerVO vo) {
        super(vo);
    }
    
    @Override
    public String getHandlerType() {
        return ProcessEventHandlerDefinition.TYPE_NAME;
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

    public boolean isInterpolateCommands() {
        return interpolateCommands;
    }

    public void setInterpolateCommands(boolean interpolateCommands) {
        this.interpolateCommands = interpolateCommands;
    }

    @Override
    public void readInto(ProcessEventHandlerVO vo) {
        super.readInto(vo);
        vo.setActiveProcessCommand(activeProcessCommand);
        if(activeProcessTimeout != null)
            vo.setActiveProcessTimeout(activeProcessTimeout);
        vo.setInactiveProcessCommand(inactiveProcessCommand);
        if(inactiveProcessTimeout != null)
            vo.setInactiveProcessTimeout(inactiveProcessTimeout);
        vo.setInterpolateCommands(interpolateCommands);
    }
    
    @Override
    public void fromVO(ProcessEventHandlerVO vo) {
        super.fromVO(vo);
        this.activeProcessCommand = vo.getActiveProcessCommand();
        this.activeProcessTimeout = vo.getActiveProcessTimeout();
        this.inactiveProcessCommand = vo.getInactiveProcessCommand();
        this.inactiveProcessTimeout = vo.getInactiveProcessTimeout();
        this.interpolateCommands = vo.isInterpolateCommands();
    }
    
    @Override
    protected ProcessEventHandlerVO newVO() {
        ProcessEventHandlerVO handler = new ProcessEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(ProcessEventHandlerDefinition.TYPE_NAME));
        return handler;
    }
}
