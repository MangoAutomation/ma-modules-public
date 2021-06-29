/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AnalogLowLimitEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogLowLimitDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class AnalogLowLimitEventDetectorModel extends TimeoutDetectorModel<AnalogLowLimitDetectorVO>{

    private double limit;
    private double resetLimit;
    private boolean useResetLimit;
    private boolean notLower;
    
    public AnalogLowLimitEventDetectorModel(AnalogLowLimitDetectorVO data) {
        fromVO(data);
    }
    
    public AnalogLowLimitEventDetectorModel() {
        
    }
    
    @Override
    public void fromVO(AnalogLowLimitDetectorVO vo) {
        super.fromVO(vo);
        this.limit = vo.getLimit();
        this.resetLimit = vo.getResetLimit();
        this.useResetLimit = vo.isUseResetLimit();
        this.notLower = vo.isNotLower();
    }
    
    @Override
    public AnalogLowLimitDetectorVO toVO() {
        AnalogLowLimitDetectorVO vo = super.toVO();
        vo.setLimit(limit);
        vo.setResetLimit(resetLimit);
        vo.setUseResetLimit(useResetLimit);
        vo.setNotLower(notLower);
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
     * @return the resetLimit
     */
    public double getResetLimit() {
        return resetLimit;
    }

    /**
     * @param resetLimit the resetLimit to set
     */
    public void setResetLimit(double resetLimit) {
        this.resetLimit = resetLimit;
    }

    /**
     * @return the useResetLimit
     */
    public boolean isUseResetLimit() {
        return useResetLimit;
    }

    /**
     * @param useResetLimit the useResetLimit to set
     */
    public void setUseResetLimit(boolean useResetLimit) {
        this.useResetLimit = useResetLimit;
    }

    /**
     * @return the notLower
     */
    public boolean isNotLower() {
        return notLower;
    }

    /**
     * @param notLower the notLower to set
     */
    public void setNotLower(boolean notLower) {
        this.notLower = notLower;
    }

    @Override
    public String getDetectorType() {
        return AnalogLowLimitEventDetectorDefinition.TYPE_NAME;
    }
}