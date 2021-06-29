/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AlphanumericRegexStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AlphanumericRegexStateDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class AlphanumericRegexStateEventDetectorModel extends TimeoutDetectorModel<AlphanumericRegexStateDetectorVO>{

    private String state;
    
    public AlphanumericRegexStateEventDetectorModel(AlphanumericRegexStateDetectorVO data) {
        fromVO(data);
    }

    public AlphanumericRegexStateEventDetectorModel() { }
    
    @Override
    public void fromVO(AlphanumericRegexStateDetectorVO vo) {
        super.fromVO(vo);
        this.state = vo.getState();
    }
    
    @Override
    public AlphanumericRegexStateDetectorVO toVO() {
        AlphanumericRegexStateDetectorVO vo = super.toVO();
        vo.setState(state);
        return vo;
    }
    
    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    @Override
    public String getDetectorType() {
        return AlphanumericRegexStateEventDetectorDefinition.TYPE_NAME;
    }
	
}
