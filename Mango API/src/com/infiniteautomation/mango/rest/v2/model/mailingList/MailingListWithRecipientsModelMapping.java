/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.MailingList;

/**
 * @author Terry Packer
 *
 */
@Component
public class MailingListWithRecipientsModelMapping implements RestModelMapping<MailingList, MailingListWithRecipientsModel> {

    @Override
    public MailingListWithRecipientsModel map(Object o, User user, RestModelMapper mapper) {
        return new MailingListWithRecipientsModel((MailingList)o);
    }

    @Override
    public Class<MailingListWithRecipientsModel> toClass() {
        return MailingListWithRecipientsModel.class;
    }

    @Override
    public Class<MailingList> fromClass() {
        return MailingList.class;
    }

}
