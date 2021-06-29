/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.BinaryStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.BinaryStateDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class BinaryStateEventDetectorModel extends TimeoutDetectorModel<BinaryStateDetectorVO>{
	
    private boolean state;
    
	public BinaryStateEventDetectorModel(BinaryStateDetectorVO data) {
		fromVO(data);
	}
	
	public BinaryStateEventDetectorModel() { }

	@Override
	public void fromVO(BinaryStateDetectorVO vo) {
	    super.fromVO(vo);
	    this.state = vo.isState();
	}
	
	@Override
	public BinaryStateDetectorVO toVO() {
	    BinaryStateDetectorVO vo = super.toVO();
	    vo.setState(state);
	    return vo;
	}
	
	
	public boolean isState(){
		return state;
	}
	
	public void setState(boolean state){
		this.state = state;
	}

    @Override
    public String getDetectorType() {
        return BinaryStateEventDetectorDefinition.TYPE_NAME;
    }
	
}
