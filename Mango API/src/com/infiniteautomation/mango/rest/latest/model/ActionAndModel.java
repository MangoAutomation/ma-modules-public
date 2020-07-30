/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;

/**
 * Used for bulk updates via CSV, combines the action and model (unwrapped) into a single object
 *
 * @author Jared Wiltshire
 */
public class ActionAndModel<T> {
    VoAction action;
    String originalXid;

    @JsonUnwrapped
    T model;

    public VoAction getAction() {
        return action;
    }
    public void setAction(VoAction action) {
        this.action = action;
    }
    public String getOriginalXid() {
        return originalXid;
    }
    public void setOriginalXid(String originalXid) {
        this.originalXid = originalXid;
    }
    public T getModel() {
        return model;
    }
    public void setModel(T model) {
        this.model = model;
    }

}
