/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

/**
 * @author Terry Packer
 *
 */
public class PointValueWebSocketPublisher extends MangoWebSocketPublisher implements DataPointListener{
	
	Log LOG = LogFactory.getLog(PointValueWebSocketPublisher.class);

	private WebSocketSession session;
	private Integer dataPointId;
	
	private boolean sendPointInitialized = false;
	private boolean sendPointUpdated = false;
	private boolean sendPointChanged = false;
	private boolean sendPointSet = false;
	private boolean sendPointBackdated = false;
	private boolean sendPointTerminated = false;
	
	public PointValueWebSocketPublisher(Integer dataPointId,  List<PointValueEventType> eventTypes,
			WebSocketSession session, ObjectMapper jacksonMapper){
		super(jacksonMapper);

		this.session = session;
		this.dataPointId = dataPointId;
		
		this.setEventTypes(eventTypes);
		
	}
	

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointInitialized()
	 */
	@Override
	public void pointInitialized() {
		
		try {
			if(!session.isOpen())
				this.terminate();
			
			
			if(sendPointInitialized)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.INITIALIZE, new PointValueTimeModel(null)));

		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
		
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointUpdated(com.serotonin.m2m2.rt.dataImage.PointValueTime)
	 */
	@Override
	public void pointUpdated(PointValueTime newValue) {
		try {
			if(!session.isOpen())
				this.terminate();
			
			if(sendPointUpdated)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.UPDATE, new PointValueTimeModel(newValue)));

		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointChanged(com.serotonin.m2m2.rt.dataImage.PointValueTime, com.serotonin.m2m2.rt.dataImage.PointValueTime)
	 */
	@Override
	public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
		try {
			if(!session.isOpen())
				this.terminate();
			
			if(sendPointChanged)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.CHANGE, new PointValueTimeModel(newValue)));

		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointSet(com.serotonin.m2m2.rt.dataImage.PointValueTime, com.serotonin.m2m2.rt.dataImage.PointValueTime)
	 */
	@Override
	public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
		try {
			if(!session.isOpen())
				this.terminate();
			
			if(sendPointSet)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.SET, new PointValueTimeModel(newValue)));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointBackdated(com.serotonin.m2m2.rt.dataImage.PointValueTime)
	 */
	@Override
	public void pointBackdated(PointValueTime value) {
		try {
			if(!session.isOpen())
				this.terminate();
			
			if(sendPointBackdated)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.BACKDATE, new PointValueTimeModel(value)));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointTerminated()
	 */
	@Override
	public void pointTerminated() {
		try {
			if(!session.isOpen())
				this.terminate();
			
			if(sendPointTerminated)
				this.sendMessage(session, new PointValueEventModel(PointValueEventType.TERMINATE, new PointValueTimeModel(null)));
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
	}


	/**
	 * Re-set the event types
	 * @param eventTypes
	 */
	public void setEventTypes(List<PointValueEventType> eventTypes){
		
		sendPointInitialized = false;
		sendPointUpdated = false;
		sendPointChanged = false;
		sendPointSet = false;
		sendPointBackdated = false;
		sendPointTerminated = false;
		
		for(PointValueEventType type : eventTypes){
			switch(type){
			case BACKDATE:
				sendPointBackdated = true;
				break;
			case CHANGE:
				sendPointChanged = true;
				break;
			case INITIALIZE:
				sendPointInitialized = true;
				break;
			case SET:
				sendPointSet = true;
				break;
			case TERMINATE:
				sendPointTerminated = true;
				break;
			case UPDATE:
				sendPointUpdated = true;
				break;
			}
		}
	}
	

	/**
	 * @param session
	 */
	public void initialize() {
		Common.runtimeManager.addDataPointListener(dataPointId, this);
	}

	public void terminate(){
		Common.runtimeManager.removeDataPointListener(dataPointId, this);
	}
	
	public WebSocketSession getSession(){
		return this.session;
	}


}
