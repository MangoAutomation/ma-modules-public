/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.publisher.mock;

import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModel;
import com.serotonin.m2m2.vo.publish.mock.MockPublishedPointVO;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;

/**
 *
 * @author Terry Packer
 */
public class MockPublishedPointModel extends AbstractPublishedPointModel<MockPublishedPointVO> {

    public MockPublishedPointModel() {

    }

    public MockPublishedPointModel(MockPublishedPointVO vo) {
        fromVO(vo);
    }

    @Override
    public String getModelType() {
        return MockPublisherDefinition.TYPE_NAME;
    }

    @Override
    public MockPublishedPointVO newVO() {
        return new MockPublishedPointVO();
    }

}
