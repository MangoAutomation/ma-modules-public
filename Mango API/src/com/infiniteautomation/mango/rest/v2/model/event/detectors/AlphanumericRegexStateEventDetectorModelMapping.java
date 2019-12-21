/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.detectors.AlphanumericRegexStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AlphanumericRegexStateDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AlphanumericRegexStateEventDetectorModelMapping extends  AbstractPointEventDetectorModelMapping<AlphanumericRegexStateDetectorVO, AlphanumericRegexStateEventDetectorModel> {

    @Override
    public Class<? extends AlphanumericRegexStateDetectorVO> fromClass() {
        return AlphanumericRegexStateDetectorVO.class;
    }

    @Override
    public Class<? extends AlphanumericRegexStateEventDetectorModel> toClass() {
        return AlphanumericRegexStateEventDetectorModel.class;
    }

    @Override
    public AlphanumericRegexStateEventDetectorModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        AlphanumericRegexStateDetectorVO detector = (AlphanumericRegexStateDetectorVO)from;
        return loadDataPoint(detector, new AlphanumericRegexStateEventDetectorModel(detector), user, mapper);
    }

    @Override
    public String getTypeName() {
        return AlphanumericRegexStateEventDetectorDefinition.TYPE_NAME;
    }

}
