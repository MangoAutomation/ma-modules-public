/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.user;

import com.infiniteautomation.mango.rest.v2.bulk.RestExceptionIndividualResponse;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;

/**
 *
 * @author Terry Packer
 */
public class UserIndividualResponse extends RestExceptionIndividualResponse<VoAction, UserModel> {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
