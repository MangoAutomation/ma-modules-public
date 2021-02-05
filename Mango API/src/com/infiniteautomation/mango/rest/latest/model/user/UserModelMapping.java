/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.infiniteautomation.mango.spring.service.UsersService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 */
@Component
public class UserModelMapping implements RestModelMapping<User, UserModel> {

    private final UsersService usersService;

    @Autowired
    public UserModelMapping(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public UserModel map(Object o, PermissionHolder currentUser, RestModelMapper mapper) {
        User user = (User) o;
        UserModel model = new UserModel(user);

        List<LinkedAccountModel> linkedAccounts = usersService.getLinkedAccounts(user).stream()
                .map(account -> mapper.map(account, LinkedAccountModel.class, currentUser))
                .collect(Collectors.toList());
        model.setLinkedAccounts(linkedAccounts);


        return model;
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
