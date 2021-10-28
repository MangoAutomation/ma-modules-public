/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.publisher.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublishedPointModelMapping;
import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.vo.publish.mock.MockPublishedPointVO;
import com.serotonin.m2m2.vo.publish.mock.MockPublisherDefinition;

/**
 *
 * @author Terry Packer
 */
@ConditionalOnProperty(value = {"${testing.enabled:false}", "${testing.restApi.enabled:false}"})
@Component
public class MockPublishedPointModelMapping extends AbstractPublishedPointModelMapping<MockPublishedPointVO, MockPublishedPointModel> {

    @Autowired
    public MockPublishedPointModelMapping(DataPointDao dataPointDao, PublisherDao publisherDao, MockPublisherDefinition definition) {
        super(dataPointDao, publisherDao, definition);
    }

    @Override
    public Class<? extends MockPublishedPointVO> fromClass() {
        return MockPublishedPointVO.class;
    }

    @Override
    public Class<? extends MockPublishedPointModel> toClass() {
        return MockPublishedPointModel.class;
    }

    @Override
    public String getTypeName() {
        return MockPublisherDefinition.TYPE_NAME;
    }

}
