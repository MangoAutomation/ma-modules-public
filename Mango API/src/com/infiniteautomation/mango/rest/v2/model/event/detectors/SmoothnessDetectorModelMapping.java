/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.SmoothnessDetectorVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class SmoothnessDetectorModelMapping extends AbstractPointEventDetectorModelMapping<SmoothnessDetectorVO, SmoothnessDetectorModel> {

    @Override
    public Class<? extends SmoothnessDetectorVO> fromClass() {
        return SmoothnessDetectorVO.class;
    }

    @Override
    public Class<? extends SmoothnessDetectorModel> toClass() {
        return SmoothnessDetectorModel.class;
    }

    @Override
    public SmoothnessDetectorModel map(Object from, User user, RestModelMapper mapper) {
        SmoothnessDetectorVO detector = (SmoothnessDetectorVO)from;
        return loadDataPoint(detector, new SmoothnessDetectorModel(detector), user, mapper);
    }

}
