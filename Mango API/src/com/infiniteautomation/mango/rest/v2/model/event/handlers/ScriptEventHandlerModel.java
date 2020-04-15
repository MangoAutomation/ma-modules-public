/*
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.infiniteautomation.mango.spring.service.RoleService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.event.handlers.ScriptEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.ScriptEventHandlerVO;
import com.serotonin.m2m2.vo.role.Role;

import io.swagger.annotations.ApiModel;

/**
 * @author Jared Wiltshire
 */
@ApiModel(value=ScriptEventHandlerDefinition.TYPE_NAME, parent=AbstractEventHandlerModel.class)
@JsonTypeName(ScriptEventHandlerDefinition.TYPE_NAME)
public class ScriptEventHandlerModel extends AbstractEventHandlerModel<ScriptEventHandlerVO> {

    String script;
    String engineName;
    Set<String> scriptRoles;

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

        RoleService roleService = Common.getBean(RoleService.class);
        Set<Role> roleXids = scriptRoles.stream().map(xid -> {
            try {
                return roleService.get(xid).getRole();
            } catch (NotFoundException e) {
                return null;
            }
        }).filter(r -> r != null).collect(Collectors.toSet());
        vo.setScriptRoles(roleXids);

        return vo;
    }

    @Override
    public void fromVO(ScriptEventHandlerVO vo) {
        super.fromVO(vo);
        this.script = vo.getScript();
        this.engineName = vo.getEngineName();
        this.scriptRoles = vo.getScriptRoles().stream().map(r -> r.getXid()).collect(Collectors.toSet());
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

    public Set<String> getScriptRoles() {
        return scriptRoles;
    }

    public void setScriptRoles(Set<String> scriptRoles) {
        this.scriptRoles = scriptRoles;
    }

}
