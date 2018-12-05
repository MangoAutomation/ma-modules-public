package com.infiniteautomation.mango.rest.v2.websocket;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.infiniteautomation.mango.rest.v2.JsonEmportV2Controller.ImportStatusProvider;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.infiniteautomation.mango.rest.v2.websocket.MangoWebSocketErrorType;
import com.infiniteautomation.mango.rest.v2.websocket.MangoWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.MultiSessionWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketSendException;

@Component("jsonConfigImportWebSocketHandlerV2")
@WebSocketMapping("/websocket/json-import")
public class JsonConfigImportWebSocketHandler extends MultiSessionWebSocketHandler {

    // For our reference to cancel the tasks
    private final MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources;

    @Autowired
    public JsonConfigImportWebSocketHandler(MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources) {
        this.importStatusResources = importStatusResources;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Check for permissions
        User user = this.getUser(session);
        if (user == null) {
            return;
        } else if (!user.hasAdminPermission()) {
            if (session.isOpen()) {
                session.close(MangoWebSocketHandler.NOT_AUTHORIZED);
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
            } else if (!user.hasAdminPermission()) {
                if (session.isOpen()) {
                    session.close(MangoWebSocketHandler.NOT_AUTHORIZED);
                }
                return;
            }

            JsonEmportControlModel model = this.jacksonMapper.readValue(message.getPayload(),
                    JsonEmportControlModel.class);

            if (model != null && model.isCancel()) {
                // Cancel the task if it is running
                this.importStatusResources.get(model.getResourceId()).cancel();
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
}
