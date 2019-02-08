/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.PointChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.PointChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class PointChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<PointChangeDetectorVO, PointChangeEventDetectorModel> {

    @Override
    public Class<? extends PointChangeDetectorVO> fromClass() {
        return PointChangeDetectorVO.class;
    }

    @Override
    public Class<? extends PointChangeEventDetectorModel> toClass() {
        return PointChangeEventDetectorModel.class;
    }

    @Override
    public PointChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        PointChangeDetectorVO detector = (PointChangeDetectorVO)from;
        return loadDataPoint(detector, new PointChangeEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return PointChangeEventDetectorDefinition.TYPE_NAME;
    }
}
