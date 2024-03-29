/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.websocket.pointValue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.websocket.MangoWebSocketErrorType;
import com.infiniteautomation.mango.rest.latest.websocket.MangoWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketSendException;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.Functions;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Event handler for single web socket session to publish events for multiple data points
 *
 * @author Terry Packer
 * @author Jared Wiltshire
 */
public class PointValueWebSocketHandler extends MangoWebSocketHandler {

    private final Map<Integer, PointValueWebSocketListener> pointIdToListenerMap = new HashMap<>();
    private boolean connectionClosed = false;
    private WebSocketSession session;
    private final PermissionService permissionService;
    private final DataPointDao dataPointDao;
    private DataPointService datapointService;

    @Autowired
    public PointValueWebSocketHandler(PermissionService permissionService, DataPointDao dataPointDao,
                                      DataPointService datapointService) {
        super();
        this.permissionService = permissionService;
        this.dataPointDao = dataPointDao;
        this.datapointService = datapointService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session = session;
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

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            PermissionHolder user = getUser(session);
            PointValueRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), PointValueRegistrationModel.class);
            DataPointVO vo;

            try {
                //This will check for not found and permissions
                vo = datapointService.get(model.getDataPointXid());
            } catch(NotFoundException e) {
                //send not found message back
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.pointNotFound", model.getDataPointXid()));
                return;
            } catch(PermissionException e) {
                //Send permission denied message here
                this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED,
                        new TranslatableMessage("permission.exception.readDataPoint", user.getPermissionHolderName()));
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

        } catch (WebSocketSendException e) {
            log.warn("Error sending websocket message", e);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error("An error occurred", e);
            }
        }

        if(log.isDebugEnabled()) {
            log.debug(message.getPayload());
        }
    }

    protected void sendMessage(Object payload) throws JsonProcessingException, Exception {
        super.sendMessage(session, payload);
    }

    /**
     * @author Terry Packer
     * @author Jared Wiltshire
     */
    public class PointValueWebSocketListener implements DataPointListener {
        private DataPointVO vo;
        private DataPointRT rt;
        private volatile EnumSet<PointValueEventType> eventTypes;

        public PointValueWebSocketListener(DataPointVO vo,  Set<PointValueEventType> eventTypes) {
            this.vo = vo;
            this.setEventTypes(eventTypes);
        }

        private void sendNotification(PointValueEventType eventType, PointValueTime pvt) throws JsonProcessingException, Exception {
            boolean enabled = false;
            boolean pointEnabled = false;
            Map<String, Object> attributes = null;
            Double convertedValue = null;
            String renderedValue = null;
            DataPointRT dprt = rt;

            if (dprt != null) {
                enabled = true; //We are enabled
                pointEnabled = true; //Must be if we are running
                if (pvt == null) {
                    pvt = dprt.getPointValue(); //Get the value
                }
                attributes = new HashMap<>(dprt.getAttributes());
            } else {
                pointEnabled = dataPointDao.isEnabled(vo.getId());
            }

            PointValueTimeModel pvtModel = null;
            if (pvt != null) {
                pvtModel = new PointValueTimeModel(pvt);

                renderedValue = Functions.getRenderedText(vo, pvt);
                if (vo.getPointLocator().getDataType() == DataType.NUMERIC) {
                    convertedValue = vo.getRenderedUnitConverter().convert(pvt.getValue().getDoubleValue());
                }
            }

            sendMessage(new PointValueEventModel(vo.getXid(), enabled, pointEnabled, attributes, eventType, pvtModel, renderedValue, convertedValue));
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
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        @Override
        public void pointInitialized() {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                rt = Common.runtimeManager.getDataPoint(vo.getId()); //Set us up
                vo = rt.getVO();

                if (this.eventTypes.contains(PointValueEventType.INITIALIZE)) {
                    sendNotification(PointValueEventType.INITIALIZE, null);
                }
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
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
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
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
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
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
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        @Override
        public void pointBackdated(PointValueTime value) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.BACKDATE)) {
                    sendNotification(PointValueEventType.BACKDATE, value);
                }
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        @Override
        public void attributeChanged(Map<String, Object> attributes) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.ATTRIBUTE_CHANGE)) {
                    sendMessage(new PointValueEventModel(vo.getXid(), true, true, attributes, PointValueEventType.ATTRIBUTE_CHANGE, null, null, null));
                }
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        @Override
        public void pointTerminated(DataPointVO dp) {
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                this.rt = null;
                if (this.eventTypes.contains(PointValueEventType.TERMINATE)){
                    sendMessage(new PointValueEventModel(vo.getXid(), false, dp.isEnabled(), null, PointValueEventType.TERMINATE, null, null, null));
                }
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        /**
         * Re-set the event types
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
            try {
                if (!session.isOpen() || getUser(session) == null) {
                    this.terminate();
                }

                if (this.eventTypes.contains(PointValueEventType.LOGGED)) {
                    sendNotification(PointValueEventType.LOGGED, value);
                }
            } catch (WebSocketSendException e) {
                log.warn("Error sending websocket message", e);
            } catch (Exception e) {
                log.error("An error occurred", e);
            }
        }

        @Override
        public String getListenerName() {
            return "Websocket for DP " + this.vo.getXid() + "'s point values.";
        }
    }
}
