/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.user;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class UserModelMapping implements RestModelMapping<User, UserModel> {

    @Override
    public UserModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
        return new UserModel((User)o);
    }

    @Override
    public Class<UserModel> toClass() {
        return UserModel.class;
    }

    @Override
    public Class<User> fromClass() {
        return User.class;
    }

}
