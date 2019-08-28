/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventSummaryModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventActionEnum;
import com.infiniteautomation.mango.rest.v2.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventLevelSummaryModel;
import com.infiniteautomation.mango.spring.service.EventInstanceService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.DataPointEventLevelSummary;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventLevelSummary;
import com.serotonin.m2m2.rt.event.UserEventListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.Validatable;
import com.serotonin.m2m2.vo.permission.PermissionException;

/**
 * @author Terry Packer
 *
 */
public class EventsWebSocketHandler extends MangoWebSocketHandler implements UserEventListener {

    private final static Log LOG = LogFactory.getLog(EventsWebSocketHandler.class);
    
    public static final String SUBSCRIPTION_ATTRIBUTE = "EventNotificationSubscription";
    public static final String REQUEST_TYPE_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String REQUEST_TYPE_DATA_POINT_SUMMARY = "DATA_POINT_SUMMARY";
    
    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="requestType")
    @JsonSubTypes({
        @JsonSubTypes.Type(name = REQUEST_TYPE_SUBSCRIPTION, value = EventsSubscriptionRequest.class),
        @JsonSubTypes.Type(name = REQUEST_TYPE_DATA_POINT_SUMMARY, value = EventsDataPointSummaryRequest.class)
    })
    public static abstract class EventsWebsocketRequest extends WebSocketRequest implements Validatable {
    }
    public static class EventsDataPointSummaryRequest extends EventsWebsocketRequest {
        private String[] dataPointXids;
        public void setDataPointXids(String[] dataPointXids) {
            this.dataPointXids = dataPointXids;
        }
        public String[] getDataPointXids() {
            return dataPointXids;
        }
        @Override
        public void validate(ProcessResult response) {
            if(dataPointXids == null || dataPointXids.length == 0) {
                response.addContextualMessage("dataPointXids", "validate.invalidValue");
            }
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
    private final EventInstanceService service;
    
    private volatile Set<AlarmLevels> levels;
    private volatile EnumSet<EventActionEnum> actions;
    private WebSocketSession session;
    private User user;
    private final Object lock = new Object();
    
    @Autowired
    public EventsWebSocketHandler(RestModelMapper modelMapper, EventInstanceService service) {
        super(true);
        this.modelMapper = modelMapper;
        this.service = service;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Check for permissions
        this.user = this.getUser(session);
        if (this.user == null) {
            return;
        } 
        this.session = session;
        session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, Boolean.FALSE);
        super.afterConnectionEstablished(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized(this.lock) {
            try {
                Boolean subscribed = (Boolean)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
                if(subscribed) {
                    terminate();
                    session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, Boolean.FALSE);
                }
            }catch(Exception e) {
                LOG.error("Failed to terminate user event lister", e);
            }
        }
        super.afterConnectionClosed(session, status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        synchronized(this.lock) {
            try {
                Boolean subscribed = (Boolean)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
                if(subscribed) {
                    terminate();
                    session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, Boolean.FALSE);
                }
            }catch(Exception e) {
                LOG.error("Failed to terminate user event lister", e);
            }
        }
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
    
                Set<AlarmLevels> levels = subscription.getLevels();
                if (levels == null) {
                    levels = Collections.emptySet();
                }
                Set<EventActionEnum> actions = subscription.getActions();
                if (actions == null) {
                    actions = EnumSet.noneOf(EventActionEnum.class);
                }
                
                boolean emptySubscriptions = levels.isEmpty() || actions.isEmpty();
                synchronized(this.lock) {
                    //Configure listener
                    Boolean subscribed = (Boolean)session.getAttributes().get(SUBSCRIPTION_ATTRIBUTE);
                    if(subscribed == null || subscribed == Boolean.FALSE) {
                        if(!emptySubscriptions) {
                            changeLevels(levels);
                            changeActions(actions);
                            initialize();
                            session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, Boolean.TRUE);
                        }
                    }else {
                        if(emptySubscriptions) {
                            terminate();
                            session.getAttributes().put(SUBSCRIPTION_ATTRIBUTE, Boolean.FALSE);
                        }else {
                            changeActions(actions);
                            changeLevels(levels);
                        }
                    }
                }
                if(subscription.isSendEventLevelSummaries()) {
                    WebSocketResponse<List<EventLevelSummaryModel>> response = new WebSocketResponse<>(request.getSequenceNumber());
                    List<UserEventLevelSummary> summaries = service.getActiveSummary(user);
                    List<EventLevelSummaryModel> models = summaries.stream().map(s -> { 
                            EventInstanceModel instanceModel = s.getLatest() != null ? modelMapper.map(s.getLatest(), EventInstanceModel.class, user) : null;
                            return new EventLevelSummaryModel(s.getAlarmLevel(), s.getUnsilencedCount(), instanceModel);
                        }).collect(Collectors.toList());
                    response.setPayload(models);
                    this.sendRawMessage(session, response);
                }else {
                    this.sendRawMessage(session, new WebSocketResponse<Void>(request.getSequenceNumber()));
                }
            }else if(request instanceof EventsDataPointSummaryRequest) {
                EventsDataPointSummaryRequest query = (EventsDataPointSummaryRequest)request;
                WebSocketResponse<List<DataPointEventSummaryModel>> response = new WebSocketResponse<>(request.getSequenceNumber());
                Collection<DataPointEventLevelSummary> summaries = service.getDataPointEventSummaries(query.getDataPointXids(), user);
                List<DataPointEventSummaryModel> models = summaries.stream().map(s -> new DataPointEventSummaryModel(s.getXid(), s.getCounts())).collect(Collectors.toList());
                response.setPayload(models);
                this.sendRawMessage(session, response);
            }
        } catch(NotFoundException e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.NOT_FOUND, e.getTranslatableMessage());
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
            }
        }catch(PermissionException e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, e.getTranslatableMessage());
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
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
