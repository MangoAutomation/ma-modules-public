/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleNotificationListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleNotificationModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleNotificationTypeEnum;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.MultiSessionWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.WebSocketSendException;

/**
 *
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/modules")
public class ModulesWebSocketHandler extends MultiSessionWebSocketHandler implements ModuleNotificationListener {

    public ModulesWebSocketHandler() {
        super(true);
        ModulesDwr.addModuleNotificationListener(this);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = getUser(session);
        if (user == null) {
            return;
        } else if (!hasPermission(user)) {
            if (session.isOpen()) {
                session.close(MangoWebSocketHandler.NOT_AUTHORIZED);
            }
            return;
        }

        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void moduleDownloaded(String name, String version) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_DOWNLOADED, name, version, null);
        notify(model);
    }

    @Override
    public void moduleUpgradeAvailable(String name, String version) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_UPGRADE_AVAILABLE, name, version, null);
        notify(model);
    }

    @Override
    public void newModuleAvailable(String name, String version) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.NEW_MODULE_AVAILABLE, name, version, null);
        notify(model);
    }

    @Override
    public void upgradeStateChanged(UpgradeState stage) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_STATE_CHANGE, stage);
        notify(model);
    }

    @Override
    public void moduleDownloadFailed(String name, String version, String reason) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_DOWNLOAD_FAILED, name, version, reason);
        notify(model);
    }

    @Override
    public void upgradeError(String error) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_ERROR, null, null, error);
        notify(model);
    }

    @Override
    public void upgradeTaskFinished() {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_FINISHED, null, null, null);
        notify(model);
    }

    public void notify(ModuleNotificationModel model) {
        for (WebSocketSession session : sessions) {
            User user = getUser(session);
            if (user != null && hasPermission(user)) {
                notify(session, model);
            }
        }
    }

    protected void notify(WebSocketSession session, ModuleNotificationModel model) {
        try {
            sendMessage(session, model);
        } catch(WebSocketSendException e) {
            log.warn("Error notifying websocket session", e);
        }  catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR,
                        new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    protected boolean hasPermission(User user){
        return user.hasAdminPermission();
    }

}
