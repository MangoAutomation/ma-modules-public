/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.v2.model.modules.ModuleNotificationModel;
import com.infiniteautomation.mango.rest.v2.model.modules.ModuleNotificationTypeEnum;
import com.infiniteautomation.mango.spring.service.ModulesService;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleNotificationListener;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/modules")
public class ModulesWebSocketHandler extends MultiSessionWebSocketHandler implements ModuleNotificationListener {

    // superadmin only
    private static final MangoPermission REQUIRED_PERMISSION = new MangoPermission();

    @Autowired
    public ModulesWebSocketHandler(ModulesService service) {
        super();
        service.addModuleNotificationListener(this);
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
            PermissionHolder user = getUser(session);
            if (hasPermission(user)) {
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

    protected boolean hasPermission(PermissionHolder user) {
        return permissionService.hasPermission(user, requiredPermission());
    }

    @Override
    protected MangoPermission requiredPermission() {
        return REQUIRED_PERMISSION;
    }
}
