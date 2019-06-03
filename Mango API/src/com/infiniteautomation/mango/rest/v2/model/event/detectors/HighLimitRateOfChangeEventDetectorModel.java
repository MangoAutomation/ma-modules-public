/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.module.definitions.event.detectors.HighLimitRateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.HighLimitRateOfChangeDetectorVO;
import com.serotonin.m2m2.vo.event.detector.HighLimitRateOfChangeDetectorVO.ComparisonMode;

/**
 * @author Terry Packer
 *
 */
public class HighLimitRateOfChangeEventDetectorModel extends TimeoutDetectorModel<HighLimitRateOfChangeDetectorVO> {
    
    private double rateOfChangeThreshold;
    private Double resetThreshold;
    private TimePeriod rateOfChangeDuration;
    private ComparisonMode comparisonMode;
    
    public HighLimitRateOfChangeEventDetectorModel(HighLimitRateOfChangeDetectorVO vo) {
        fromVO(vo);
    }
    
    public HighLimitRateOfChangeEventDetectorModel() { }
    
     @Override
    public void fromVO(HighLimitRateOfChangeDetectorVO vo) {
        super.fromVO(vo);
        this.rateOfChangeThreshold = vo.getRateOfChangeThreshold();
        this.resetThreshold = vo.getResetThreshold();
        this.comparisonMode = vo.getComparisonMode();
        this.rateOfChangeDuration = new TimePeriod(vo.getRateOfChangeDurationPeriods(), TimePeriodType.convertTo(vo.getRateOfChangeDurationType()));
    }
    @Override
    public HighLimitRateOfChangeDetectorVO toVO() {
        HighLimitRateOfChangeDetectorVO vo = super.toVO();
        vo.setRateOfChangeThreshold(rateOfChangeThreshold);
        vo.setResetThreshold(resetThreshold);
        vo.setComparisonMode(comparisonMode);
        if(rateOfChangeDuration != null) {
            vo.setRateOfChangeDurationPeriods(rateOfChangeDuration.getPeriods());
            vo.setRateOfChangeDurationType(TimePeriodType.convertFrom(rateOfChangeDuration.getType()));
        }
        return vo;
    }
    
    public double getRateOfChangeThreshold() {
        return rateOfChangeThreshold;
    }

    public void setRateOfChangeThreshold(double rateOfChangeThreshold) {
        this.rateOfChangeThreshold = rateOfChangeThreshold;
    }

    public Double getResetThreshold() {
        return resetThreshold;
    }

    public void setResetThreshold(Double resetThreshold) {
        this.resetThreshold = resetThreshold;
    }

    public TimePeriod getRateOfChangeDuration() {
        return rateOfChangeDuration;
    }

    public void setRateOfChangeDuration(TimePeriod rateOfChangeDuration) {
        this.rateOfChangeDuration = rateOfChangeDuration;
    }

    public ComparisonMode getComparisonMode() {
        return comparisonMode;
    }

    public void setComparisonMode(ComparisonMode comparisonMode) {
        this.comparisonMode = comparisonMode;
    }

    @Override
    public String getDetectorType() {
        return HighLimitRateOfChangeDetectorDefinition.TYPE_NAME;
    }

}
