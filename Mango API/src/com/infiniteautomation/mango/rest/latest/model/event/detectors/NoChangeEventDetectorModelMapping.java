/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.NoChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NoChangeDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class NoChangeEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<NoChangeDetectorVO, NoChangeEventDetectorModel> {

    @Override
    public Class<? extends NoChangeDetectorVO> fromClass() {
        return NoChangeDetectorVO.class;
    }

    @Override
    public Class<? extends NoChangeEventDetectorModel> toClass() {
        return NoChangeEventDetectorModel.class;
    }

    @Override
    public NoChangeEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        NoChangeDetectorVO detector = (NoChangeDetectorVO)from;
        return loadDataPoint(detector, new NoChangeEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return NoChangeEventDetectorDefinition.TYPE_NAME;
    }
}
