/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.eventDetector;

import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.web.mvc.rest.IMangoVoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredVoStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.detectors.AbstractEventDetectorModel;

/**
 * 
 * @author Terry Packer
 */
public class EventDetectorStreamCallback extends FilteredVoStreamCallback<AbstractEventDetectorVO<?>, AbstractEventDetectorModel<?>, EventDetectorDao<AbstractEventDetectorVO<?>>>{

	private final DataPointFilter filter;
	
	/**
	 * @param controller
	 */
	public EventDetectorStreamCallback(
			IMangoVoRestController<AbstractEventDetectorVO<?>, AbstractEventDetectorModel<?>, EventDetectorDao<AbstractEventDetectorVO<?>>> controller, User user) {
		super(controller);
		this.filter = new DataPointFilter(user);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredQueryStreamCallback#filter(java.lang.Object)
	 */
	@Override
	protected boolean filter(AbstractEventDetectorVO<?> vo) {
		switch(vo.getDetectorSourceType()){
		case EventType.EventTypeNames.DATA_POINT:
		    //The row mapper will not have set the data point vo to the point event detectors
			return !this.filter.hasDataPointReadPermission(((AbstractPointEventDetectorVO<?>)vo).getDataPoint());
		default:
			return true;
		}
	}

}
