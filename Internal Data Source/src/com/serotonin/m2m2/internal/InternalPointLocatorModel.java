package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=InternalPointLocatorModelDefinition.TYPE_NAME)
public class InternalPointLocatorModel extends PointLocatorModel<InternalPointLocatorVO> {

	public InternalPointLocatorModel(InternalPointLocatorVO data) {
		super(data);
	}
	
	public InternalPointLocatorModel() {
		super(new InternalPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return InternalPointLocatorModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("monitorId")
	@CSVColumnGetter(order=18, header="monitorId")
	public String getMonitorId() {
	    return data.getMonitorId();
	}

	@JsonSetter("monitor")
	@CSVColumnSetter(order=18, header="monitor")
	public void setMonitorId(String monitorId) {
	    this.data.setMonitorId(monitorId);
	}

}
