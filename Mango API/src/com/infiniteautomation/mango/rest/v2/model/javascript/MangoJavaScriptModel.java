/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.javascript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.script.ScriptContextVariableModel;
import com.infiniteautomation.mango.util.script.MangoJavaScript;
import com.infiniteautomation.mango.util.script.ScriptLogLevels;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IDataPointValueSource;
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
    private String permissions;
    private ScriptLogLevels logLevel;
    @ApiModelProperty("If non-null coerce the result into a PointValueTime with this data type")
    private String resultDataType; 
    
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
    public String getPermissions() {
        return permissions;
    }
    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    /**
     * @return the logLevel
     */
    public ScriptLogLevels getLogLevel() {
        return logLevel;
    }
    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(ScriptLogLevels logLevel) {
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
    
    public MangoJavaScript toVO(boolean testRun) {
        MangoJavaScript vo = new MangoJavaScript();
        vo.setWrapInFunction(wrapInFunction);
        vo.setContext(convertContext(testRun));
        vo.setLogLevel(logLevel);
        vo.setPermissions(permissions);
        if(resultDataType != null)
            vo.setResultDataTypeId(DataTypes.CODES.getId(resultDataType));
        vo.setScript(script);
        return vo;
    }
    
    private Map<String, IDataPointValueSource> convertContext(boolean testRun) {
        Map<String, IDataPointValueSource> result = new HashMap<>();
        if(context != null)
            for(ScriptContextVariableModel variable : context) {
                DataPointVO dpvo = DataPointDao.getInstance().getByXid(variable.getXid());
                if(dpvo == null)
                    throw new ServerErrorException(new TranslatableMessage("rest.error.pointNotFound", variable.getXid()));

                DataPointRT dprt = Common.runtimeManager.getDataPoint(dpvo.getId());
                if(dprt == null) {
                    if(!testRun)
                        throw new ServerErrorException(new TranslatableMessage("rest.error.pointNotEnabled", variable.getXid()));
                    if(dpvo.getDefaultCacheSize() == 0)
                        dpvo.setDefaultCacheSize(1);
                    dprt = new DataPointRT(dpvo, dpvo.getPointLocator().createRuntime(), DataSourceDao.getInstance().getDataSource(dpvo.getDataSourceId()), null);
                    dprt.resetValues();
                }

                result.put(variable.getVariableName(), dprt);
            }
        return result;
    }
}
