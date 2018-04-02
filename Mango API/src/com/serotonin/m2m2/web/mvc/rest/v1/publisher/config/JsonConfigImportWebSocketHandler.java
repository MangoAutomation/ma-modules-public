package com.serotonin.m2m2.web.mvc.rest.v1.publisher.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller;
import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

public class JsonConfigImportWebSocketHandler extends MangoWebSocketPublisher {

    private static final Log LOG = LogFactory.getLog(JsonConfigImportWebSocketHandler.class);
    final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // For our reference to cancel the tasks
    private JsonEmportV2Controller controller;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Check for permissions
        User user = this.getUser(session);
        if (user == null) {
            return;
        } else if (!user.isAdmin()) {
            if (session.isOpen()) {
                session.close(MangoWebSocketPublisher.NOT_AUTHORIZED);
            }
            return;
        }

        super.afterConnectionEstablished(session);
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            User user = this.getUser(session);
            // TODO Can anyone cancel the import?
            if (user == null) {
                return;
            } else if (!user.isAdmin()) {
                if (session.isOpen()) {
                    session.close(MangoWebSocketPublisher.NOT_AUTHORIZED);
                }
                return;
            }

            JsonEmportControlModel model = this.jacksonMapper.readValue(message.getPayload(),
                    JsonEmportControlModel.class);
            if (model != null && model.isCancel()) {
                // Cancel the task if it is running
                this.controller.cancelImport(model.getResourceId());
            }
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public void notify(ImportStatusProvider model) {
        for (WebSocketSession session : sessions)
            notify(session, model);
    }

    protected void notify(WebSocketSession session, ImportStatusProvider model) {
        try {
            User user = this.getUser(session);
            if (user == null) {
                return;
            }
            
            sendMessage(session, model);
        } catch (Exception e) {
            // TODO Mango 3.4 add new exception type for closed session and don't try and send error if it was a closed session exception
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                LOG.error(e1.getMessage(), e1);
            }
        }
    }

    public void setController(JsonEmportV2Controller controller) {
        this.controller = controller;
    }
}
