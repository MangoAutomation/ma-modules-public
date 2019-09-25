/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Terry Packer
 *
 */
public class ApprovedUsersModel {

    private List<String> approvedUsers = new ArrayList<>();
    private Map<String, TranslatableMessage> failedUsers = new HashMap<>();
    
    public void addApproved(String username) {
        approvedUsers.add(username);
    }

    public void addFailedApproval(String username, TranslatableMessage translatableMessage) {
        failedUsers.put(username, translatableMessage);
    }

    public List<String> getApprovedUsers() {
        return approvedUsers;
    }

    public void setApprovedUsers(List<String> approvedUsers) {
        this.approvedUsers = approvedUsers;
    }

    public Map<String, TranslatableMessage> getFailedUsers() {
        return failedUsers;
    }

    public void setFailedUsers(Map<String, TranslatableMessage> failedUsers) {
        this.failedUsers = failedUsers;
    }
    
}
