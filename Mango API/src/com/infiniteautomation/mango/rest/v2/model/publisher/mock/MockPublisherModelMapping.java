/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.publisher.mock;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherVO;

/**
 *
 * @author Terry Packer
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MockPublisherModelMapping implements RestModelJacksonMapping<MockPublisherVO, MockPublisherModel> {

    @Override
    public Class<? extends MockPublisherVO> fromClass() {
        return MockPublisherVO.class;
    }

    @Override
    public Class<? extends MockPublisherModel> toClass() {
        return MockPublisherModel.class;
    }

    @Override
    public MockPublisherModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MockPublisherModel((MockPublisherVO)from);
    }

    @Override
    public String getTypeName() {
        return MockPublisherDefinition.TYPE_NAME;
    }

}
