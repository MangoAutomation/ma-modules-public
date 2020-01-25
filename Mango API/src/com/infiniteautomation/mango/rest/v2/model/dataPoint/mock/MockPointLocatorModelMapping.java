/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.mock;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.vo.dataPoint.MockPointLocatorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;


/**
 * Mock data point mapping used for testing
 * @author Terry Packer
 *
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MockPointLocatorModelMapping implements RestModelJacksonMapping<MockPointLocatorVO, MockPointLocatorModel> {

    @Override
    public Class<? extends MockPointLocatorVO> fromClass() {
        return MockPointLocatorVO.class;
    }

    @Override
    public Class<? extends MockPointLocatorModel> toClass() {
        return MockPointLocatorModel.class;
    }

    @Override
    public MockPointLocatorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MockPointLocatorModel((MockPointLocatorVO)from);
    }

    @Override
    public String getTypeName() {
        return MockPointLocatorModel.TYPE_NAME;
    }
}
