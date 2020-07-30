/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.AnalogRangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogRangeDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogRangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<AnalogRangeDetectorVO, AnalogRangeEventDetectorModel> {

    @Override
    public Class<? extends AnalogRangeDetectorVO> fromClass() {
        return AnalogRangeDetectorVO.class;
    }

    @Override
    public Class<? extends AnalogRangeEventDetectorModel> toClass() {
        return AnalogRangeEventDetectorModel.class;
    }

    @Override
    public AnalogRangeEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        AnalogRangeDetectorVO detector = (AnalogRangeDetectorVO)from;
        return loadDataPoint(detector, new AnalogRangeEventDetectorModel(detector), user, mapper);
    }
    
    @Override
    public String getTypeName() {
        return AnalogRangeEventDetectorDefinition.TYPE_NAME;
    }
}
