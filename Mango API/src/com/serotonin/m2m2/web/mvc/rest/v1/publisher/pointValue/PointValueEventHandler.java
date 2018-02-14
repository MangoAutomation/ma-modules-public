/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.serotonin.m2m2.db.dao.DataPointDao;
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

	// TODO Mango 3.4 use concurrent hash map and remove synchronization
	private final Map<Integer, PointValueWebSocketPublisher> map = new HashMap<Integer, PointValueWebSocketPublisher>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

	public PointValueEventHandler(){
		super(MangoRestSpringConfiguration.getObjectMapper());
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception{
		super.afterConnectionEstablished(session);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		
		try {
			User user = this.getUser(session);
            if (user == null) {
                return;
            }
			PointValueRegistrationModel model = this.jacksonMapper.readValue(message.getPayload(), PointValueRegistrationModel.class);
			
			synchronized (map) {
				// Handle message.getPayload() here
				DataPointVO vo = DataPointDao.instance.getByXid(model.getDataPointXid());
				if (vo != null) {
					
					//Check permissions
					if(!Permissions.hasDataPointReadPermission(user, vo)){
						this.sendErrorMessage(session, MangoWebSocketErrorType.PERMISSION_DENIED, 
								new TranslatableMessage("permission.exception.readDataPoint", user.getUsername()));
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
						pub = new PointValueWebSocketPublisher(session.getUri(), vo, model.getEventTypes(), session, this.jacksonMapper);
						
						// beanFactory will only exist in Mango v3.3.1 and greater
						if (beanFactory != null) {
    		                beanFactory.autowireBean(pub);
    		                pub = (PointValueWebSocketPublisher) beanFactory.initializeBean(pub, PointValueWebSocketPublisher.class.getName());
						}
						
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
            // TODO Mango 3.4 add new exception type for closed session and don't try and send error if it was a closed session exception
			try {
				this.sendErrorMessage(session, MangoWebSocketErrorType.SERVER_ERROR, new TranslatableMessage("rest.error.serverError", e.getMessage()));
			} catch (Exception e1) {
				LOG.error(e.getMessage(), e);
			}
		} 
		if(LOG.isDebugEnabled())
			LOG.debug(message.getPayload());
	}



	@Override
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) {

		lock.writeLock().lock();
		try{
			Iterator<Integer> it = map.keySet().iterator();
			while (it.hasNext()) {
				Integer id = it.next();
				PointValueWebSocketPublisher pub = map.get(id);
				pub.terminate();

                // beanFactory will only exist in Mango v3.3.1 and greater
                if (beanFactory != null) {
                    beanFactory.destroyBean(pub);
                }
			}
			map.clear();
		}finally{
			lock.writeLock().unlock();
		}
		// Handle closing connection here
		if(LOG.isDebugEnabled())
			LOG.debug("Sesssion closed: " + status.getReason());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.socket.handler.AbstractWebSocketHandler#handleTransportError(org.springframework.web.socket.WebSocketSession, java.lang.Throwable)
	 */
	@Override
	public void handleTransportError(WebSocketSession session,
			Throwable e) throws Exception{
		// Handle error during transport here
		LOG.error("Websocket Transport Error:", e);
		session.close(CloseStatus.SERVER_ERROR);
	}
	
}
