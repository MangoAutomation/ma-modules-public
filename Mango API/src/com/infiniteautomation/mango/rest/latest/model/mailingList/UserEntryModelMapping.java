/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.mailingList;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.vo.mailingList.UserEntry;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@Component
public class UserEntryModelMapping implements RestModelMapping<UserEntry, UserEntryModel> {

    @Override
    public Class<? extends UserEntry> fromClass() {
        return UserEntry.class;
    }

    @Override
    public Class<? extends UserEntryModel> toClass() {
        return UserEntryModel.class;
    }

    @Override
    public UserEntryModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        UserEntry vo = (UserEntry)from;
        return new UserEntryModel(vo);
    }

}
