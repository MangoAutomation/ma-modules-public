/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.javascript;

import java.util.List;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeModel;
import com.infiniteautomation.mango.util.script.MangoJavaScriptAction;
import com.infiniteautomation.mango.util.script.MangoJavaScriptError;
import com.infiniteautomation.mango.util.script.MangoJavaScriptResult;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;

import io.swagger.annotations.ApiModelProperty;

/**
 * Results of validating/running a Mango JavaScript
 *
 * @author Terry Packer
 *
 */
public class MangoJavaScriptResultModel {

    @ApiModelProperty("Things the script has actioned i.e. setting a point value")
    private List<MangoJavaScriptAction> actions;
    @ApiModelProperty("Errors from executing the script")
    private List<MangoJavaScriptError> errors;
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
    public List<MangoJavaScriptAction> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<MangoJavaScriptAction> actions) {
        this.actions = actions;
    }

    /**
     * @return the errors
     */
    public List<MangoJavaScriptError> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<MangoJavaScriptError> errors) {
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
