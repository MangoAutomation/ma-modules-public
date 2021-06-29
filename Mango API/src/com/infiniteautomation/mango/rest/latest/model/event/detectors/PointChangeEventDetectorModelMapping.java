/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.PointChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.PointChangeDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class PointChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<PointChangeDetectorVO, PointChangeEventDetectorModel> {

    @Override
    public Class<? extends PointChangeDetectorVO> fromClass() {
        return PointChangeDetectorVO.class;
    }

    @Override
    public Class<? extends PointChangeEventDetectorModel> toClass() {
        return PointChangeEventDetectorModel.class;
    }

    @Override
    public PointChangeEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        PointChangeDetectorVO detector = (PointChangeDetectorVO)from;
        return loadDataPoint(detector, new PointChangeEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return PointChangeEventDetectorDefinition.TYPE_NAME;
    }
}
