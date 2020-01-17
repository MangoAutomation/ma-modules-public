/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.publisher.mock;

import com.infiniteautomation.mango.rest.v2.model.publisher.AbstractPublishedPointModel;
import com.infiniteautomation.mango.rest.v2.model.publisher.AbstractPublisherModel;
import com.serotonin.m2m2.vo.publish.mock.MockPublishedPointVO;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherVO;

import io.swagger.annotations.ApiModel;

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

    @Override
    public AbstractPublishedPointModel<MockPublishedPointVO> modelPoint(
            MockPublishedPointVO point) {
        return new MockPublishedPointModel(point);
    }

}
