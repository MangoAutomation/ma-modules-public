/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.RateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class RateOfChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<RateOfChangeDetectorVO, RateOfChangeEventDetectorModel> {

    @Override
    public Class<? extends RateOfChangeDetectorVO> fromClass() {
        return RateOfChangeDetectorVO.class;
    }

    @Override
    public Class<? extends RateOfChangeEventDetectorModel> toClass() {
        return RateOfChangeEventDetectorModel.class;
    }

    @Override
    public RateOfChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        RateOfChangeDetectorVO detector = (RateOfChangeDetectorVO)from;
        return loadDataPoint(detector, new RateOfChangeEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return RateOfChangeDetectorDefinition.TYPE_NAME;
    }
}
