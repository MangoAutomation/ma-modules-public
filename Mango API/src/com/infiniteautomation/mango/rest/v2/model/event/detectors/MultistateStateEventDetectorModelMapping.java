/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.MultistateStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.MultistateStateDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MultistateStateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<MultistateStateDetectorVO, MultistateStateEventDetectorModel> {

    @Override
    public Class<? extends MultistateStateDetectorVO> fromClass() {
        return MultistateStateDetectorVO.class;
    }

    @Override
    public Class<? extends MultistateStateEventDetectorModel> toClass() {
        return MultistateStateEventDetectorModel.class;
    }

    @Override
    public MultistateStateEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        MultistateStateDetectorVO detector = (MultistateStateDetectorVO)from;
        return loadDataPoint(detector, new MultistateStateEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return MultistateStateEventDetectorDefinition.TYPE_NAME;
    }
}
