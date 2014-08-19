/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class EnvCanDataSourceModel extends AbstractDataSourceModel<EnvCanDataSourceVO>{

	public EnvCanDataSourceModel() {
		super(new EnvCanDataSourceVO());
	}

	
	/**
	 * @param data
	 */
	public EnvCanDataSourceModel(DataSourceVO<EnvCanDataSourceVO> data) {
		super(data);
	}

	
	
}
