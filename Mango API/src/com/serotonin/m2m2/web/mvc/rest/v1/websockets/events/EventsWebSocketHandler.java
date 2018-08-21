/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.events;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventTypeEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventRegistrationModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * @author Terry Packer
 * @author Jared Wiltshire
 */
class EventsWebSocketHandler extends MangoWebSocketHandler {

    private final Object lock = new Object();
    private EventsWebSocketListener listener;
    private boolean connectionClosed = false;
    private WebSocketSession session;

    EventsWebSocketHandler() {
        super();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session = session;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        synchronized(this.lock) {
            if (!this.connectionClosed) {
                if (this.listener != null) {
                    this.listener.terminate();
                    this.listener = null;
                }
                this.connectionClosed = true;
            }
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            User user = this.getUser(session);
            if (user == null) {
                return;
            }

            EventRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), EventRegistrationModel.class);
            Set<String> levels = model.getLevels();
            if (levels == null) {
                levels = Collections.emptySet();
            }
            Set<EventEventTypeEnum> events = model.getEventTypes();
            if (events == null) {
                events = EnumSet.noneOf(EventEventTypeEnum.class);
            }
            boolean emptySubscriptions = levels.isEmpty() || events.isEmpty();

            synchronized(this.lock) {
                if (!this.connectionClosed) {
                    if (this.listener != null) {
                        if (emptySubscriptions) {
                            this.listener.terminate();
                            this.listener = null;
                        } else {
                            this.listener.changeLevels(levels);
                            this.listener.changeEvents(events);
                        }
                    } else if (!emptySubscriptions) {
                        this.listener = new EventsWebSocketListener(user, levels, events);
                        this.listener.initialize();
                    }
                }
            }
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
            }
        }
        if(log.isDebugEnabled())
            log.debug(message.getPayload());
    }

    protected void sendMessage(Object payload) throws JsonProcessingException, Exception {
        // not uncommon that the connection is closed and publisher terminated but event notification still received
        // especially on logout
        if (session.isOpen()) {
            super.sendMessage(session, payload);
        }
    }

    public class EventsWebSocketListener implements UserEventListener {
        private final User user;
        private volatile Set<Integer> levels;
        private volatile EnumSet<EventEventTypeEnum> events;

        public EventsWebSocketListener(User user, Set<String> levels, Set<EventEventTypeEnum> events) {
            this.user = user;
            this.changeLevels(levels);
            this.changeEvents(events);
        }

        public void initialize() {
            Common.eventManager.addUserEventListener(this);
        }

        public void terminate(){
            Common.eventManager.removeUserEventListener(this);
        }

        public void changeLevels(Set<String> strLevels) {
            Set<Integer> levels = new HashSet<>();
            for (String level : strLevels){
                levels.add(AlarmLevels.CODES.getId(level));
            }
            this.levels = levels;
        }

        public void changeEvents(Set<EventEventTypeEnum> events) {
            this.events = EnumSet.copyOf(events);
        }

        @Override
        public int getUserId() {
            return user.getId();
        }

        @Override
        public void raised(EventInstance evt) {
            if(!session.isOpen() || getUser(session) == null)
                this.terminate();

            if(!this.events.contains(EventEventTypeEnum.RAISED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            try{
                sendMessage(new EventEventModel(EventEventTypeEnum.RAISED, evt));
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event raised", e);
                }
            }
        }

        @Override
        public void returnToNormal(EventInstance evt) {
            if(!session.isOpen() || getUser(session) == null)
                this.terminate();

            if(!this.events.contains(EventEventTypeEnum.RETURN_TO_NORMAL))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            try{
                sendMessage(new EventEventModel(EventEventTypeEnum.RETURN_TO_NORMAL, evt));
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event return to normal", e);
                }
            }
        }

        @Override
        public void deactivated(EventInstance evt) {
            if(!session.isOpen() || getUser(session) == null)
                this.terminate();

            if(!this.events.contains(EventEventTypeEnum.DEACTIVATED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            try{
                sendMessage(new EventEventModel(EventEventTypeEnum.DEACTIVATED, evt));
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event deactivated", e);
                }
            }
        }

        @Override
        public void acknowledged(EventInstance evt) {
            if(!session.isOpen() || getUser(session) == null)
                this.terminate();

            if(!this.events.contains(EventEventTypeEnum.ACKNOWLEDGED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            try{
                sendMessage(new EventEventModel(EventEventTypeEnum.ACKNOWLEDGED, evt));
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event acknowledged", e);
                }
            }
        }
    }
}
