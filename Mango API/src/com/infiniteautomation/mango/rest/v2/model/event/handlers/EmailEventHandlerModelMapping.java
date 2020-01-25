/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class EmailEventHandlerModelMapping implements RestModelJacksonMapping<EmailEventHandlerVO, EmailEventHandlerModel> {

    @Override
    public EmailEventHandlerModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
        return new EmailEventHandlerModel((EmailEventHandlerVO)o);
    }

    @Override
    public Class<EmailEventHandlerModel> toClass() {
        return EmailEventHandlerModel.class;
    }

    @Override
    public Class<EmailEventHandlerVO> fromClass() {
        return EmailEventHandlerVO.class;
    }

    @Override
    public String getTypeName() {
        return "EMAIL";
    }

}
