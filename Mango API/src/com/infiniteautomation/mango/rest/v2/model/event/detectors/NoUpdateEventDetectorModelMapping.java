/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.NoUpdateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NoUpdateDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class NoUpdateEventDetectorModelMapping extends AbstractPointEventDetectorModelMapping<NoUpdateDetectorVO, NoUpdateEventDetectorModel> {

    @Override
    public Class<? extends NoUpdateDetectorVO> fromClass() {
        return NoUpdateDetectorVO.class;
    }

    @Override
    public Class<? extends NoUpdateEventDetectorModel> toClass() {
        return NoUpdateEventDetectorModel.class;
    }

    @Override
    public NoUpdateEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        NoUpdateDetectorVO detector = (NoUpdateDetectorVO)from;
        return loadDataPoint(detector, new NoUpdateEventDetectorModel(detector), user, mapper);
    }
    @Override
    public String getTypeName() {
        return NoUpdateEventDetectorDefinition.TYPE_NAME;
    }
}
