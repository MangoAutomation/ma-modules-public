/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.user;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.serotonin.m2m2.vo.LinkedAccount;
import com.serotonin.m2m2.vo.OAuth2LinkedAccount;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(name = OAuth2LinkedAccount.TYPE, value = OAuth2LinkedAccountModel.class)
})
public abstract class LinkedAccountModel {
    public abstract LinkedAccount toLinkedAccount();
}
