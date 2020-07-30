/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AnalogChangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogChangeDetectorVO;

/**
 * 
 * @author Terry Packer
 */
public class AnalogChangeEventDetectorModel extends TimeoutDetectorModel<AnalogChangeDetectorVO>{


    private double limit; 
    private boolean checkIncrease;
    private boolean checkDecrease;
    private String updateEvent;
    
	public AnalogChangeEventDetectorModel(AnalogChangeDetectorVO data) {
		fromVO(data);
	}

    public AnalogChangeEventDetectorModel() {
        
    }	
    
    @Override
    public void fromVO(AnalogChangeDetectorVO vo) {
        super.fromVO(vo);
        this.limit = vo.getLimit();
        this.checkIncrease = vo.isCheckIncrease();
        this.checkDecrease = vo.isCheckDecrease();
        this.updateEvent = AnalogChangeDetectorVO.UPDATE_EVENT_TYPE_CODES.getCode(vo.getUpdateEvent());
    }
    
    @Override
    public AnalogChangeDetectorVO toVO() {
        AnalogChangeDetectorVO vo = super.toVO();
        vo.setLimit(limit);
        vo.setCheckIncrease(checkIncrease);
        vo.setCheckDecrease(checkDecrease);
        vo.setUpdateEvent(AnalogChangeDetectorVO.UPDATE_EVENT_TYPE_CODES.getId(updateEvent));
        
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
     * @return the checkIncrease
     */
    public boolean isCheckIncrease() {
        return checkIncrease;
    }

    /**
     * @param checkIncrease the checkIncrease to set
     */
    public void setCheckIncrease(boolean checkIncrease) {
        this.checkIncrease = checkIncrease;
    }

    /**
     * @return the checkDecrease
     */
    public boolean isCheckDecrease() {
        return checkDecrease;
    }

    /**
     * @param checkDecrease the checkDecrease to set
     */
    public void setCheckDecrease(boolean checkDecrease) {
        this.checkDecrease = checkDecrease;
    }

    /**
     * @return the updateEvent
     */
    public String getUpdateEvent() {
        return updateEvent;
    }

    /**
     * @param updateEvent the updateEvent to set
     */
    public void setUpdateEvent(String updateEvent) {
        this.updateEvent = updateEvent;
    }

    @Override
    public String getDetectorType() {
        return AnalogChangeEventDetectorDefinition.TYPE_NAME;
    }
}
