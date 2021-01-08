/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.latest.model.ArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.event.DataPointEventSummaryModel;
import com.infiniteautomation.mango.rest.latest.model.event.EventActionEnum;
import com.infiniteautomation.mango.rest.latest.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.latest.model.event.EventLevelSummaryModel;
import com.infiniteautomation.mango.spring.service.EventInstanceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.*;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.Validatable;
import com.serotonin.m2m2.vo.permission.PermissionException;
import net.jazdw.rql.parser.ASTNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Terry Packer
 *
 */
public class EventsWebSocketHandler extends MangoWebSocketHandler implements UserEventListener {

    private final static Log LOG = LogFactory.getLog(EventsWebSocketHandler.class);

    public static final String SUBSCRIPTION_ATTRIBUTE = "EventNotificationSubscription";
    public static final String REQUEST_TYPE_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String REQUEST_TYPE_DATA_POINT_SUMMARY = "DATA_POINT_SUMMARY";
    public static final String REQUEST_TYPE_ALL_ACTIVE_EVENTS = "ALL_ACTIVE_EVENTS";
    public static final String REQUEST_TYPE_ACTIVE_EVENTS_QUERY = "ACTIVE_EVENTS_QUERY";

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="requestType")
    @JsonSubTypes({
        @JsonSubTypes.Type(name = REQUEST_TYPE_SUBSCRIPTION, value = EventsSubscriptionRequest.class),
        @JsonSubTypes.Type(name = REQUEST_TYPE_DATA_POINT_SUMMARY, value = EventsDataPointSummaryRequest.class),
        @JsonSubTypes.Type(name = REQUEST_TYPE_ALL_ACTIVE_EVENTS, value = AllActiveEventsRequest.class),
        @JsonSubTypes.Type(name = REQUEST_TYPE_ACTIVE_EVENTS_QUERY, value = ActiveEventsQuery.class)
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

    public static class AllActiveEventsRequest extends EventsWebsocketRequest {
        @Override
        public void validate(ProcessResult response) { }
    }

    public static class EventsSubscriptionRequest extends EventsWebsocketRequest {
        private Set<AlarmLevels> levels;
        private Set<EventActionEnum> actions;
        private boolean sendActiveSummary;
        private boolean sendUnacknowledgedSummary;

        @Override
        public void validate(ProcessResult response) { }

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

        public boolean isSendActiveSummary() {
            return sendActiveSummary;
        }

        public void setSendActiveSummary(boolean sendActiveSummary) {
            this.sendActiveSummary = sendActiveSummary;
        }

        public boolean isSendUnacknowledgedSummary() {
            return sendUnacknowledgedSummary;
        }

        public void setSendUnacknowledgedSummary(boolean sendUnacknowledgedSummary) {
            this.sendUnacknowledgedSummary = sendUnacknowledgedSummary;
        }

    }

    public static class EventsSubscriptionResponse {
        List<EventLevelSummaryModel> activeSummary;
        List<EventLevelSummaryModel> unacknowledgedSummary;
        public List<EventLevelSummaryModel> getActiveSummary() {
            return activeSummary;
        }
        public void setActiveSummary(List<EventLevelSummaryModel> activeSummary) {
            this.activeSummary = activeSummary;
        }
        public List<EventLevelSummaryModel> getUnacknowledgedSummary() {
            return unacknowledgedSummary;
        }
        public void setUnacknowledgedSummary(List<EventLevelSummaryModel> unacknowledgedSummary) {
            this.unacknowledgedSummary = unacknowledgedSummary;
        }
    }

    public static class ActiveEventsQuery extends EventsWebsocketRequest {
        private String query;

        public String getQuery() {
            return query;
        }
        public void setQuery(String query) {
            this.query = query;
        }
        @Override
        public void validate(ProcessResult response) { }
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
        super();
        this.modelMapper = modelMapper;
        this.service = service;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Check for permissions, must be a User for UserEventListener
        this.user = (User) this.getUser(session);
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
            User user = (User) this.getUser(session);
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

                EventsSubscriptionResponse response = new EventsSubscriptionResponse();
                if (subscription.isSendActiveSummary()) {
                    List<UserEventLevelSummary> summaries = service.getActiveSummary();

                    List<EventLevelSummaryModel> models = summaries.stream().map(s -> {
                        EventInstanceModel instanceModel = s.getLatest() != null ? modelMapper.map(s.getLatest(), EventInstanceModel.class, user) : null;
                        return new EventLevelSummaryModel(s.getAlarmLevel(), s.getCount(), instanceModel);
                    }).collect(Collectors.toList());

                    response.setActiveSummary(models);
                }
                if (subscription.isSendUnacknowledgedSummary()) {
                    List<UserEventLevelSummary> summaries = service.getUnacknowledgedSummary();

                    List<EventLevelSummaryModel> models = summaries.stream().map(s -> {
                        EventInstanceModel instanceModel = s.getLatest() != null ? modelMapper.map(s.getLatest(), EventInstanceModel.class, user) : null;
                        return new EventLevelSummaryModel(s.getAlarmLevel(), s.getCount(), instanceModel);
                    }).collect(Collectors.toList());

                    response.setUnacknowledgedSummary(models);
                }
                this.sendRawMessage(session, new WebSocketResponse<>(request.getSequenceNumber(), response));
            }else if(request instanceof EventsDataPointSummaryRequest) {
                EventsDataPointSummaryRequest query = (EventsDataPointSummaryRequest)request;
                WebSocketResponse<List<DataPointEventSummaryModel>> response = new WebSocketResponse<>(request.getSequenceNumber());
                Collection<DataPointEventLevelSummary> summaries = service.getDataPointEventSummaries(query.getDataPointXids());
                List<DataPointEventSummaryModel> models = summaries.stream().map(s -> new DataPointEventSummaryModel(s.getXid(), s.getCounts())).collect(Collectors.toList());
                response.setPayload(models);
                this.sendRawMessage(session, response);
            }else if(request instanceof AllActiveEventsRequest) {
                WebSocketResponse<List<EventInstanceModel>> response = new WebSocketResponse<>(request.getSequenceNumber());
                List<EventInstance> active = service.getAllActiveUserEvents();
                List<EventInstanceModel> models = new ArrayList<>(active.size());
                for(EventInstance vo : active) {
                    models.add(modelMapper.map(vo, EventInstanceModel.class, user));
                }
                response.setPayload(models);
                this.sendRawMessage(session, response);
            } else if (request instanceof ActiveEventsQuery) {
                List<EventInstance> active = service.getAllActiveUserEvents();
                List<EventInstanceModel> models = new ArrayList<>(active.size());
                for(EventInstance vo : active) {
                    models.add(modelMapper.map(vo, EventInstanceModel.class, user));
                }

                String query = ((ActiveEventsQuery) request).getQuery();
                ASTNode rql = RQLUtils.parseRQLtoAST(query);

                WebSocketResponse<ArrayWithTotal<Stream<EventInstanceModel>>> response = new WebSocketResponse<>(request.getSequenceNumber());
                response.setPayload(new FilteredStreamWithTotal<>(models, rql, user.getTranslations()));
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

    protected void notify(EventActionEnum action, EventInstance event, WebSocketSession session) {
        //This is used for serialization where things like the TranslatableMessageSerializer
        this.runAs.runAs(user, () -> {
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
        });
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
            EventsWebSocketHandler.this.notify(EventActionEnum.RAISED, evt, session);
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
            EventsWebSocketHandler.this.notify(EventActionEnum.RETURN_TO_NORMAL, evt, session);
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
            EventsWebSocketHandler.this.notify(EventActionEnum.DEACTIVATED, evt, session);
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
            EventsWebSocketHandler.this.notify(EventActionEnum.ACKNOWLEDGED, evt, session);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error notifying of event acknowledged", e);
            }
        }
    }
}
