/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.events;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventListener;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventEventTypeEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventRegistrationModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
public class EventEventHandler extends MangoWebSocketHandler {

    // TODO Mango 3.4 use log from super class
	private final Log log = LogFactory.getLog(this.getClass());

    private final Object publisherLock = new Object();
	private EventWebSocketPublisher publisher;
    private boolean connectionClosed = false;
    private WebSocketSession session;
	
	public EventEventHandler() {
		super();
	}
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	    super.afterConnectionEstablished(session);
	    this.session = session;
    }

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			User user = this.getUser(session);
			if (user == null) {
				return;
			}
			
			EventRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), EventRegistrationModel.class);
            Set<String> levels = model.getLevels();
            if (levels == null) {
                levels = Collections.emptySet();
            }
            Set<EventEventTypeEnum> events = model.getEventTypes();
            if (events == null) {
                events = EnumSet.noneOf(EventEventTypeEnum.class);
            }
			boolean emptySubscriptions = levels.isEmpty() || events.isEmpty();

			synchronized(this.publisherLock) {
			    if (!this.connectionClosed) {
			        if (this.publisher != null) {
			            if (emptySubscriptions) {
	                        this.publisher.terminate();
	                        this.publisher = null;
	                    } else {
	                        this.publisher.changeLevels(levels);
	                        this.publisher.changeEvents(events);
	                    }
                    } else if (!emptySubscriptions) {
                        this.publisher = new EventWebSocketPublisher(user, levels, events);
                        this.publisher.initialize();
                    }
			    }
			}
		} catch (Exception e) {
			try {
				this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
			} catch (Exception e1) {
				log.error(e.getMessage(), e);
			}
		}
		if(log.isDebugEnabled())
		    log.debug(message.getPayload());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        synchronized(this.publisherLock) {
            if (!this.connectionClosed) {
                if (this.publisher != null) {
                    this.publisher.terminate();
                    this.publisher = null;
                }
                this.connectionClosed = true;
            }
        }

		// Handle closing connection here
		if (log.isDebugEnabled()) {
		    log.debug("Websocket connection closed, status code: " + status.getCode() + ", reason: " + status.getReason());
		}
	}

	public class EventWebSocketPublisher implements UserEventListener {
	    private final User user;
	    private volatile Set<Integer> levels;
	    private volatile EnumSet<EventEventTypeEnum> events;

	    public EventWebSocketPublisher(User user, Set<String> levels, Set<EventEventTypeEnum> events) {
	        this.user = user;
            this.changeLevels(levels);
	        this.changeEvents(events);
	    }

	    public void initialize() {
	        Common.eventManager.addUserEventListener(this);
	    }
	    
	    public void terminate(){
	        Common.eventManager.removeUserEventListener(this);
	    }

	    public void changeLevels(Set<String> strLevels) {
	        Set<Integer> levels = new HashSet<>();
	        for (String level : strLevels){
	            levels.add(AlarmLevels.CODES.getId(level));
	        }
	        this.levels = levels;
	    }
	    
	    public void changeEvents(Set<EventEventTypeEnum> events) {
	        this.events = EnumSet.copyOf(events);
	    }

	    @Override
	    public int getUserId() {
	        return user.getId();
	    }

	    @Override
	    public void raised(EventInstance evt) {
	        if(!session.isOpen() || getUser(session) == null)
	            this.terminate();
	        
	        if(!this.events.contains(EventEventTypeEnum.RAISED))
	            return;
	        
	        if(!this.levels.contains(evt.getAlarmLevel()))
	            return;
	        
	        try{
	            sendMessage(session, new EventEventModel(EventEventTypeEnum.RAISED, evt));
	        } catch (Exception e) {
	            log.error(e.getMessage(),e);
	        }
	    }

	    @Override
	    public void returnToNormal(EventInstance evt) {
	        if(!session.isOpen() || getUser(session) == null)
	            this.terminate();
	        
	        if(!this.events.contains(EventEventTypeEnum.RETURN_TO_NORMAL))
	            return;
	        
	        if(!this.levels.contains(evt.getAlarmLevel()))
	            return;
	        
	        try{
	            sendMessage(session, new EventEventModel(EventEventTypeEnum.RETURN_TO_NORMAL, evt));
	        } catch (Exception e) {
	            log.error(e.getMessage(),e);
	        }
	    }

	    @Override
	    public void deactivated(EventInstance evt) {
	        if(!session.isOpen() || getUser(session) == null)
	            this.terminate();
	        
	        if(!this.events.contains(EventEventTypeEnum.DEACTIVATED))
	            return;
	        
	        if(!this.levels.contains(evt.getAlarmLevel()))
	            return;
	        
	        try{
	            sendMessage(session, new EventEventModel(EventEventTypeEnum.DEACTIVATED, evt));
	        } catch (Exception e) {
	            log.error(e.getMessage(),e);
	        }
	    }

	    @Override
	    public void acknowledged(EventInstance evt) {
	        if(!session.isOpen() || getUser(session) == null)
	            this.terminate();
	        
	        if(!this.events.contains(EventEventTypeEnum.ACKNOWLEDGED))
	            return;
	        
	        if(!this.levels.contains(evt.getAlarmLevel()))
	            return;
	        
	        try{
	            sendMessage(session, new EventEventModel(EventEventTypeEnum.ACKNOWLEDGED, evt));
	        } catch (Exception e) {
	            log.error(e.getMessage(),e);
	        }
	    }
	}
}
