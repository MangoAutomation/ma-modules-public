/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.publisher.mock;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.mock.MockPublishedPointVO;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;

/**
 *
 * @author Terry Packer
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MockPublishedPointModelMapping implements RestModelJacksonMapping<MockPublishedPointVO, MockPublishedPointModel> {

    @Override
    public Class<? extends MockPublishedPointVO> fromClass() {
        return MockPublishedPointVO.class;
    }

    @Override
    public Class<? extends MockPublishedPointModel> toClass() {
        return MockPublishedPointModel.class;
    }

    @Override
    public MockPublishedPointModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MockPublishedPointModel((MockPublishedPointVO)from);
    }

    @Override
    public String getTypeName() {
        return MockPublisherDefinition.TYPE_NAME;
    }

}
