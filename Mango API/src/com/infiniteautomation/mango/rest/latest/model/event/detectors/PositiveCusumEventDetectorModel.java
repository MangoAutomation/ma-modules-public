/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.PositiveCusumEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.PositiveCusumDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class PositiveCusumEventDetectorModel extends TimeoutDetectorModel<PositiveCusumDetectorVO> {

    private double limit;
    private double weight;

    public PositiveCusumEventDetectorModel(PositiveCusumDetectorVO data) {
        fromVO(data);
    }

    public PositiveCusumEventDetectorModel() {}

    @Override
    public void fromVO(PositiveCusumDetectorVO vo) {
        super.fromVO(vo);
        this.limit = vo.getLimit();
        this.weight = vo.getWeight();
    }

    @Override
    public PositiveCusumDetectorVO toVO() {
        PositiveCusumDetectorVO vo = super.toVO();
        vo.setLimit(limit);
        vo.setWeight(weight);
        return vo;
    }


    /**
     * @return the limit
     */
    public double getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(double limit) {
        this.limit = limit;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String getDetectorType() {
        return PositiveCusumEventDetectorDefinition.TYPE_NAME;
    }

}
