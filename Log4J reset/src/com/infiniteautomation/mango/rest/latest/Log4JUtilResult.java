/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import com.infiniteautomation.mango.rest.latest.model.system.actions.SystemActionResult;

/**
 * @author Terry Packer
 *
 */
public class Log4JUtilResult extends SystemActionResult {
    
    private String logOutput;

    /**
     * @return the logOutput
     */
    public String getLogOutput() {
        return logOutput;
    }

    /**
     * @param logOutput the logOutput to set
     */
    public void setLogOutput(String logOutput) {
        this.logOutput = logOutput;
    }
    
    
}
