/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.vo;

import com.serotonin.m2m2.reports.ReportModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel;

/**
 * @author Terry Packer
 *
 */
public class ReportModel extends AbstractVoModel<ReportVO>{

	/**
	 * @param data
	 */
	public ReportModel(ReportVO data) {
		super(data);
	}
	
	public ReportModel(){
		super(new ReportVO());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractBasicVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return ReportModelDefinition.TYPE_NAME;
	}

}
