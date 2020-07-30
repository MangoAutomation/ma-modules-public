/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.mailingList;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.vo.mailingList.UserPhoneEntry;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class UserPhoneEntryModelMapping implements RestModelMapping<UserPhoneEntry, UserPhoneEntryModel> {

    @Override
    public Class<? extends UserPhoneEntry> fromClass() {
        return UserPhoneEntry.class;
    }

    @Override
    public Class<? extends UserPhoneEntryModel> toClass() {
        return UserPhoneEntryModel.class;
    }

    @Override
    public UserPhoneEntryModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        UserPhoneEntry vo = (UserPhoneEntry)from;
        return new UserPhoneEntryModel(vo);
    }

}
