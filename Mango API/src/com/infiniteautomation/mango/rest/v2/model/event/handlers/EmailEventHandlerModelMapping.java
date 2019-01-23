/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class EmailEventHandlerModelMapping implements RestModelMapping<EmailEventHandlerVO, EmailEventHandlerModel> {

    @Override
    public EmailEventHandlerModel map(Object o, User user, RestModelMapper mapper) {
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

}
