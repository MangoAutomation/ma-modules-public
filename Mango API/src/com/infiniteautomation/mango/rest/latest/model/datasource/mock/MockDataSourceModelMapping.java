/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.datasource.mock;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MockDataSourceModelMapping implements RestModelJacksonMapping<MockDataSourceVO, MockDataSourceModel> {

    @Override
    public Class<? extends MockDataSourceVO> fromClass() {
        return MockDataSourceVO.class;
    }

    @Override
    public Class<? extends MockDataSourceModel> toClass() {
        return MockDataSourceModel.class;
    }

    @Override
    public MockDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MockDataSourceModel((MockDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return MockDataSourceDefinition.TYPE_NAME;
    }

}
