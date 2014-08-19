/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class VMStatDataSourceModel extends AbstractDataSourceModel<VMStatDataSourceVO>{

	/**
	 * @param data
	 */
	public VMStatDataSourceModel(DataSourceVO<VMStatDataSourceVO> data) {
		super(data);
	}
	
	public VMStatDataSourceModel() {
		super(new VMStatDataSourceVO());
	}

}
