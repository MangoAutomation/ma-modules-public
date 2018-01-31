/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.jetty.JettyWebSocketSession;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * TODO review the code in MangoWebSocketPublisher for the next Mango core release v3.4.x
 * Make WebSocketDefinition return a more generic web socket handler
 * 
 * @author Jared Wiltshire
 */
public class MangoV2WebSocketHandler extends MangoWebSocketHandler {
    protected final Log log = LogFactory.getLog(this.getClass());

    protected void sendMessage(WebSocketSession session, WebSocketMessage message) throws JsonProcessingException, IOException {
        this.sendStringMessage(session, this.jacksonMapper.writeValueAsString(message));
    }
    
    protected void sendMessageUsingView(WebSocketSession session, WebSocketMessage message, Class<?> view) throws JsonProcessingException, IOException {
        ObjectWriter objectWriter = this.jacksonMapper.writerWithView(view);
        this.sendStringMessage(session, objectWriter.writeValueAsString(message));
    }
    
    protected void sendStringMessage(WebSocketSession session, String message) {
        JettyWebSocketSession jettySession = (JettyWebSocketSession) session;
        jettySession.getNativeSession().getRemote().sendStringByFuture(message);
    }

    public static enum WebSocketMessageType {
        RESPONSE, NOTIFICATION
    }
    
    public static interface WebSocketMessage {
        public WebSocketMessageType getMessageType() ;
    }
    
    public static class WebSocketResponse<T> implements WebSocketMessage {
        int sequenceNumber;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T payload;
        
        public WebSocketResponse() {
        }
        
        public WebSocketResponse(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        public void setSequenceNumber(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public WebSocketMessageType getMessageType() {
            return WebSocketMessageType.RESPONSE;
        }

        public T getPayload() {
            return payload;
        }

        public void setPayload(T payload) {
            this.payload = payload;
        }
    }

    public static class WebSocketNotification<T> implements WebSocketMessage {
        /**
         * Use CrudNotificationType where possible
         */
        String notificationType;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T payload;
        
        public WebSocketNotification() {
        }
        
        public WebSocketNotification(CrudNotificationType notificationType, T payload) {
            this.notificationType = notificationType.getNotificationType();
            this.payload = payload;
        }

        @Override
        public WebSocketMessageType getMessageType() {
            return WebSocketMessageType.NOTIFICATION;
        }

        public T getPayload() {
            return payload;
        }

        public void setPayload(T payload) {
            this.payload = payload;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }
    }

    public static abstract class WebSocketRequest {
        int sequenceNumber;

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        public void setSequenceNumber(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }
    }
}
