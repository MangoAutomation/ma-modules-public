/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.script.MangoJavaScript;
import com.infiniteautomation.mango.util.script.ScriptPermissions;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.rt.script.ScriptContextVariable;
import com.serotonin.m2m2.util.log.LogLevel;
import com.serotonin.m2m2.vo.DataPointVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Terry Packer
 *
 */
public class MangoJavaScriptModel {

    @ApiModelProperty("Should we wrap the script in a function and call that function? Useful when script has a return method.")
    private boolean wrapInFunction;
    private String script;
    private List<ScriptContextVariableModel> context;
    private Set<String> permissions;
    private LogLevel logLevel;
    @ApiModelProperty("If non-null coerce the result into a PointValueTime with this data type")
    private String resultDataType;

    @ApiModelProperty("Any additional context to be added to the script during exeuction/testing")
    private Map<String, Object> additionalContext;

    public MangoJavaScriptModel() {

    }

    /**
     * @return the wrapInFunction
     */
    public boolean isWrapInFunction() {
        return wrapInFunction;
    }

    /**
     * @param wrapInFunction the wrapInFunction to set
     */
    public void setWrapInFunction(boolean wrapInFunction) {
        this.wrapInFunction = wrapInFunction;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }
    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }
    /**
     * @return the context
     */
    public List<ScriptContextVariableModel> getContext() {
        return context;
    }
    /**
     * @param context the context to set
     */
    public void setContext(List<ScriptContextVariableModel> context) {
        this.context = context;
    }
    /**
     * @return the permissions
     */
    public Set<String> getPermissions() {
        return permissions;
    }
    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    /**
     * @return the logLevel
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }
    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * @return the resultDataTypeId
     */
    public String getResultDataType() {
        return resultDataType;
    }

    /**
     * @param resultDataTypeId the resultDataTypeId to set
     */
    public void setResultDataType(String resultDataTypeId) {
        this.resultDataType = resultDataTypeId;
    }

    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(Map<String, Object> additionalContext) {
        this.additionalContext = additionalContext;
    }

    public MangoJavaScript toVO() {
        MangoJavaScript vo = new MangoJavaScript();
        vo.setWrapInFunction(wrapInFunction);
        vo.setContext(convertContext());
        vo.setLogLevel(logLevel);
        PermissionService service = Common.getBean(PermissionService.class);
        vo.setPermissions(new ScriptPermissions(service.explodeLegacyPermissionGroupsToRoles(permissions)));
        if(resultDataType != null)
            vo.setResultDataType(DataType.fromName(resultDataType));
        vo.setScript(script);
        vo.setAdditionalContext(additionalContext);
        return vo;
    }

    private List<ScriptContextVariable> convertContext() {
        List<ScriptContextVariable> result = new ArrayList<>();
        if(context != null)
            for(ScriptContextVariableModel variable : context) {
                ScriptContextVariable var = new ScriptContextVariable();
                DataPointVO dpvo = DataPointDao.getInstance().getByXid(variable.getXid());
                if(dpvo == null)
                    var.setDataPointId(Common.NEW_ID);
                else
                    var.setDataPointId(dpvo.getId());
                var.setVariableName(variable.getVariableName());
                result.add(var);
            }
        return result;
    }

    /**
     * Holder for script context variable info
     * @author Terry Packer
     *
     */
    public static class ScriptContextVariableModel {
        private String xid;
        private String variableName;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean contextUpdate;

        public ScriptContextVariableModel() { }

        public ScriptContextVariableModel(String xid, String variableName) {
            this.xid = xid;
            this.variableName = variableName;
        }

        public ScriptContextVariableModel(String xid, String variableName, Boolean updatesContext) {
            this.xid = xid;
            this.variableName = variableName;
            this.contextUpdate = updatesContext;
        }

        public String getXid() {
            return xid;
        }
        public void setXid(String xid) {
            this.xid = xid;
        }
        public String getVariableName() {
            return variableName;
        }
        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }
        public Boolean getContextUpdate() {
            return contextUpdate;
        }
        public void setContextUpdate(Boolean contextUpdate) {
            this.contextUpdate = contextUpdate;
        }
    }
}
