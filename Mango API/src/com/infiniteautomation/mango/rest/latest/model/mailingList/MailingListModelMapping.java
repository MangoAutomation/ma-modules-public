/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.mailingList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.MailingListService;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.vo.mailingList.MailingListRecipient;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MailingListModelMapping implements RestModelMapping<MailingList, MailingListModel> {

    private final MailingListService mailingListService;

    @Autowired
    public MailingListModelMapping(MailingListService mailingListService) {
        this.mailingListService = mailingListService;
    }

    @Override
    public Class<? extends MailingList> fromClass() {
        return MailingList.class;
    }

    @Override
    public Class<? extends MailingListModel> toClass() {
        return MailingListModel.class;
    }

    @Override
    public boolean supports(Class<?> from, Class<?> toClass) {
        return this.fromClass().isAssignableFrom(from) &&
                (toClass.isAssignableFrom(this.toClass())
                        || toClass.isAssignableFrom(MailingListWithRecipientsModel.class));
    }

    @Override
    public MailingListModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        MailingList vo = (MailingList)from;
        MailingListModel model;
        if(mailingListService.hasRecipientViewPermission(user, vo)) {
            model = new MailingListWithRecipientsModel(vo);

            if(vo.getEntries() != null && vo.getEntries().size() > 0) {
                List<EmailRecipientModel> recipients = new ArrayList<>();
                ((MailingListWithRecipientsModel)model).setRecipients(recipients);

                for(MailingListRecipient entry : vo.getEntries()) {
                    recipients.add(mapper.map(entry, EmailRecipientModel.class, user));
                }
            }
        } else {
            model = new MailingListModel(vo);
        }
        model.setReadPermissions(new MangoPermissionModel(vo.getReadPermission()));
        model.setEditPermissions(new MangoPermissionModel(vo.getEditPermission()));

        return model;
    }

    @Override
    public MailingList unmap(Object from, PermissionHolder user, RestModelMapper mapper) throws ValidationException {
        MailingListModel model = (MailingListModel)from;
        MailingList vo = model.toVO();
        return vo;
    }

}
