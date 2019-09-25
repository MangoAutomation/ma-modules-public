/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.user;

import java.util.List;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.Validatable;

/**
 * @author Terry Packer
 *
 */
public class ApproveUsersModel implements Validatable {

    private List<String> usernames;
    private boolean sendEmail;
    private List<String> rolesToAdd;
    
    public List<String> getUsernames() {
        return usernames;
    }
    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
    public boolean isSendEmail() {
        return sendEmail;
    }
    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
    public List<String> getRolesToAdd() {
        return rolesToAdd;
    }
    public void setRolesToAdd(List<String> rolesToAdd) {
        this.rolesToAdd = rolesToAdd;
    }
    @Override
    public void validate(ProcessResult response) {
        if(usernames == null || usernames.size() == 0) {
            response.addContextualMessage("usernames", "validate.required");
        }
    }
}
