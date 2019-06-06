/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.definitions.event.detectors.RateOfChangeDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO.ComparisonMode;

/**
 * @author Terry Packer
 *
 */
public class RateOfChangeEventDetectorModel extends TimeoutDetectorModel<RateOfChangeDetectorVO> {

    public enum CalculationMode {
        INSTANTANEOUS,
        AVERAGE
    }

    private double rateOfChangeThreshold;
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

        Double resetThreshold = vo.getResetThreshold();
        this.useResetThreshold  = resetThreshold != null;
        this.resetThreshold = this.useResetThreshold ? resetThreshold : 0;

        this.comparisonMode = vo.getComparisonMode();

        this.calculationMode = CalculationMode.AVERAGE;
        if (vo.getRateOfChangePeriods() > 0) {
            this.calculationMode = CalculationMode.AVERAGE;
            this.rateOfChangePeriod = new TimePeriod(vo.getRateOfChangePeriods(), TimePeriodType.convertTo(vo.getRateOfChangePeriodType()));
        } else {
            this.calculationMode = CalculationMode.INSTANTANEOUS;
            this.rateOfChangePeriod = new TimePeriod(0, TimePeriodType.SECONDS);
        }

        this.useAbsoluteValue = vo.isUseAbsoluteValue();
    }

    @Override
    public RateOfChangeDetectorVO toVO() {
        RateOfChangeDetectorVO vo = super.toVO();
        vo.setRateOfChangeThreshold(rateOfChangeThreshold);
        if (this.useResetThreshold) {
            vo.setResetThreshold(resetThreshold);
        } else {
            vo.setResetThreshold(null);
        }
        vo.setComparisonMode(comparisonMode);
        if (this.calculationMode == CalculationMode.AVERAGE) {
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
