/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.spring.MangoRestSpringConfiguration;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketErrorType;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * Event Handler for 1 Web socket session to publish events for multiple data points
 * 
 * @author Terry Packer
 * 
 */
public class PointValueEventHandler extends MangoWebSocketHandler {
	
	private static final Log LOG = LogFactory.getLog(PointValueEventHandler.class);

	private final Map<Integer, PointValueWebSocketPublisher> map = new HashMap<Integer, PointValueWebSocketPublisher>();


	
	public PointValueEventHandler(){
		super(MangoRestSpringConfiguration.objectMapper);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) { }

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		
		try {
			User user = this.getUser(session);
			if(user == null){
				//Not Logged In so no go
				this.sendErrorMessage(session, MangoWebSocketErrorType.NOT_LOGGED_IN, new TranslatableMessage("rest.error.notLoggedIn"));
				
				return;
			}	
			PointValueRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), PointValueRegistrationModel.class);
			
			synchronized (map) {
				// Handle message.getPayload() here
				DataPointVO vo = DaoRegistry.dataPointDao.getByXid(model.getDataPointXid());
				if (vo != null) {
					
					//Check permissions
					if(!Permissions.hasDataPointReadPermission(user, vo)){
						this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, 
								new TranslatableMessage("rest.error.noReadPermissionForPoint", user.getUsername(), vo.getXid()));
						return;
					}
					int dataPointId = vo.getId();
					PointValueWebSocketPublisher pub = map.get(dataPointId);
					if (pub != null) {
					    List<PointValueEventType> events = model.getEventTypes();
					    if (events.isEmpty()) {
					        pub.terminate();
					        map.remove(dataPointId);
					    }
					    else {
	                        pub.setEventTypes(events);
					    }
					} else {
						pub = new PointValueWebSocketPublisher(vo, model.getEventTypes(), session, this.jacksonMapper);
						pub.initialize();
						map.put(vo.getId(), pub);
						//Immediately send the most recent Point Value and the status of the data point
						pub.sendPointStatus();
					}
				} else {
					this.sendErrorMessage(session,MangoWebSocketErrorType.SERVER_ERROR, 
							new TranslatableMessage("rest.error.pointNotFound", model.getDataPointXid()));
				}
			}
		
		} catch (Exception e) {
			try {
				this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
			} catch (JsonProcessingException e1) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e1) {
				LOG.error(e.getMessage(), e);
			}
		} 
		LOG.debug(message.getPayload());
	}



	@Override
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) {

		synchronized (map) {
			Iterator<Integer> it = map.keySet().iterator();
			while (it.hasNext()) {
				Integer id = it.next();
				PointValueWebSocketPublisher pub = map.get(id);
				pub.terminate();
				}
		}
		// Handle closing connection here
		LOG.debug("Sesssion closed");
	}

	@Override
	public void handleTransportError(WebSocketSession session,
			Throwable exception) {
		// Handle error during transport here
		LOG.debug("Transport Error.", exception);
	}
}
