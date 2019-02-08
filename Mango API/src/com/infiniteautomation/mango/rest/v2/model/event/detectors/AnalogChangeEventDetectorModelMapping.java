/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.AnalogChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AnalogChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<AnalogChangeDetectorVO, AnalogChangeEventDetectorModel> {

    @Override
    public Class<? extends AnalogChangeDetectorVO> fromClass() {
        return AnalogChangeDetectorVO.class;
    }

    @Override
    public Class<? extends AnalogChangeEventDetectorModel> toClass() {
        return AnalogChangeEventDetectorModel.class;
    }

    @Override
    public AnalogChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        AnalogChangeDetectorVO detector = (AnalogChangeDetectorVO)from;
        return loadDataPoint(detector, new AnalogChangeEventDetectorModel(detector), user, mapper);
    }
    
    @Override
    public String getTypeName() {
        return AnalogChangeEventDetectorDefinition.TYPE_NAME;
    }
}
