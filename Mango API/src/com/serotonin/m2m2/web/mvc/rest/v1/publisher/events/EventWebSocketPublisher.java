/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.AlarmLevels;
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
	
	private final User user;
	private WebSocketSession session;
	private final List<Integer> levels;
	private boolean sendRaised;
	private boolean sendReturnToNormal;
	private boolean sendDeactivated;
	private boolean sendAcknowledged;
	
	/**
	 * @param session 
	 * @param levels 
	 * @param user 
	 * @param jacksonMapper
	 */
	public EventWebSocketPublisher(User user, List<String> levels,
			List<EventEventTypeEnum> events,
			WebSocketSession session, 
			ObjectMapper jacksonMapper) {
		super(jacksonMapper);
		this.session = session;
		this.user = user;
		this.levels = new ArrayList<Integer>();
		
		for(EventEventTypeEnum event : events){
			switch(event){
			case RAISED:
				this.sendRaised = true;
			break;
			case RETURN_TO_NORMAL:
				this.sendReturnToNormal = true;
			break;
			case DEACTIVATED:
				this.sendDeactivated = true;
			break;
			case ACKNOWLEDGED:
				this.sendAcknowledged = true;
			break;
			default:
				throw new ShouldNeverHappenException("Unknown EventEventType: " + event);
			}
		}
		
		//Fill the levels
		for(String level : levels){
			this.levels.add(AlarmLevels.CODES.getId(level));
		}
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
	
	public void changeLevels(List<String> levels){
		this.levels.clear();
		//Fill the levels
		for(String level : levels){
			this.levels.add(AlarmLevels.CODES.getId(level));
		}
	}
	
	public void changeEvents(List<EventEventTypeEnum> events){
		if(events.contains(EventEventTypeEnum.RAISED))
			this.sendRaised = true;
		else
			this.sendRaised = false;

		if(events.contains(EventEventTypeEnum.RETURN_TO_NORMAL))
			this.sendReturnToNormal = true;
		else
			this.sendReturnToNormal = false;

		if(events.contains(EventEventTypeEnum.DEACTIVATED))
			this.sendDeactivated = true;
		else
			this.sendDeactivated = false;

		if(events.contains(EventEventTypeEnum.ACKNOWLEDGED))
			this.sendAcknowledged = true;
		else
			this.sendAcknowledged = false;
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
		
		if(!sendRaised)
			return;
		
		if(!this.levels.contains(evt.getAlarmLevel()))
			return;
		
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.RAISED, evt));
		} catch (Exception e) {
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
		
		if(!sendReturnToNormal)
			return;
		
		if(!this.levels.contains(evt.getAlarmLevel()))
			return;
		
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.RETURN_TO_NORMAL, evt));
		} catch (Exception e) {
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
		
		if(!sendDeactivated)
			return;
		
		if(!this.levels.contains(evt.getAlarmLevel()))
			return;
		
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.DEACTIVATED, evt));
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.event.UserEventListener#acknowledged(com.serotonin.m2m2.rt.event.EventInstance)
	 */
	@Override
	public void acknowledged(EventInstance evt) {
		if(!session.isOpen())
			this.terminate();
		
		if(!sendAcknowledged)
			return;
		
		if(!this.levels.contains(evt.getAlarmLevel()))
			return;
		
		try{
			this.sendMessage(session, new EventEventModel(EventEventTypeEnum.ACKNOWLEDGED, evt));
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
}
