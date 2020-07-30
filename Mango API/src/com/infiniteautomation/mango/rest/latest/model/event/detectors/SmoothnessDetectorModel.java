/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.SmoothnessEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.SmoothnessDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class SmoothnessDetectorModel extends TimeoutDetectorModel<SmoothnessDetectorVO>{

    private double limit;
    private double boxcar;
    
	public SmoothnessDetectorModel(SmoothnessDetectorVO data) {
		fromVO(data);
	}

	public SmoothnessDetectorModel() { }
	
	@Override
	public void fromVO(SmoothnessDetectorVO vo) {
	    super.fromVO(vo);
	    this.limit = vo.getLimit();
	    this.boxcar = vo.getBoxcar();
	}
	
	@Override
	public SmoothnessDetectorVO toVO() {
	    SmoothnessDetectorVO vo = super.toVO();
	    vo.setLimit(limit);
	    vo.setBoxcar(boxcar);
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
     * @return the boxcar
     */
    public double getBoxcar() {
        return boxcar;
    }

    /**
     * @param boxcar the boxcar to set
     */
    public void setBoxcar(double boxcar) {
        this.boxcar = boxcar;
    }

    @Override
    public String getDetectorType() {
        return SmoothnessEventDetectorDefinition.TYPE_NAME;
    }
	

}
