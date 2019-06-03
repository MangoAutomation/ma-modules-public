/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.module.definitions.event.detectors.HighLimitRateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.HighLimitRateOfChangeDetectorVO;

/**
 * @author Terry Packer
 *
 */
public class HighLimitRateOfChangeEventDetectorModel extends TimeoutDetectorModel<HighLimitRateOfChangeDetectorVO> {

    private double change;
    private double resetChange;
    private boolean useResetChange;
    private boolean notHigher;
    private TimePeriod rocDuration;
    
    public HighLimitRateOfChangeEventDetectorModel(HighLimitRateOfChangeDetectorVO vo) {
        fromVO(vo);
    }
    
    public HighLimitRateOfChangeEventDetectorModel() { }
    
     @Override
    public void fromVO(HighLimitRateOfChangeDetectorVO vo) {
        super.fromVO(vo);
        this.change = vo.getChange();
        this.resetChange = vo.getResetChange();
        this.useResetChange = vo.isUseResetChange();
        this.notHigher = vo.isNotHigher();
        this.rocDuration = new TimePeriod(vo.getRocDuration(), TimePeriodType.convertTo(vo.getRocDurationType()));
    }
    @Override
    public HighLimitRateOfChangeDetectorVO toVO() {
        HighLimitRateOfChangeDetectorVO vo = super.toVO();
        vo.setChange(change);
        vo.setResetChange(resetChange);
        vo.setUseResetChange(useResetChange);
        vo.setNotHigher(notHigher);
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

    public boolean isNotHigher() {
        return notHigher;
    }

    public void setNotHigher(boolean notHigher) {
        this.notHigher = notHigher;
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
