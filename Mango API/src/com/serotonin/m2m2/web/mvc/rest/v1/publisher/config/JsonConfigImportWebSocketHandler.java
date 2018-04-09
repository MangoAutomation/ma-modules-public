package com.serotonin.m2m2.web.mvc.rest.v1.publisher.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller;
import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;
import com.serotonin.m2m2.web.mvc.websocket.MultiSessionWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.WebSocketClosedException;

public class JsonConfigImportWebSocketHandler extends MultiSessionWebSocketHandler {

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
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            User user = this.getUser(session);
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
                log.error(e.getMessage(), e);
            }
        }
    }

    public void notify(ImportStatusProvider model) {
        for (WebSocketSession session : sessions) {
            User user = getUser(session);
            if (user != null) {
                notify(session, model);
            }
        }
    }

    protected void notify(WebSocketSession session, ImportStatusProvider model) {
        try {
            sendMessage(session, model);
        } catch(WebSocketClosedException e) {
            log.warn("Tried to notify closed websocket session", e);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    public void setController(JsonEmportV2Controller controller) {
        this.controller = controller;
    }
}
