/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.module.definitions.event.detectors.RateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO.ComparisonMode;

/**
 * @author Terry Packer
 *
 */
public class RateOfChangeEventDetectorModel extends TimeoutDetectorModel<RateOfChangeDetectorVO> {
    
    private double rateOfChangeThreshold;
    private Double resetThreshold;
    private TimePeriod rateOfChangePeriod;
    private ComparisonMode comparisonMode;
    private boolean useAbsoluteValue;
    
    public RateOfChangeEventDetectorModel(RateOfChangeDetectorVO vo) {
        fromVO(vo);
    }
    
    public RateOfChangeEventDetectorModel() { }
    
     @Override
    public void fromVO(RateOfChangeDetectorVO vo) {
        super.fromVO(vo);
        this.rateOfChangeThreshold = vo.getRateOfChangeThreshold();
        this.resetThreshold = vo.getResetThreshold();
        this.comparisonMode = vo.getComparisonMode();
        this.rateOfChangePeriod = new TimePeriod(vo.getRateOfChangePeriods(), TimePeriodType.convertTo(vo.getRateOfChangePeriodType()));
        this.useAbsoluteValue = vo.isUseAbsoluteValue();
     }
    @Override
    public RateOfChangeDetectorVO toVO() {
        RateOfChangeDetectorVO vo = super.toVO();
        vo.setRateOfChangeThreshold(rateOfChangeThreshold);
        vo.setResetThreshold(resetThreshold);
        vo.setComparisonMode(comparisonMode);
        if(rateOfChangePeriod != null) {
            vo.setRateOfChangePeriods(rateOfChangePeriod.getPeriods());
            vo.setRateOfChangePeriodType(TimePeriodType.convertFrom(rateOfChangePeriod.getType()));
        }
        vo.setUseAbsoluteValue(useAbsoluteValue);
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

    public TimePeriod getRateOfChangePeriod() {
        return rateOfChangePeriod;
    }

    public void setRateOfChangePeriod(TimePeriod rateOfChangePeriod) {
        this.rateOfChangePeriod = rateOfChangePeriod;
    }

    public ComparisonMode getComparisonMode() {
        return comparisonMode;
    }

    public void setComparisonMode(ComparisonMode comparisonMode) {
        this.comparisonMode = comparisonMode;
    }

    public boolean isUseAbsoluteValue() {
        return useAbsoluteValue;
    }

    public void setUseAbsoluteValue(boolean useAbsoluteValue) {
        this.useAbsoluteValue = useAbsoluteValue;
    }

    @Override
    public String getDetectorType() {
        return RateOfChangeDetectorDefinition.TYPE_NAME;
    }

}
