/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import com.infiniteautomation.mango.rest.v2.model.system.actions.SystemActionModel;
import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class Log4JUtilModel extends SystemActionModel {
    
    private Log4JUtilAction action;
    
    /**
     * @return the action
     */
    public Log4JUtilAction getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(Log4JUtilAction action) {
        this.action = action;
    }

    @Override
    public void validate(ProcessResult result) {
        if(action == null)
            result.addContextualMessage("action", "validate.required");
    }

    public static enum Log4JUtilAction {
        RESET,
        TEST_DEBUG,
        TEST_INFO,
        TEST_WARN,
        TEST_ERROR,
        TEST_FATAL
    }
}
