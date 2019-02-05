/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource;

import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;

/**
 * Useful for testing
 * 
 * @author Terry Packer
 *
 */
public class MockDataSourceModel extends AbstractDataSourceModel<MockDataSourceVO>{

	/**
	 * @param data
	 */
	public MockDataSourceModel(MockDataSourceVO data) {
		super(data);
	}

	public MockDataSourceModel(){
		super(new MockDataSourceVO());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return "DS.MOCK";
	}

}
