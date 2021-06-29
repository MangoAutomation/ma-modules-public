/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.event.detectors.PointChangeDetectorRT;
import com.serotonin.m2m2.vo.event.detector.PointChangeDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class PointChangeEventDetectorRTModel extends AbstractPointEventDetectorRTModel<PointChangeDetectorVO> {

    private DataValue oldValue;
    private DataValue newValue;
    private TranslatableMessage annotation;

    public PointChangeEventDetectorRTModel(PointChangeDetectorRT rt) {
        super(rt);
        this.oldValue = rt.getOldValue();
        this.newValue = rt.getNewValue();
        this.annotation = rt.getAnnotation();
    }

    public DataValue getOldValue() {
        return oldValue;
    }

    public void setOldValue(DataValue oldValue) {
        this.oldValue = oldValue;
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
