/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * Event Handler for 1 Web socket session to publish events for multiple data points
 * 
 * @author Terry Packer
 * @author Jared Wiltshire
 */
public class PointValueWebSocketHandler extends MangoWebSocketPublisher {

    private final Log log = LogFactory.getLog(this.getClass());
    private final Map<Integer, PointValueWebSocketListener> pointIdToListenerMap = new HashMap<>();
    private boolean connectionClosed = false;
    private WebSocketSession session;

    public PointValueWebSocketHandler(){
        super();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session = session;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        try {
            User user = getUser(session);
            if (user == null) {
                return;
            }
            PointValueRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), PointValueRegistrationModel.class);

            // Handle message.getPayload() here
            DataPointVO vo = DataPointDao.instance.getByXid(model.getDataPointXid());
            if (vo == null) {
                this.sendErrorMessage(session,MangoWebSocketErrorType.SERVER_ERROR, 
                        new TranslatableMessage("rest.error.pointNotFound", model.getDataPointXid()));
                return;
            }

            //Check permissions
            if(!Permissions.hasDataPointReadPermission(user, vo)){
                this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, 
                        new TranslatableMessage("permission.exception.readDataPoint", user.getUsername()));
                return;
            }

            Set<PointValueEventType> eventsTypes = model.getEventTypes();
            int dataPointId = vo.getId();
            
            synchronized(pointIdToListenerMap) {
                if (this.connectionClosed) {
                    return;
                }
                
                PointValueWebSocketListener publisher = pointIdToListenerMap.get(dataPointId);

                if (publisher != null) {
                    if (eventsTypes.isEmpty()) {
                        publisher.terminate();
                        pointIdToListenerMap.remove(dataPointId);
                    } else {
                        publisher.setEventTypes(eventsTypes);
                    }
                } else if (!eventsTypes.isEmpty()) {
                    publisher = new PointValueWebSocketListener(vo, eventsTypes);
                    publisher.initialize();
                    //Immediately send the most recent Point Value and the status of the data point
                    publisher.sendPointStatus();
                    pointIdToListenerMap.put(dataPointId, publisher);
                }
            }
            
        } catch (Exception e) {
            // TODO Mango 3.4 add new exception type for closed session and don't try and send error if it was a closed session exception
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
            }
        } 
        if(log.isDebugEnabled())
            log.debug(message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        synchronized(pointIdToListenerMap) {
            if (!this.connectionClosed) {
                this.connectionClosed = true;
                for (Entry<Integer, PointValueWebSocketListener> entry : pointIdToListenerMap.entrySet()) {
                    PointValueWebSocketListener pub = entry.getValue();
                    pub.terminate();
                }
            }
        }

        // Handle closing connection here
        if (log.isDebugEnabled()) {
            log.debug("Websocket connection closed, status code: " + status.getCode() + ", reason: " + status.getReason());
        }
    }

    protected void sendMessage(Object payload) throws JsonProcessingException, Exception {
        // not uncommon that the connection is closed and listener terminated but notification still received
        // especially on logout
        if (session.isOpen()) {
            super.sendMessage(session, payload);
        }
    }

    /**
     * @author Terry Packer
     * @author Jared Wiltshire
     */
    public class PointValueWebSocketListener implements DataPointListener {
        private DataPointVO vo;
        private DataPointRT rt;
        private UriComponentsBuilder imageServletBuilder;
        private volatile EnumSet<PointValueEventType> eventTypes;

        public PointValueWebSocketListener(DataPointVO vo,  Set<PointValueEventType> eventTypes) {
            this.vo = vo;

            //If we are an image type we should build the URLS
            if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/{ts}_{id}.jpg");

            this.setEventTypes(eventTypes);
        }

        private void sendNotification(PointValueEventType eventType, PointValueTime pvt) throws JsonProcessingException, Exception {
            boolean enabled = false;
            Map<String,Object> attributes = null;
            Double convertedValue = null;
            String renderedValue = null;
            if (rt != null) {
                enabled = true; //We are enabled
                if (pvt == null) {
                    pvt = rt.getPointValue(); //Get the value
                }
                attributes = new HashMap<>(rt.getAttributes());
                renderedValue = Functions.getRenderedText(vo, pvt);
                if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC && (pvt != null)) {
                    convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue());
                }
            }
            
            PointValueTimeModel pvtModel = null;
            if (pvt != null) {
                pvtModel = new PointValueTimeModel(pvt);
                if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE) {
                    pvtModel.setValue(imageServletBuilder.buildAndExpand(pvt.getTime(), vo.getId()).toUri().toString());
                }
            }
            sendMessage(new PointValueEventModel(vo.getXid(), enabled, attributes, eventType, pvtModel, renderedValue, convertedValue));
        }

        /**
         * Initial response upon new registration
         */
        public void sendPointStatus() {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                rt = Common.runtimeManager.getDataPoint(vo.getId()); //Set us up
                sendNotification(PointValueEventType.REGISTERED, null);
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void pointInitialized() {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                rt = Common.runtimeManager.getDataPoint(vo.getId()); //Set us up

                if (this.eventTypes.contains(PointValueEventType.INITIALIZE)) {
                    sendNotification(PointValueEventType.INITIALIZE, null);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void pointUpdated(PointValueTime newValue) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.UPDATE)) {
                    sendNotification(PointValueEventType.UPDATE, newValue);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.CHANGE)) {
                    sendNotification(PointValueEventType.CHANGE, newValue);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.SET)) {
                    sendNotification(PointValueEventType.SET, newValue);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void pointBackdated(PointValueTime value) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.BACKDATE)) {
                    sendNotification(PointValueEventType.SET, value);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        @Override
        public void attributeChanged(Map<String, Object> attributes) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.ATTRIBUTE_CHANGE)) {
                    sendMessage(new PointValueEventModel(vo.getXid(), true, attributes, PointValueEventType.ATTRIBUTE_CHANGE, null, null, null));
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }

        @Override
        public void pointTerminated() {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                this.rt = null;

                if (this.eventTypes.contains(PointValueEventType.TERMINATE)){
                    sendMessage(new PointValueEventModel(vo.getXid(), false, null, PointValueEventType.TERMINATE, null, null, null));
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        /**
         * Re-set the event types
         * @param eventTypes
         */
        public void setEventTypes(Set<PointValueEventType> eventTypes) {
            this.eventTypes = EnumSet.copyOf(eventTypes);
        }

        public void initialize() {
            Common.runtimeManager.addDataPointListener(vo.getId(), this);
        }

        public void terminate() {
            Common.runtimeManager.removeDataPointListener(vo.getId(), this);
        }

        @Override
        public void pointLogged(PointValueTime value) {
            //Do nothing for now
        }

        @Override
        public String getListenerName() {
            return "Websocket for DP " + this.vo.getXid() + "'s point values.";
        }
    }
}
