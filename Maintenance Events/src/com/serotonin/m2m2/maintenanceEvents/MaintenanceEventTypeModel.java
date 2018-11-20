/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

/**
 * 
 * @author Terry Packer
 */
public class MaintenanceEventTypeModel extends EventTypeModel{

	private int maintenanceId;
	
	public MaintenanceEventTypeModel(){ }
	
	public MaintenanceEventTypeModel(MaintenanceEventType type){
		this.maintenanceId = type.getReferenceId1();
	}
	
	public int getMaintenanceId() {
		return maintenanceId;
	}

	public void setMaintenanceId(int maintenanceId) {
		this.maintenanceId = maintenanceId;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return MaintenanceEventType.TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#isRateLimited()
	 */
	@Override
	public boolean isRateLimited() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#getDuplicateHandling()
	 */
	@Override
	public String getDuplicateHandling() {
		return EventType.DUPLICATE_HANDLING_CODES.getCode(EventType.DuplicateHandling.IGNORE);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel#getEventTypeInstance()
	 */
	@Override
	public EventType toEventType() {
		return new MaintenanceEventType(this.maintenanceId);
	}

}
