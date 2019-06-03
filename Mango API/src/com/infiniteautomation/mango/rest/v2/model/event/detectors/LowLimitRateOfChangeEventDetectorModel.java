/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.module.definitions.event.detectors.HighLimitRateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.LowLimitRateOfChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
public class LowLimitRateOfChangeEventDetectorModel extends TimeoutDetectorModel<LowLimitRateOfChangeDetectorVO> {

    private double change;
    private double resetChange;
    private boolean useResetChange;
    private boolean notLower;
    private TimePeriod rocDuration;
    
    public LowLimitRateOfChangeEventDetectorModel(LowLimitRateOfChangeDetectorVO vo) {
        fromVO(vo);
    }
    
    public LowLimitRateOfChangeEventDetectorModel() { }
    
     @Override
    public void fromVO(LowLimitRateOfChangeDetectorVO vo) {
        super.fromVO(vo);
        this.change = vo.getChange();
        this.resetChange = vo.getResetChange();
        this.useResetChange = vo.isUseResetChange();
        this.notLower = vo.isNotLower();
        this.rocDuration = new TimePeriod(vo.getRocDuration(), TimePeriodType.convertTo(vo.getRocDurationType()));
    }
    @Override
    public LowLimitRateOfChangeDetectorVO toVO() {
        LowLimitRateOfChangeDetectorVO vo = super.toVO();
        vo.setChange(change);
        vo.setResetChange(resetChange);
        vo.setUseResetChange(useResetChange);
        vo.setNotLower(notLower);
        if(rocDuration != null) {
            vo.setRocDuration(rocDuration.getPeriods());
            vo.setRocDurationType(TimePeriodType.convertFrom(rocDuration.getType()));
        }
        
        return vo;
    }
     
    
    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getResetChange() {
        return resetChange;
    }

    public void setResetChange(double resetChange) {
        this.resetChange = resetChange;
    }

    public boolean isUseResetChange() {
        return useResetChange;
    }

    public void setUseResetChange(boolean useResetChange) {
        this.useResetChange = useResetChange;
    }

    public boolean isNotLower() {
        return notLower;
    }

    public void setNotLower(boolean notLower) {
        this.notLower = notLower;
    }

    public TimePeriod getRocDuration() {
        return rocDuration;
    }

    public void setRocDuration(TimePeriod rocDuration) {
        this.rocDuration = rocDuration;
    }

    @Override
    public String getDetectorType() {
        return HighLimitRateOfChangeDetectorDefinition.TYPE_NAME;
    }

}
