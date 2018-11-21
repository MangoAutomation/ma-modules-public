/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class EmailEventHandlerModelMapping implements RestModelMapping<EmailEventHandlerVO, EmailEventHandlerModel> {

    @Override
    public EmailEventHandlerModel map(Object o) {
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
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventHandlerModel.class));
    }
}
