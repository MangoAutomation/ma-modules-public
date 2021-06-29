/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;

/**
 *
 * @author Terry Packer
 */
public class UserActionAndModel {
    VoAction action;
    String originalUsername;

    @JsonUnwrapped
    UserModel model;

    public VoAction getAction() {
        return action;
    }
    public void setAction(VoAction action) {
        this.action = action;
    }
    public String getOriginalUsername() {
        return originalUsername;
    }
    public void setOriginalUsername(String originalUsername) {
        this.originalUsername = originalUsername;
    }
    public UserModel getModel() {
        return model;
    }
    public void setModel(UserModel model) {
        this.model = model;
    }
}
