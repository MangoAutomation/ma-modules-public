/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.LowLimitRateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.LowLimitRateOfChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class LowLimitRateOfChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<LowLimitRateOfChangeDetectorVO, LowLimitRateOfChangeEventDetectorModel> {

    @Override
    public Class<? extends LowLimitRateOfChangeDetectorVO> fromClass() {
        return LowLimitRateOfChangeDetectorVO.class;
    }

    @Override
    public Class<? extends LowLimitRateOfChangeEventDetectorModel> toClass() {
        return LowLimitRateOfChangeEventDetectorModel.class;
    }

    @Override
    public LowLimitRateOfChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        LowLimitRateOfChangeDetectorVO detector = (LowLimitRateOfChangeDetectorVO)from;
        return loadDataPoint(detector, new LowLimitRateOfChangeEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return LowLimitRateOfChangeDetectorDefinition.TYPE_NAME;
    }
}
