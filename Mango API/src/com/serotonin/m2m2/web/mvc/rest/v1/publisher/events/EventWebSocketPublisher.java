/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.events;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventTypeEnum;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

/**
 * @author Terry Packer
 *
 */
public class EventWebSocketPublisher extends MangoWebSocketPublisher implements UserEventListener{

	private static final Log LOG = LogFactory.getLog(EventWebSocketPublisher.class);
	
	private User user;
	private WebSocketSession session;
	private List<String> levels;
	
	/**
	 * @param session 
	 * @param levels 
	 * @param user 
	 * @param jacksonMapper
	 */
	public EventWebSocketPublisher(User user, List<String> levels, WebSocketSession session, ObjectMapper jacksonMapper) {
		super(jacksonMapper);
		this.user = user;
		this.levels = levels;
		this.session = session;
	}

	public void initialize() {
		Common.eventManager.addUserEventListener(this);
	}
	public void terminate(){
		Common.eventManager.removeUserEventListener(this);
	}
	
	public WebSocketSession getSession(){
		return this.session;
	}
	
	public void setLevels(List<String> levels){
		this.levels = levels;
	}

	public void sendEvents(){
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.UserEventListener#getUserId()
	 */
	@Override
	public int getUserId() {
		return user.getId();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.UserEventListener#raised(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void raised(EventInstance evt) {
		
		if(!session.isOpen())
			this.terminate();
		
		//TODO Filter if we need to send it
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.RAISED, evt));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.UserEventListener#returnToNormal(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void returnToNormal(EventInstance evt) {
		if(!session.isOpen())
			this.terminate();
		
		//TODO Filter if we need to send it
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.RETURN_TO_NORMAL, evt));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.UserEventListener#deactivated(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void deactivated(EventInstance evt) {
		if(!session.isOpen())
			this.terminate();
		
		//TODO Filter if we need to send it
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.DEACTIVATED, evt));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}
}
