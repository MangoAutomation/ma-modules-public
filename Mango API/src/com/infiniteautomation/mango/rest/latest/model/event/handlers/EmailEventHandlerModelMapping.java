/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.mailingList.EmailRecipientModel;
import com.serotonin.m2m2.vo.event.EmailEventHandlerVO;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class EmailEventHandlerModelMapping implements AbstractEventHandlerModelMapping<EmailEventHandlerVO> {

    @Override
    public EmailEventHandlerModel mapHandler(EmailEventHandlerVO vo, PermissionHolder user, RestModelMapper mapper) {
        EmailEventHandlerModel model = new EmailEventHandlerModel(vo);

        if(vo.getActiveRecipients() != null) {
            List<EmailRecipientModel> activeRecipients = new ArrayList<>();
            model.setActiveRecipients(activeRecipients);

            for(MailingListRecipient bean : vo.getActiveRecipients()) {
                activeRecipients.add(mapper.map(bean, EmailRecipientModel.class, user));
            }
        }

        if(vo.getEscalationRecipients() != null) {
            List<EmailRecipientModel> escalationRecipients = new ArrayList<>();
            model.setEscalationRecipients(escalationRecipients);

            for(MailingListRecipient bean : vo.getEscalationRecipients()) {
                escalationRecipients.add(mapper.map(bean, EmailRecipientModel.class, user));
            }
        }

        if(vo.getInactiveRecipients() != null) {
            List<EmailRecipientModel> inactiveRecipients = new ArrayList<>();
            model.setInactiveRecipients(inactiveRecipients);

            for(MailingListRecipient bean : vo.getInactiveRecipients()) {
                inactiveRecipients.add(mapper.map(bean, EmailRecipientModel.class, user));
            }
        }

        return model;
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
