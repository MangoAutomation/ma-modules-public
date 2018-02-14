/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleNotificationListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleNotificationModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleNotificationTypeEnum;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * 
 * @author Terry Packer
 */
public class ModulesWebSocketHandler extends MangoWebSocketHandler implements ModuleNotificationListener{
	private static final Log LOG = LogFactory.getLog(ModulesWebSocketHandler.class);
			
    final Set<WebSocketSession> sessions = new HashSet<WebSocketSession>();
    final ReadWriteLock lock = new ReentrantReadWriteLock();
        
    public ModulesWebSocketHandler(){
        ModulesDwr.addModuleNotificationListener(this);
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = getUser(session);
        // TODO Mango 3.4 replace close status with constant from MangoWebSocketPublisher
        if (user == null) {
            return;
        } else if (!hasPermission(user)) {
            if (session.isOpen()) {
                session.close(new CloseStatus(4003, "Not authorized"));
            }
            return;
        }

    	super.afterConnectionEstablished(session);
        lock.writeLock().lock();
        try {
            sessions.add(session);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        
        lock.writeLock().lock();
        try {
            sessions.remove(session);
        } finally {
            lock.writeLock().unlock();
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleDownloaded(java.lang.String, java.lang.String)
	 */
	@Override
	public void moduleDownloaded(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_DOWNLOADED, name, version, null);
		notify(model);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleUpgradeAvailable(java.lang.String, java.lang.String)
	 */
	@Override
	public void moduleUpgradeAvailable(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_UPGRADE_AVAILABLE, name, version, null);
		notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleUpgradeAvailable(java.lang.String, java.lang.String)
	 */
	@Override
	public void newModuleAvailable(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.NEW_MODULE_AVAILABLE, name, version, null);
		notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#upgradeStateChanged(java.lang.String)
	 */
	@Override
	public void upgradeStateChanged(UpgradeState stage) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_STATE_CHANGE, stage);
		notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleDownloadFailed(java.lang.String, java.lang.String)
	 */
	@Override
	public void moduleDownloadFailed(String name, String version, String reason) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_DOWNLOAD_FAILED, name, version, reason);
        notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#upgradeError(java.lang.String)
	 */
	@Override
	public void upgradeError(String error) {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_ERROR, null, null, error);
        notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#upgradeTaskFinished()
	 */
	@Override
	public void upgradeTaskFinished() {
        ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_FINISHED, null, null, null);
        notify(model);
	}
	
    public void notify(ModuleNotificationModel model) {
        lock.readLock().lock();
        try {
            for (WebSocketSession session : sessions) {
                User user = getUser(session);
                if (user != null && hasPermission(user)) {
                    notify(session, model);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }
	
    protected void notify(WebSocketSession session, ModuleNotificationModel model) {
        try {
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
    
    protected boolean hasPermission(User user){
        return user.isAdmin();
    }
	
}
