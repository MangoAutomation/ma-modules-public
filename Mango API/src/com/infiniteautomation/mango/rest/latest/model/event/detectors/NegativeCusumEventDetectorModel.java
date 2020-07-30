/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.NegativeCusumEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.NegativeCusumDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class NegativeCusumEventDetectorModel extends TimeoutDetectorModel<NegativeCusumDetectorVO>{
	
    private double limit;
    private double weight;
    
	public NegativeCusumEventDetectorModel(NegativeCusumDetectorVO data) {
		fromVO(data);
	}

	public NegativeCusumEventDetectorModel() { }
	
	@Override
	public void fromVO(NegativeCusumDetectorVO vo) {
	    super.fromVO(vo);
	    this.limit = vo.getLimit();
	    this.weight = vo.getWeight();
	}
	
	@Override
	public NegativeCusumDetectorVO toVO() {
	    NegativeCusumDetectorVO vo = super.toVO();
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
        return NegativeCusumEventDetectorDefinition.TYPE_NAME;
    }
	
}
