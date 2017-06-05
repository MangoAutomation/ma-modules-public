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
    	if(!hasPermission(getUser(session))){
    		this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, new TranslatableMessage("common.default", "Permission Denied"));
    		session.close();
    	}

    	super.afterConnectionEstablished(session);
    	lock.writeLock().lock();
    	try{
    		sessions.add(session);
    	}finally{
    		lock.writeLock().unlock();
    	}
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	lock.writeLock().lock();
    	try{
    		sessions.remove(session);
    	}finally{
    		lock.writeLock().unlock();
    	}
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleDownloaded(java.lang.String, java.lang.String)
	 */
	@Override
	public void moduleDownloaded(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_DOWNLOADED, name, version);
		notify(model);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleUpgradeAvailable(java.lang.String, java.lang.String)
	 */
	@Override
	public void moduleUpgradeAvailable(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.MODULE_UPGRADE_AVAILABLE, name, version);
		notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#moduleUpgradeAvailable(java.lang.String, java.lang.String)
	 */
	@Override
	public void newModuleAvailable(String name, String version) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.NEW_MODULE_AVAILABLE, name, version);
		notify(model);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.ModuleNotificationListener#upgradeStateChanged(java.lang.String)
	 */
	@Override
	public void upgradeStateChanged(String stage) {
		ModuleNotificationModel model = new ModuleNotificationModel(ModuleNotificationTypeEnum.UPGRADE_STATE_CHANGE, stage);
		notify(model);
	}
	
    public void notify(ModuleNotificationModel model) {
    	lock.readLock().lock();
    	try{
	        for (WebSocketSession session : sessions) {
	            if (hasPermission(getUser(session))) {
	                notify(session, model);
	            }
	        }
    	}finally{
    		lock.readLock().unlock();
    	}
    }
	
    protected void notify(WebSocketSession session, ModuleNotificationModel model) {
        try {
            sendMessage(session, model);
        } catch (Exception e) {
            try {
                this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
            } catch (Exception e1) {
                LOG.error(e1.getMessage(), e1);
            }
        }
    }
    
    protected boolean hasPermission(User user){
    	return user.isAdmin();
    }
	
}
