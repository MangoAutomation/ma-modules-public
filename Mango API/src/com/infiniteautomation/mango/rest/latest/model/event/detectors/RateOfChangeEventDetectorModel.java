/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.infiniteautomation.mango.rest.latest.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriodType;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.definitions.event.detectors.RateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO.CalculationMode;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO.ComparisonMode;

/**
 * @author Terry Packer
 *
 */
public class RateOfChangeEventDetectorModel extends TimeoutDetectorModel<RateOfChangeDetectorVO> {

    private double rateOfChangeThreshold;
    private TimePeriodType rateOfChangeThresholdUnit;
    private boolean useResetThreshold;
    private double resetThreshold;
    private CalculationMode calculationMode;
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
        this.rateOfChangeThresholdUnit = TimePeriodType.convertTo(vo.getRateOfChangeThresholdPeriodType());
        this.useResetThreshold = vo.isUseResetThreshold();
        this.resetThreshold = vo.getResetThreshold();
        this.comparisonMode = vo.getComparisonMode();
        this.calculationMode = vo.getCalculationMode();
        this.rateOfChangePeriod = new TimePeriod(vo.getRateOfChangePeriods(), TimePeriodType.convertTo(vo.getRateOfChangePeriodType()));
        this.useAbsoluteValue = vo.isUseAbsoluteValue();
    }

    @Override
    public RateOfChangeDetectorVO toVO() {
        RateOfChangeDetectorVO vo = super.toVO();
        vo.setRateOfChangeThreshold(rateOfChangeThreshold);
        if(rateOfChangeThresholdUnit != null) {
            vo.setRateOfChangeThresholdPeriodType(TimePeriodType.convertFrom(rateOfChangeThresholdUnit));
        }
        vo.setUseResetThreshold(useResetThreshold);
        vo.setResetThreshold(resetThreshold);
        vo.setComparisonMode(comparisonMode);
        vo.setCalculationMode(calculationMode);
        
        if (this.calculationMode == CalculationMode.AVERAGE && rateOfChangePeriod != null) {
            vo.setRateOfChangePeriods(rateOfChangePeriod.getPeriods());
            vo.setRateOfChangePeriodType(TimePeriodType.convertFrom(rateOfChangePeriod.getType()));
        } else {
            vo.setRateOfChangePeriods(0);
            vo.setRateOfChangePeriodType(Common.TimePeriods.SECONDS);
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

    public TimePeriodType getRateOfChangeThresholdUnit() {
        return rateOfChangeThresholdUnit;
    }

    public void setRateOfChangeThresholdUnit(TimePeriodType rateOfChangeUnit) {
        this.rateOfChangeThresholdUnit = rateOfChangeUnit;
    }

    public double getResetThreshold() {
        return resetThreshold;
    }

    public void setResetThreshold(double resetThreshold) {
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

    public boolean isUseResetThreshold() {
        return useResetThreshold;
    }

    public void setUseResetThreshold(boolean useResetThreshold) {
        this.useResetThreshold = useResetThreshold;
    }

    public CalculationMode getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(CalculationMode calculationMode) {
        this.calculationMode = calculationMode;
    }

}
