/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.system.actions;

import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * Base class for all system action inputs
 * 
 * @author Terry Packer
 *
 */
public class SystemActionModel {

    /* For temporary resource */
    private Long expiration;  //How long after it finishes will the result remain
    private Long timeout; //How long after it starts does it have until the task is cancelled

    /**
     * @return the expiration
     */
    public Long getExpiration() {
        return expiration;
    }
    /**
     * @param expiration the expiration to set
     */
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    /**
     * @return the timeout
     */
    public Long getTimeout() {
        return timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
    
    /**
     * @throws ValidationException
     */
    public void ensureValid() throws ValidationException {
        ProcessResult result = new ProcessResult();
        validate(result);
        result.ensureValid();
    }
    
    /**
     * Override as needed
     * @param result
     */
    public void validate(ProcessResult result) {
        
    }
    
}
