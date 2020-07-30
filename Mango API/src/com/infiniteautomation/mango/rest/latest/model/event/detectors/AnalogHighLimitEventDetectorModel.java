/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AnalogHighLimitEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogHighLimitDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class AnalogHighLimitEventDetectorModel extends TimeoutDetectorModel<AnalogHighLimitDetectorVO>{
	
    private double limit;
    private double resetLimit;
    private boolean useResetLimit;
    private boolean notHigher;
    
	public AnalogHighLimitEventDetectorModel(AnalogHighLimitDetectorVO data) {
		fromVO(data);
	}
	
	public AnalogHighLimitEventDetectorModel() {
	    
	}
	
	@Override
	public void fromVO(AnalogHighLimitDetectorVO vo) {
	    super.fromVO(vo);
	    this.limit = vo.getLimit();
	    this.resetLimit = vo.getResetLimit();
	    this.useResetLimit = vo.isUseResetLimit();
	    this.notHigher = vo.isNotHigher();
	}
	
	@Override
	public AnalogHighLimitDetectorVO toVO() {
	    AnalogHighLimitDetectorVO vo = super.toVO();
	    vo.setLimit(limit);
	    vo.setResetLimit(resetLimit);
	    vo.setUseResetLimit(useResetLimit);
	    vo.setNotHigher(notHigher);
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
     * @return the notHigher
     */
    public boolean isNotHigher() {
        return notHigher;
    }

    /**
     * @param notHigher the notHigher to set
     */
    public void setNotHigher(boolean notHigher) {
        this.notHigher = notHigher;
    }

    @Override
    public String getDetectorType() {
        return AnalogHighLimitEventDetectorDefinition.TYPE_NAME;
    }

	
}