/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=InternalPointLocatorModel.TYPE_NAME)
public class InternalPointLocatorModel extends PointLocatorModel<InternalPointLocatorVO> {
    public static final String TYPE_NAME = "PL.INTERNAL";

	public InternalPointLocatorModel(InternalPointLocatorVO data) {
		super(data);
	}
	
	public InternalPointLocatorModel() {
		super(new InternalPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
	@JsonGetter("monitorId")
	@CSVColumnGetter(order=18, header="monitorId")
	public String getMonitorId() {
	    return data.getMonitorId();
	}

	@JsonSetter("monitorId")
	@CSVColumnSetter(order=18, header="monitor")
	public void setMonitorId(String monitorId) {
	    this.data.setMonitorId(monitorId);
	}
	
	@JsonGetter
	public TranslatableMessage getDescription() {
        ValueMonitor<?> monitor = Common.MONITORED_VALUES.getValueMonitor(data.getMonitorId());
        if (monitor != null)
            return monitor.getName();
        else
            return null;
	}

}
