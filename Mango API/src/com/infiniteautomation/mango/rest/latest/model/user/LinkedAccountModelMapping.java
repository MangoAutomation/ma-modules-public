/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.user;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.LinkedAccount;
import com.serotonin.m2m2.vo.OAuth2LinkedAccount;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

@Component
public class LinkedAccountModelMapping implements RestModelMapping<LinkedAccount, LinkedAccountModel> {

    @Override
    public LinkedAccountModel map(Object o, PermissionHolder currentUser, RestModelMapper mapper) {
        LinkedAccount linkedAccount = (LinkedAccount) o;
        if (linkedAccount instanceof OAuth2LinkedAccount) {
            return new OAuth2LinkedAccountModel((OAuth2LinkedAccount) linkedAccount);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedAccount unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        return ((LinkedAccountModel) from).toLinkedAccount();
    }

    @Override
    public Class<LinkedAccountModel> toClass() {
        return LinkedAccountModel.class;
    }

    @Override
    public Class<LinkedAccount> fromClass() {
        return LinkedAccount.class;
    }

}
