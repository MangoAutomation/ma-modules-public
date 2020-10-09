/*
 * Copyright (C) 2020 Infinite Automation Systems Inc. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.MultistateBitEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.MultistateBitDetectorVO;

/**
 * @author Jared Wiltshire
 */
public class MultistateBitEventDetectorModel extends TimeoutDetectorModel<MultistateBitDetectorVO> {

    private int bitmask;
    private boolean inverted;

	public MultistateBitEventDetectorModel(MultistateBitDetectorVO data) {
		fromVO(data);
	}

	public MultistateBitEventDetectorModel() { }

	@Override
	public void fromVO(MultistateBitDetectorVO vo) {
	    super.fromVO(vo);
	    this.bitmask = vo.getBitmask();
	    this.inverted = vo.isInverted();
	}

	@Override
	public MultistateBitDetectorVO toVO() {
		MultistateBitDetectorVO vo = super.toVO();
		vo.setBitmask(bitmask);
		vo.setInverted(inverted);
		return vo;
	}

	@Override
    public String getDetectorType() {
        return MultistateBitEventDetectorDefinition.TYPE_NAME;
    }

	public int getBitmask() {
		return bitmask;
	}

	public void setBitmask(int bitmask) {
		this.bitmask = bitmask;
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}
}
