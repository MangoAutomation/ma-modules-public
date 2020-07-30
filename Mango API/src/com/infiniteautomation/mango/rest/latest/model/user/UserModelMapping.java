/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class UserModelMapping implements RestModelMapping<User, UserModel> {

    private final Map<String,String> fieldMap;

    public UserModelMapping() {
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("roles", "permissions");
    }

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

    @Override
    public ProcessResult mapValidationErrors(Class<?> modelClass, Class<?> validatedClass,
            ProcessResult result, RestModelMapper restModelMapper) {
        return mapValidationErrors(fieldMap, result);
    }

}
