/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.BinaryStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.BinaryStateDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class BinaryStateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<BinaryStateDetectorVO, BinaryStateEventDetectorModel> {

    @Override
    public Class<? extends BinaryStateDetectorVO> fromClass() {
        return BinaryStateDetectorVO.class;
    }

    @Override
    public Class<? extends BinaryStateEventDetectorModel> toClass() {
        return BinaryStateEventDetectorModel.class;
    }

    @Override
    public BinaryStateEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        BinaryStateDetectorVO detector = (BinaryStateDetectorVO)from;
        return loadDataPoint(detector, new BinaryStateEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return BinaryStateEventDetectorDefinition.TYPE_NAME;
    }
}
