/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.NegativeCusumEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NegativeCusumDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class NegativeCusumEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<NegativeCusumDetectorVO, NegativeCusumEventDetectorModel> {

    @Override
    public Class<? extends NegativeCusumDetectorVO> fromClass() {
        return NegativeCusumDetectorVO.class;
    }

    @Override
    public Class<? extends NegativeCusumEventDetectorModel> toClass() {
        return NegativeCusumEventDetectorModel.class;
    }

    @Override
    public NegativeCusumEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        NegativeCusumDetectorVO detector = (NegativeCusumDetectorVO)from;
        return loadDataPoint(detector, new NegativeCusumEventDetectorModel(detector), user, mapper);
    }
    
    @Override
    public String getTypeName() {
        return NegativeCusumEventDetectorDefinition.TYPE_NAME;
    }
}
