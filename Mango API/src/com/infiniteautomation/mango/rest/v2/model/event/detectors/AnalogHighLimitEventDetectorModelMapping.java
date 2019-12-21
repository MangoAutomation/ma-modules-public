/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.AnalogHighLimitEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogHighLimitDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AnalogHighLimitEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<AnalogHighLimitDetectorVO, AnalogHighLimitEventDetectorModel> {

    @Override
    public Class<? extends AnalogHighLimitDetectorVO> fromClass() {
        return AnalogHighLimitDetectorVO.class;
    }

    @Override
    public Class<? extends AnalogHighLimitEventDetectorModel> toClass() {
        return AnalogHighLimitEventDetectorModel.class;
    }

    @Override
    public AnalogHighLimitEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        AnalogHighLimitDetectorVO detector = (AnalogHighLimitDetectorVO)from;
        return loadDataPoint(detector, new AnalogHighLimitEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return AnalogHighLimitEventDetectorDefinition.TYPE_NAME;
    }
}
