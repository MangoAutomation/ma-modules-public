/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.event.detectors.UpdateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.UpdateDetectorVO;

/**
 * @author Jared Wiltshire
 */
public class UpdateEventDetectorRTModel extends AbstractPointEventDetectorRTModel<UpdateDetectorVO> {

    private DataValue newValue;
    private TranslatableMessage annotation;

    public UpdateEventDetectorRTModel(UpdateDetectorRT rt) {
        super(rt);
        this.newValue = rt.getNewValue();
        this.annotation = rt.getAnnotation();
    }

    public DataValue getNewValue() {
        return newValue;
    }

    public void setNewValue(DataValue newValue) {
        this.newValue = newValue;
    }

    public TranslatableMessage getAnnotation() {
        return annotation;
    }

    public void setAnnotation(TranslatableMessage annotation) {
        this.annotation = annotation;
    }

}
