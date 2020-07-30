/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.datasource.mock;

import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractPollingDataSourceModel;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;

/**
 *
 * @author Terry Packer
 */
public class MockDataSourceModel extends AbstractPollingDataSourceModel<MockDataSourceVO> {

    public MockDataSourceModel() {

    }

    public MockDataSourceModel(MockDataSourceVO vo) {
        fromVO(vo);
    }

    @Override
    public String getModelType() {
        return MockDataSourceDefinition.TYPE_NAME;
    }

}
