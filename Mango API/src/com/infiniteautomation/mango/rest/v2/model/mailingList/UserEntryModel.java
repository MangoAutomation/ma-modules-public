/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.RecipientListEntryBean;
import com.serotonin.m2m2.vo.mailingList.UserEntry;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value = "USER", parent = EmailRecipientModel.class)
@JsonTypeName("USER")
public class UserEntryModel extends EmailRecipientModel {

    private String username;

    public UserEntryModel() {}

    @JsonCreator
    public UserEntryModel(@JsonProperty("username") String username) {
        this.username = username;
    }

    public UserEntryModel(UserEntry data) {
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
    public EmailRecipient fromModel() {
        UserEntry entry = new UserEntry();
        User user = UserDao.getInstance().getByXid(username);
        if (user != null) {
            entry.setUser(user);
            entry.setUserId(user.getId());
        }
        return entry;
    }

    @Override
    public RecipientListEntryBean toBean() {
        RecipientListEntryBean bean = new RecipientListEntryBean();
        bean.setRecipientType(EmailRecipient.TYPE_USER);
        User u = UserDao.getInstance().getByXid(username);
        if(u != null)
            bean.setReferenceId(u.getId());
        return bean;
    }
}
