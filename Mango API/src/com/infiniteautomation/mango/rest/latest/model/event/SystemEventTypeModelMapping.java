/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.user.UserModel;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class SystemEventTypeModelMapping implements RestModelJacksonMapping<SystemEventType, SystemEventTypeModel> {

    @Override
    public Class<SystemEventType> fromClass() {
        return SystemEventType.class;
    }

    @Override
    public Class<SystemEventTypeModel> toClass() {
        return SystemEventTypeModel.class;
    }

    @Override
    public SystemEventTypeModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        SystemEventType type = (SystemEventType) from;
        if(StringUtils.equals(SystemEventType.TYPE_USER_LOGIN, type.getEventSubtype())) {
            UserModel userModel = null;
            User u = UserDao.getInstance().get(type.getReferenceId1());
            if(u != null)
                userModel = new UserModel(u);
            return new SystemEventTypeModel(type, userModel);
        }else
            return new SystemEventTypeModel(type);
    }
    
    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.SYSTEM;
    }
}
