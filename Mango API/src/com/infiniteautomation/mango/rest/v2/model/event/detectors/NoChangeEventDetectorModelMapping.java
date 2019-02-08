/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.NoChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.NoChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class NoChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<NoChangeDetectorVO, NoChangeEventDetectorModel> {

    @Override
    public Class<? extends NoChangeDetectorVO> fromClass() {
        return NoChangeDetectorVO.class;
    }

    @Override
    public Class<? extends NoChangeEventDetectorModel> toClass() {
        return NoChangeEventDetectorModel.class;
    }

    @Override
    public NoChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        NoChangeDetectorVO detector = (NoChangeDetectorVO)from;
        return loadDataPoint(detector, new NoChangeEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return NoChangeEventDetectorDefinition.TYPE_NAME;
    }
}
