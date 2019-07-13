/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.v2.EventsRestController;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.EventActionEnum;
import com.infiniteautomation.mango.rest.v2.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventLevelSummaryModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.Validatable;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/events")
public class EventsWebSocketHandler extends MultiSessionWebSocketHandler {

    public static final String SUBSCRIPTION_ATTRIBUTE = "EventNotificationSubscription";
    public static final String REQUEST_TYPE_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String REQUEST_TYPE_QUERY = "QUERY";
    
    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="requestType")
    @JsonSubTypes({
        @JsonSubTypes.Type(name = REQUEST_TYPE_SUBSCRIPTION, value = EventsSubscriptionRequest.class),
        @JsonSubTypes.Type(name = REQUEST_TYPE_QUERY, value = EventsRqlQueryRequest.class)
    })
    public static abstract class EventsWebsocketRequest extends WebSocketRequest implements Validatable {
    }
    public static class EventsRqlQueryRequest extends EventsWebsocketRequest {
        private String rqlQuery;
        
        @Override
        public void validate(ProcessResult response) {
            // TODO Auto-generated method stub
        }
        
        public String getRqlQuery() {
            return rqlQuery;
        }
        public void setRqlQuery(String rqlQuery) {
            this.rqlQuery = rqlQuery;
        }
    }
    
    public static class EventsSubscriptionRequest extends EventsWebsocketRequest {
        private Set<AlarmLevels> levels;
        private Set<EventActionEnum> actions;
        private boolean sendEventLevelSummaries;
        
        @Override
        public void validate(ProcessResult response) {
            
        }

        public Set<AlarmLevels> getLevels() {
            return levels;
        }
        public void setLevels(Set<AlarmLevels> levels) {
            this.levels = levels;
        }
        public Set<EventActionEnum> getActions() {
            return actions;
        }
        public void setActions(Set<EventActionEnum> actions) {
            this.actions = actions;
        }
        public boolean isSendEventLevelSummaries() {
            return sendEventLevelSummaries;
        }
        public void setSendEventLevelSummaries(boolean sendEventLevelSummaries) {
            this.sendEventLevelSummaries = sendEventLevelSummaries;
        }

    }
    
    private final RestModelMapper modelMapper;
    private final EventsRestController controller;
    
    @Autowired
    public EventsWebSocketHandler(RestModelMapper modelMapper, EventsRestController controller) {
        super(true);
        this.modelMapper = modelMapper;
        this.controller = controller;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Check for permissions
        User user = this.getUser(session);
        if (user == null) {
            return;
        } 
        super.afterConnectionEstablished(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        EventsWebSocketListener listener = (EventsWebSocketListener)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
        if(listener != null)
            listener.terminate();
        super.afterConnectionClosed(session, status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        EventsWebSocketListener listener = (EventsWebSocketListener)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
        if(listener != null)
            listener.terminate();
        super.handleTransportError(session, exception);
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            User user = this.getUser(session);
            JsonNode tree = this.jacksonMapper.readTree(message.getPayload());
            
            if (!WebSocketMessageType.REQUEST.messageTypeMatches(tree) || tree.get("requestType") == null) {
                return;
            }
            
            EventsWebsocketRequest request = this.jacksonMapper.treeToValue(tree, EventsWebsocketRequest.class);
            request.ensureValid();
            
            if(request instanceof EventsSubscriptionRequest) {
                EventsSubscriptionRequest subscription = (EventsSubscriptionRequest)request;
                //Configure listener
                EventsWebSocketListener listener = (EventsWebSocketListener)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
    
                Set<AlarmLevels> levels = subscription.getLevels();
                if (levels == null) {
                    levels = Collections.emptySet();
                }
                Set<EventActionEnum> actions = subscription.getActions();
                if (actions == null) {
                    actions = EnumSet.noneOf(EventActionEnum.class);
                }
                
                boolean emptySubscriptions = levels.isEmpty() || actions.isEmpty();
                if(listener == null) {
                    if(!emptySubscriptions) {
                        listener = new EventsWebSocketListener(session, user, levels, actions);
                        listener.initialize();
                    }
                }else {
                    if(emptySubscriptions) {
                        listener.terminate();
                    }else {
                        listener.changeActions(actions);
                        listener.changeLevels(levels);
                    }
                }
                if(subscription.isSendEventLevelSummaries()) {
                    WebSocketResponse<List<EventLevelSummaryModel>> response = new WebSocketResponse<>(request.getSequenceNumber());
                    response.setPayload(controller.getActiveSummary(user));
                    this.sendRawMessage(session, response);
                }else {
                    this.sendRawMessage(session, new WebSocketResponse<Void>(request.getSequenceNumber()));
                }
            }else if(request instanceof EventsRqlQueryRequest) {
                EventsRqlQueryRequest query = (EventsRqlQueryRequest)request;
                WebSocketResponse<StreamedArrayWithTotal> response = new WebSocketResponse<>(request.getSequenceNumber());
                response.setPayload(controller.queryRQL(RQLUtils.parseRQLtoAST(query.getRqlQuery()), user));
                this.sendRawMessage(session, response);
            }
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    protected void notify(EventActionEnum action, EventInstance event, User user, WebSocketSession session) {
        try {
            EventInstanceModel instanceModel = modelMapper.map(event, EventInstanceModel.class, user); 
            sendRawMessage(session, new WebSocketNotification<EventInstanceModel>(action.name(), instanceModel));
        } catch(WebSocketSendException e) {
            log.warn("Error notifying websocket session", e);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }
    
    
    public class EventsWebSocketListener implements UserEventListener {
        private final WebSocketSession session;
        private final User user;
        private volatile Set<AlarmLevels> levels;
        private volatile EnumSet<EventActionEnum> actions;

        public EventsWebSocketListener(WebSocketSession session, User user, Set<AlarmLevels> levels, Set<EventActionEnum> actions) {
            this.session = session;
            this.user = user;
            this.changeLevels(levels);
            this.changeActions(actions);
        }

        public void initialize() {
            Common.eventManager.addUserEventListener(this);
        }

        public void terminate(){
            Common.eventManager.removeUserEventListener(this);
        }

        public void changeLevels(Set<AlarmLevels> levels) {
            this.levels = levels;
        }

        public void changeActions(Set<EventActionEnum> actions) {
            this.actions = EnumSet.copyOf(actions);
        }

        @Override
        public int getUserId() {
            return user.getId();
        }

        @Override
        public void raised(EventInstance evt) {
            if (!session.isOpen() || getUser(session) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Terminating listener for session " + session.getId());
                }
                this.terminate();
                return;
            }

            if(!this.actions.contains(EventActionEnum.RAISED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            if (log.isDebugEnabled()) {
                log.debug("Event raised, notifying session " + session.getId() + ": " + evt.toString());
            }

            try{
                EventsWebSocketHandler.this.notify(EventActionEnum.RAISED, evt, user, session);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event raised", e);
                }
            }
        }

        @Override
        public void returnToNormal(EventInstance evt) {
            if (!session.isOpen() || getUser(session) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Terminating listener for session " + session.getId());
                }
                this.terminate();
                return;
            }

            if(!this.actions.contains(EventActionEnum.RETURN_TO_NORMAL))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            if (log.isDebugEnabled()) {
                log.debug("Event return to normal, notifying session " + session.getId() + ": " + evt.toString());
            }

            try{
                EventsWebSocketHandler.this.notify(EventActionEnum.RETURN_TO_NORMAL, evt, user, session);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event return to normal", e);
                }
            }
        }

        @Override
        public void deactivated(EventInstance evt) {
            if (!session.isOpen() || getUser(session) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Terminating listener for session " + session.getId());
                }
                this.terminate();
                return;
            }

            if(!this.actions.contains(EventActionEnum.DEACTIVATED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            if (log.isDebugEnabled()) {
                log.debug("Event deactivated, notifying session " + session.getId() + ": " + evt.toString());
            }

            try{
                EventsWebSocketHandler.this.notify(EventActionEnum.DEACTIVATED, evt, user, session);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event deactivated", e);
                }
            }
        }

        @Override
        public void acknowledged(EventInstance evt) {
            if (!session.isOpen() || getUser(session) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Terminating listener for session " + session.getId());
                }
                this.terminate();
                return;
            }

            if(!this.actions.contains(EventActionEnum.ACKNOWLEDGED))
                return;

            if(!this.levels.contains(evt.getAlarmLevel()))
                return;

            if (log.isDebugEnabled()) {
                log.debug("Event acknowledged, notifying session " + session.getId() + ": " + evt.toString());
            }

            try{
                EventsWebSocketHandler.this.notify(EventActionEnum.ACKNOWLEDGED, evt, user, session);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error notifying of event acknowledged", e);
                }
            }
        }
    }
}
