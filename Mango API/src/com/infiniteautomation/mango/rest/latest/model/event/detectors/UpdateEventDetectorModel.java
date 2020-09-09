/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.UpdateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.UpdateDetectorVO;

/**
 * @author Jared Wiltshire
 */
public class UpdateEventDetectorModel extends AbstractPointEventDetectorModel<UpdateDetectorVO> {

    public UpdateEventDetectorModel(UpdateDetectorVO data) {
        fromVO(data);
    }

    public UpdateEventDetectorModel() {
    }

    @Override
    public String getDetectorType() {
        return UpdateEventDetectorDefinition.TYPE_NAME;
    }

}
