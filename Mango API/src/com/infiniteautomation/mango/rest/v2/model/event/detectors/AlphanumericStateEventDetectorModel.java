/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AlphanumericStateEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AlphanumericStateDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class AlphanumericStateEventDetectorModel extends TimeoutDetectorModel<AlphanumericStateDetectorVO>{

    private String state;
    
	public AlphanumericStateEventDetectorModel(AlphanumericStateDetectorVO data) {
		fromVO(data);
	}

	public AlphanumericStateEventDetectorModel() { }
	
	@Override
	public void fromVO(AlphanumericStateDetectorVO vo) {
	    super.fromVO(vo);
	    this.state = vo.getState();
	}
	
	@Override
	public AlphanumericStateDetectorVO toVO() {
	    AlphanumericStateDetectorVO vo = super.toVO();
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
        return AlphanumericStateEventDetectorDefinition.TYPE_NAME;
    }
	
}
