/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.permissions;

import java.util.Set;
import java.util.TreeSet;

import com.infiniteautomation.mango.permission.UserRolesDetails;
import com.serotonin.m2m2.vo.role.Role;

/**
 *
 * @author Terry Packer
 */
public class UserRolesDetailsModel {
    private final String username;
    private final boolean admin;
    private final Set<String> allGroups = new TreeSet<>();
    private final Set<String> matchingGroups = new TreeSet<>();

    public UserRolesDetailsModel(UserRolesDetails vo) {
        this.username = vo.getUsername();
        this.admin = vo.isAdmin();
        for(Role role : vo.getAllRoles()) {
            allGroups.add(role.getXid());
        }
        for(Role role : vo.getMatchingRoles()) {
            matchingGroups.add(role.getXid());
        }
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Set<String> getAllGroups() {
        return allGroups;
    }

    public Set<String> getMatchingGroups() {
        return matchingGroups;
    }

    public void addGroup(String group) {
        allGroups.add(group);
    }

    public void addMatchingGroup(String group) {
        matchingGroups.add(group);
    }

    public boolean isAccess() {
        if (admin)
            return true;
        return !matchingGroups.isEmpty();
    }
}
