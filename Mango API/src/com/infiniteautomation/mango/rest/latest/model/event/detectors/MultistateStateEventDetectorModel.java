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
    private boolean multipleStates;
	/**
	 * takes precedence over state if not null
	 */
	private int[] states;
	private boolean inverted;

	public MultistateStateEventDetectorModel(MultistateStateDetectorVO data) {
		fromVO(data);
	}

	public MultistateStateEventDetectorModel() { }

	@Override
	public void fromVO(MultistateStateDetectorVO vo) {
	    super.fromVO(vo);
	    this.state = vo.getState();
	    this.states = vo.getStates();
	    this.multipleStates = this.states != null;
	    this.inverted = vo.isInverted();
	}

	@Override
	public MultistateStateDetectorVO toVO() {
	    MultistateStateDetectorVO vo = super.toVO();
	    vo.setState(state);
	    if (multipleStates) {
			vo.setStates(states);
		}
		vo.setInverted(inverted);
	    return vo;
	}


	public int getState(){
		return state;
	}

	public void setState(int state){
		this.state = state;
	}

	public int[] getStates() {
		return states;
	}

	public void setStates(int[] states) {
		this.states = states;
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	public boolean isMultipleStates() {
		return multipleStates;
	}

	public void setMultipleStates(boolean multipleStates) {
		this.multipleStates = multipleStates;
	}

	@Override
    public String getDetectorType() {
        return MultistateStateEventDetectorDefinition.TYPE_NAME;
    }

}
