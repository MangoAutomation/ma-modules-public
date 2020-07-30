/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.MultistateStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.MultistateStateDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class MultistateStateEventDetectorModel extends TimeoutDetectorModel<MultistateStateDetectorVO>{

    private int state;
    
	public MultistateStateEventDetectorModel(MultistateStateDetectorVO data) {
		fromVO(data);
	}
	
	public MultistateStateEventDetectorModel() { }
	
	@Override
	public void fromVO(MultistateStateDetectorVO vo) {
	    super.fromVO(vo);
	    this.state = vo.getState();
	}
	
	@Override
	public MultistateStateDetectorVO toVO() {
	    MultistateStateDetectorVO vo = super.toVO();
	    vo.setState(state);
	    return vo;
	}
	

	public int getState(){
		return state;
	}
	
	public void setState(int state){
		this.state = state;
	}

    @Override
    public String getDetectorType() {
        return MultistateStateEventDetectorDefinition.TYPE_NAME;
    }
	
}
