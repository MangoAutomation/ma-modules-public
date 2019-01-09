/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.javascript;

import java.util.List;

import com.infiniteautomation.mango.util.script.MangoJavaScriptResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

import io.swagger.annotations.ApiModelProperty;

/**
 * Results of validating/running a Mango JavaScript
 * 
 * @author Terry Packer
 *
 */
public class MangoJavaScriptResultModel {

    @ApiModelProperty("Things the script has actioned i.e. setting a point value")
    private List<TranslatableMessage> actions;
    @ApiModelProperty("Errors from executing the script")
    private List<TranslatableMessage> errors;
    @ApiModelProperty("Script log and console.log messages")
    private String scriptOutput;
    @ApiModelProperty("Returned value from script, can be null")
    private Object result;
    
    public MangoJavaScriptResultModel() {
        
    }
    
    public MangoJavaScriptResultModel(MangoJavaScriptResult script) {
        this.actions = script.getActions();
        this.errors = script.getErrors();
        this.scriptOutput = script.getScriptOutput();
        Object result = script.getResult();
        if(result instanceof PointValueTime)
            this.result = new PointValueTimeModel((PointValueTime)result);
        else
            this.result = result;
    }
    
    /**
     * @return the actions
     */
    public List<TranslatableMessage> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<TranslatableMessage> actions) {
        this.actions = actions;
    }

    /**
     * @return the errors
     */
    public List<TranslatableMessage> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<TranslatableMessage> errors) {
        this.errors = errors;
    }

    /**
     * @return the scriptOutput
     */
    public String getScriptOutput() {
        return scriptOutput;
    }

    /**
     * @param scriptOutput the scriptOutput to set
     */
    public void setScriptOutput(String scriptOutput) {
        this.scriptOutput = scriptOutput;
    }

    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }
}
