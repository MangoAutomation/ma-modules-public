package com.serotonin.m2m2.web.mvc.rest.v1.publisher.config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.JsonEmportRestController.ImportBackgroundTask;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;
import com.serotonin.util.ProgressiveTaskListener;

public class JsonConfigImportWebSocketHandler extends MangoWebSocketHandler implements ProgressiveTaskListener {
	
	private static final Log LOG = LogFactory.getLog(JsonConfigImportWebSocketHandler.class);
	final Set<WebSocketSession> sessions = new HashSet<WebSocketSession>();
	final ReadWriteLock lock = new ReentrantReadWriteLock();
	private ImportBackgroundTask task;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		//Check for permissions
		User user = this.getUser(session);
		if(user == null){
			this.sendErrorMessage(session, MangoWebSocketErrorType.NOT_LOGGED_IN, new TranslatableMessage("rest.error.notLoggedIn"));
			session.close(CloseStatus.NOT_ACCEPTABLE);
			return;
		}else if(!user.isAdmin()){
			this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, new TranslatableMessage("rest.error.permissionDenied"));
			session.close(CloseStatus.NOT_ACCEPTABLE);
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
		lock.writeLock().lock();
		try {
			sessions.remove(session);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	//TODO Handle Incoming Cancel Message
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {	
			User user = this.getUser(session);
			if(user == null || !user.isAdmin()){
				//TODO Can anyone cancel the import?
				this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, new TranslatableMessage("rest.error.permissionDenied"));
				session.close(CloseStatus.NOT_ACCEPTABLE);
				return;
			}
			if(StringUtils.equals(message.getPayload(), "CANCEL")){
				//Cancel the task if it is running
				if(task != null){
					task.cancel();
				}
			}
		} catch (Exception e) {
			try {
				this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
			} catch (Exception e1) {
				LOG.error(e.getMessage(), e);
			}
		} 
	}
	
    public void notify(JsonConfigImportStatusModel model) {
    	lock.readLock().lock();
    	try{
	        for (WebSocketSession session : sessions)
                notify(session, model);
    	}finally{
    		lock.readLock().unlock();
    	}
    }
	
    protected void notify(WebSocketSession session, JsonConfigImportStatusModel model) {
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
    
    public void setTask(ImportBackgroundTask task){
    	this.task = task;
    }

	@Override
	public void progressUpdate(float progress) {
		this.notify(new JsonConfigImportStatusModel(JsonConfigImportStateEnum.RUNNING, progress));
	}

	@Override
	public void taskCancelled() {
		this.notify(new JsonConfigImportStatusModel(JsonConfigImportStateEnum.CANCELLED, 0.0f));
	}

	@Override
	public void taskCompleted() {
		notify(new JsonConfigImportStatusModel(JsonConfigImportStateEnum.COMPLETED, this.task.getResponse(), 100f));
	}
	
}
