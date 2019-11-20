/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.ScriptEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.ScriptEventHandlerVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Jared Wiltshire
 */
@ApiModel(value=ScriptEventHandlerDefinition.TYPE_NAME, parent=AbstractEventHandlerModel.class)
@JsonTypeName(ScriptEventHandlerDefinition.TYPE_NAME)
public class ScriptEventHandlerModel extends AbstractEventHandlerModel<ScriptEventHandlerVO> {

    String script;
    String engineName;

    public ScriptEventHandlerModel() {
    }

    public ScriptEventHandlerModel(ScriptEventHandlerVO vo) {
        fromVO(vo);
    }

    @Override
    public String getHandlerType() {
        return ScriptEventHandlerDefinition.TYPE_NAME;
    }

    @Override
    public ScriptEventHandlerVO toVO() {
        ScriptEventHandlerVO vo = super.toVO();
        vo.setScript(this.script);
        vo.setEngineName(this.engineName);
        return vo;
    }

    @Override
    public void fromVO(ScriptEventHandlerVO vo) {
        super.fromVO(vo);
        this.script = vo.getScript();
        this.engineName = vo.getEngineName();
    }

    @Override
    protected ScriptEventHandlerVO newVO() {
        ScriptEventHandlerVO handler = new ScriptEventHandlerVO();
        handler.setDefinition(ModuleRegistry.getEventHandlerDefinition(ScriptEventHandlerDefinition.TYPE_NAME));
        return handler;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

}
