/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.system.actions;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.Validatable;

/**
 * Base class for all system action inputs
 *
 * @author Terry Packer
 *
 */
public class SystemActionModel implements Validatable {

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
     * Override as needed
     * @param result
     */
    @Override
    public void validate(ProcessResult result) {

    }

}
