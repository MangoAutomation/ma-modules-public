/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.HighLimitRateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.HighLimitRateOfChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class HighLimitRateOfChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<HighLimitRateOfChangeDetectorVO, HighLimitRateOfChangeEventDetectorModel> {

    @Override
    public Class<? extends HighLimitRateOfChangeDetectorVO> fromClass() {
        return HighLimitRateOfChangeDetectorVO.class;
    }

    @Override
    public Class<? extends HighLimitRateOfChangeEventDetectorModel> toClass() {
        return HighLimitRateOfChangeEventDetectorModel.class;
    }

    @Override
    public HighLimitRateOfChangeEventDetectorModel map(Object from, User user, RestModelMapper mapper) {
        HighLimitRateOfChangeDetectorVO detector = (HighLimitRateOfChangeDetectorVO)from;
        return loadDataPoint(detector, new HighLimitRateOfChangeEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return HighLimitRateOfChangeDetectorDefinition.TYPE_NAME;
    }
}
