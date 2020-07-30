/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.mailingList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;
import com.serotonin.m2m2.vo.mailingList.UserEntry;
import com.serotonin.m2m2.vo.mailingList.UserPhoneEntry;

import io.swagger.annotations.ApiModel;

/**
 *
 * @author Terry Packer
 */
@ApiModel(value = "USER_PHONE_NUMBER", parent = EmailRecipientModel.class)
@JsonTypeName("USER_PHONE_NUMBER")
public class UserPhoneEntryModel extends EmailRecipientModel {

    private String username;

    public UserPhoneEntryModel() {}

    @JsonCreator
    public UserPhoneEntryModel(@JsonProperty("username") String username) {
        this.username = username;
    }

    public UserPhoneEntryModel(UserPhoneEntry data) {
        User u = UserDao.getInstance().get(data.getUserId());
        if (u != null)
            username = u.getUsername();
    }

    @JsonGetter("username")
    public String getUsername() {
        return username;
    }

    @JsonSetter("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public MailingListRecipient fromModel() {
        UserEntry entry = new UserEntry();
        Integer user = UserDao.getInstance().getIdByXid(username);
        if (user != null) {
            entry.setUserId(user);
        }
        return entry;
    }
}
