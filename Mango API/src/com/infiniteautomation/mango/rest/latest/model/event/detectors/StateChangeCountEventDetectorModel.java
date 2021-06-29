/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.StateChangeCountEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.StateChangeCountDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class StateChangeCountEventDetectorModel extends TimeoutDetectorModel<StateChangeCountDetectorVO>{
	
    private int changeCount;
    
	public StateChangeCountEventDetectorModel(StateChangeCountDetectorVO data) {
		fromVO(data);
	}

	public StateChangeCountEventDetectorModel() { }
	
	@Override
	public void fromVO(StateChangeCountDetectorVO vo) {
	    super.fromVO(vo);
	    this.changeCount = vo.getChangeCount();
	}
	
	@Override
	public StateChangeCountDetectorVO toVO() {
	    StateChangeCountDetectorVO vo = super.toVO();
	    vo.setChangeCount(changeCount);
	    return vo;
	}
	
	public int getChangeCount() {
		return this.changeCount;
	}

	public void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}

    @Override
    public String getDetectorType() {
        return StateChangeCountEventDetectorDefinition.TYPE_NAME;
    }
}
