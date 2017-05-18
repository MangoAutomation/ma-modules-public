/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports.handler;

import com.serotonin.m2m2.web.mvc.rest.v1.model.events.handlers.AbstractEventHandlerModel;

/**
 * 
 * @author Terry Packer
 */
public class ReportEventHandlerModel extends AbstractEventHandlerModel<ReportEventHandlerVO>{

	public ReportEventHandlerModel(){
		super(new ReportEventHandlerVO());
	}
	
	public ReportEventHandlerModel(ReportEventHandlerVO data) {
		super(data);
	}
	
	public int getActiveReportId() {
		return this.data.getActiveReportId();
	}

	public void setActiveReportId(int activeReportId) {
		this.data.setActiveReportId(activeReportId);
	}

	public int getInactiveReportId() {
		return this.data.getInactiveReportId();
	}

	public void setInactiveReportId(int inactiveReportId) {
		this.data.setInactiveReportId(inactiveReportId);
	}
	
}
