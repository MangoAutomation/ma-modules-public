/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.DataPointListener;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * This class should contain some form of synchronization that will allow for the 
 * Listener events to return without doing any work if the socket is trying to send
 * a current message.  This way we won't clog up the medium priority level with hung up tasks.
 * 
 * 
 * @author Terry Packer
 *
 */
public class PointValueWebSocketPublisher extends MangoWebSocketPublisher implements DataPointListener{
	
	private static final Log LOG = LogFactory.getLog(PointValueWebSocketPublisher.class);

	private WebSocketSession session;
	private DataPointVO vo;
	private DataPointRT rt;
	
	private boolean sendPointInitialized = false;
	private boolean sendPointUpdated = false;
	private boolean sendPointChanged = false;
	private boolean sendPointSet = false;
	private boolean sendPointBackdated = false;
	private boolean sendPointTerminated = false;
	
	public PointValueWebSocketPublisher(DataPointVO vo,  List<PointValueEventType> eventTypes,
			WebSocketSession session, ObjectMapper jacksonMapper){
		super(jacksonMapper);

		this.session = session;
		this.vo = vo;
		
		this.setEventTypes(eventTypes);
	}
	
	/**
	 * Initial response upon new registration
	 */
	public void sendPointStatus(){
		try {
			if(!session.isOpen())
				this.terminate();
				
				rt = Common.runtimeManager.getDataPoint(vo.getId());
				boolean enabled = false;
				Map<String,Object> attributes = null;
				PointValueTime pvt = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					pvt = rt.getPointValue(); //Get the value
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, pvt);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(pvt != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(pvt != null)
					pvtModel = new PointValueTimeModel(pvt);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.REGISTERED, pvtModel, renderedValue, convertedValue));

		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#pointInitialized()
	 */
	@Override
	public void pointInitialized() {
		
		try {
			if(!session.isOpen())
				this.terminate();
			
			rt = Common.runtimeManager.getDataPoint(vo.getId()); //Set us up
			
			if(sendPointInitialized){
				boolean enabled = false;
				Map<String,Object> attributes = null;
				PointValueTime pvt = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					pvt = rt.getPointValue(); //Get the value
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, pvt);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(pvt != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(pvt != null)
					pvtModel = new PointValueTimeModel(pvt);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.INITIALIZE, pvtModel, renderedValue, convertedValue));
			}
		} catch (Exception e) {
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
			
			if(sendPointUpdated){
				boolean enabled = false;
				Map<String,Object> attributes = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, newValue);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(newValue != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(newValue.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(newValue != null)
					pvtModel = new PointValueTimeModel(newValue);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.UPDATE, pvtModel, renderedValue, convertedValue));
			}
		} catch (Exception e) {
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
			
			if(sendPointChanged){
				boolean enabled = false;
				Map<String,Object> attributes = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, newValue);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(newValue != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(newValue.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(newValue != null)
					pvtModel = new PointValueTimeModel(newValue);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.CHANGE, pvtModel, renderedValue, convertedValue));
			}
		} catch (Exception e) {
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
			
			if(sendPointSet){
				boolean enabled = false;
				Map<String,Object> attributes = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, newValue);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(newValue != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(newValue.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(newValue != null)
					pvtModel = new PointValueTimeModel(newValue);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.SET, pvtModel, renderedValue, convertedValue));
			}
		} catch (Exception e) {
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
			
			if(sendPointBackdated){
				boolean enabled = false;
				Map<String,Object> attributes = null;
				Double convertedValue = null;
				String renderedValue = null;
				if(rt != null){
					enabled = true; //We are enabled
					attributes = rt.getAttributes();
					renderedValue = Functions.getRenderedText(vo, value);
					if((vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)&&(value != null))
						convertedValue = vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getValue().getDoubleValue());
				}
				PointValueTimeModel pvtModel = null;
				if(value != null)
					pvtModel = new PointValueTimeModel(value);
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), enabled, attributes, PointValueEventType.BACKDATE, pvtModel, renderedValue, convertedValue));
			}
		} catch (Exception e) {
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
			
			this.rt = null;
			
			if(sendPointTerminated){
				this.sendMessage(session, new PointValueEventModel(vo.getXid(), false, null, PointValueEventType.TERMINATE, null, null, null));
			}
		} catch (Exception e) {
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
			case REGISTERED:
				//Always send this for now
				break;
			}
		}
	}
	

	/**
	 * @param session
	 */
	public void initialize() {
		Common.runtimeManager.addDataPointListener(vo.getId(), this);
	}

	public void terminate(){
		Common.runtimeManager.removeDataPointListener(vo.getId(), this);
	}
	
	public WebSocketSession getSession(){
		return this.session;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.dataImage.DataPointListener#getListenerName()
	 */
	@Override
	public String getListenerName() {
		return "Web Socket Publisher: " + vo.getXid();
	}


}
