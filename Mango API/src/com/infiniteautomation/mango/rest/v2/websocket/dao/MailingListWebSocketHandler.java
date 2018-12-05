/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListModel;
import com.infiniteautomation.mango.rest.v2.model.mailingList.MailingListWithRecipientsModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.MailingListService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.MailingList;

/**
 * @author Jared Wiltshire
 */
@Component
@WebSocketMapping("/websocket/mailing-lists")
public class MailingListWebSocketHandler extends DaoNotificationWebSocketHandler<MailingList> {

    private final MailingListService service;

    @Autowired
    public MailingListWebSocketHandler(MailingListService service) {
        this.service = service;
    }

    @Override
    protected boolean hasPermission(User user, MailingList vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(MailingList vo) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object createModel(MailingList vo, User user) {
        if (service.hasRecipientViewPermission(user, vo)) {
            return new MailingListWithRecipientsModel(vo);
        } else {
            return new MailingListModel(vo);
        }
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends MailingList> event) {
        this.notify(event);
    }

    @Override
    protected boolean isModelPerUser() {
        return true;
    }
}
