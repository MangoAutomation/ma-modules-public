/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.user;

import com.infiniteautomation.mango.rest.latest.bulk.IndividualRequest;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;

/**
 *
 * @author Terry Packer
 */
public class UserIndividualRequest extends IndividualRequest<VoAction, UserModel> {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
