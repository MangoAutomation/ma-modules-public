/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.AnalogLowLimitEventDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AnalogLowLimitDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogLowLimitEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<AnalogLowLimitDetectorVO, AnalogLowLimitEventDetectorModel> {

    @Override
    public Class<? extends AnalogLowLimitDetectorVO> fromClass() {
        return AnalogLowLimitDetectorVO.class;
    }

    @Override
    public Class<? extends AnalogLowLimitEventDetectorModel> toClass() {
        return AnalogLowLimitEventDetectorModel.class;
    }

    @Override
    public AnalogLowLimitEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        AnalogLowLimitDetectorVO detector = (AnalogLowLimitDetectorVO)from;
        return loadDataPoint(detector, new AnalogLowLimitEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return AnalogLowLimitEventDetectorDefinition.TYPE_NAME;
    }
}
