/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.publisher.mock;

import io.swagger.annotations.ApiModel;

import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.serotonin.m2m2.vo.publish.mock.MockPublishedPointVO;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherVO;

/**
 *
 * @author Terry Packer
 */
@ApiModel(value=MockPublisherDefinition.TYPE_NAME, parent=AbstractPublisherModel.class)
public class MockPublisherModel extends AbstractPublisherModel<MockPublishedPointVO, MockPublisherVO> {

    public MockPublisherModel() {

    }

    public MockPublisherModel(MockPublisherVO vo) {
        fromVO(vo);
    }

    @Override
    public String getModelType() {
        return MockPublisherDefinition.TYPE_NAME;
    }

}
