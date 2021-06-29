/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.AbstractPointLocatorModel;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.virtual.vo.AlternateBooleanChangeVO;
import com.serotonin.m2m2.virtual.vo.AnalogAttractorChangeVO;
import com.serotonin.m2m2.virtual.vo.BrownianChangeVO;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.IncrementAnalogChangeVO;
import com.serotonin.m2m2.virtual.vo.IncrementMultistateChangeVO;
import com.serotonin.m2m2.virtual.vo.NoChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomAnalogChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomBooleanChangeVO;
import com.serotonin.m2m2.virtual.vo.RandomMultistateChangeVO;
import com.serotonin.m2m2.virtual.vo.SinusoidalChangeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;

/**
 * TODO rest v3 this class should abstract and we should create
 *  models for all the different types of VOs in the Point Locator.
 *
 * Then the definitions can be matched on type such as:
 *
 * PL.VIRTUAL.AlternateBooleanChange
 * PL.VIRTUAL.AnalogAttractorChange
 * PL.VIRTUAL.BrownianChange
 * etc.
 *
 * This will greatly simplify the Models and be much clearer on what the point is
 *
 *
 * @author Terry Packer
 *
 */
public class VirtualPointLocatorModel extends AbstractPointLocatorModel<VirtualPointLocatorVO>{

    public static final String TYPE_NAME = "PL.VIRTUAL";

    private String changeType;
    private double maxChange;
    private double volatility;
    private String attractionPointXid;
    private double min;
    private double max;
    private double change;
    private boolean roll;
    private int[] values;
    private double amplitude;
    private double offset;
    private double period;
    private double phaseShift;
    private String startValue;


    public VirtualPointLocatorModel() { }

    public VirtualPointLocatorModel(VirtualPointLocatorVO vo) {
        super(vo);
    }

    @Override
    public void fromVO(VirtualPointLocatorVO locator) {
        super.fromVO(locator);
        this.changeType = ChangeTypeVO.CHANGE_TYPE_CODES.getCode(locator.getChangeTypeId());
        switch(locator.getChangeTypeId()) {
            case ChangeTypeVO.Types.ALTERNATE_BOOLEAN:
                this.startValue = locator.getAlternateBooleanChange().getStartValue();
                break;
            case ChangeTypeVO.Types.ANALOG_ATTRACTOR:
                this.startValue = locator.getAnalogAttractorChange().getStartValue();
                this.maxChange = locator.getAnalogAttractorChange().getMaxChange();
                this.volatility = locator.getAnalogAttractorChange().getVolatility();
                break;
            case ChangeTypeVO.Types.BROWNIAN:
                this.startValue = locator.getBrownianChange().getStartValue();
                this.min = locator.getBrownianChange().getMin();
                this.max = locator.getBrownianChange().getMax();
                this.maxChange = locator.getBrownianChange().getMaxChange();
                break;
            case ChangeTypeVO.Types.INCREMENT_ANALOG:
                this.startValue = locator.getIncrementAnalogChange().getStartValue();
                this.min = locator.getIncrementAnalogChange().getMin();
                this.max = locator.getIncrementAnalogChange().getMax();
                this.change = locator.getIncrementAnalogChange().getChange();
                this.roll = locator.getIncrementAnalogChange().isRoll();
                break;
            case ChangeTypeVO.Types.INCREMENT_MULTISTATE:
                this.startValue = locator.getIncrementMultistateChange().getStartValue();
                this.values = locator.getIncrementMultistateChange().getValues();
                this.roll =  locator.getIncrementMultistateChange().isRoll();
                break;
            case ChangeTypeVO.Types.NO_CHANGE:
                this.startValue = locator.getNoChange().getStartValue();
                break;
            case ChangeTypeVO.Types.RANDOM_ANALOG:
                this.startValue = locator.getRandomAnalogChange().getStartValue();
                this.min = locator.getRandomAnalogChange().getMin();
                this.max = locator.getRandomAnalogChange().getMax();
                break;
            case ChangeTypeVO.Types.RANDOM_BOOLEAN:
                this.startValue = locator.getRandomBooleanChange().getStartValue();
                break;
            case ChangeTypeVO.Types.RANDOM_MULTISTATE:
                this.startValue = locator.getRandomMultistateChange().getStartValue();
                this.values = locator.getRandomMultistateChange().getValues();
                break;
            case ChangeTypeVO.Types.SINUSOIDAL:
                this.startValue = locator.getSinusoidalChange().getStartValue();
                this.amplitude = locator.getSinusoidalChange().getAmplitude();
                this.offset = locator.getSinusoidalChange().getOffset();
                this.period = locator.getSinusoidalChange().getPeriod();
                this.phaseShift = locator.getSinusoidalChange().getPhaseShift();
                break;
        }
    }

    @Override
    public VirtualPointLocatorVO toVO() {
        VirtualPointLocatorVO vo = new VirtualPointLocatorVO();
        vo.setDataTypeId(DataTypes.CODES.getId(dataType));
        vo.setChangeTypeId(ChangeTypeVO.CHANGE_TYPE_CODES.getId(changeType));
        vo.setSettable(settable);

        switch(vo.getChangeTypeId()) {
            case ChangeTypeVO.Types.ALTERNATE_BOOLEAN:
                AlternateBooleanChangeVO abc = new AlternateBooleanChangeVO();
                abc.setStartValue(startValue);
                vo.setAlternateBooleanChange(abc);
                break;
            case ChangeTypeVO.Types.ANALOG_ATTRACTOR:
                AnalogAttractorChangeVO aac = new AnalogAttractorChangeVO();
                aac.setStartValue(startValue);
                aac.setMaxChange(maxChange);
                aac.setVolatility(volatility);
                Integer attractionPointId = DataPointDao.getInstance().getIdByXid(attractionPointXid);
                if(attractionPointId != null) {
                    aac.setAttractionPointId(attractionPointId);
                }
                vo.setAnalogAttractorChange(aac);
                break;
            case ChangeTypeVO.Types.BROWNIAN:
                BrownianChangeVO bc = new BrownianChangeVO();
                bc.setStartValue(startValue);
                bc.setMin(min);
                bc.setMax(max);
                bc.setMaxChange(maxChange);
                vo.setBrownianChange(bc);
                break;
            case ChangeTypeVO.Types.INCREMENT_ANALOG:
                IncrementAnalogChangeVO iac = new IncrementAnalogChangeVO();
                iac.setStartValue(startValue);
                iac.setMin(min);
                iac.setMax(max);
                iac.setChange(change);
                iac.setRoll(roll);
                vo.setIncrementAnalogChange(iac);
                break;
            case ChangeTypeVO.Types.INCREMENT_MULTISTATE:
                IncrementMultistateChangeVO imc = new IncrementMultistateChangeVO();
                imc.setStartValue(startValue);
                imc.setValues(values);
                imc.setRoll(roll);
                vo.setIncrementMultistateChange(imc);
                break;
            case ChangeTypeVO.Types.NO_CHANGE:
                NoChangeVO nc = new NoChangeVO();
                nc.setStartValue(startValue);
                vo.setNoChange(nc);
                break;
            case ChangeTypeVO.Types.RANDOM_ANALOG:
                RandomAnalogChangeVO rac = new RandomAnalogChangeVO();
                rac.setStartValue(startValue);
                rac.setMin(min);
                rac.setMax(max);
                vo.setRandomAnalogChange(rac);
                break;
            case ChangeTypeVO.Types.RANDOM_BOOLEAN:
                RandomBooleanChangeVO rbc = new RandomBooleanChangeVO();
                rbc.setStartValue(startValue);
                vo.setRandomBooleanChange(rbc);
                break;
            case ChangeTypeVO.Types.RANDOM_MULTISTATE:
                RandomMultistateChangeVO rmc = new RandomMultistateChangeVO();
                rmc.setStartValue(startValue);
                rmc.setValues(values);
                vo.setRandomMultistateChange(rmc);
                break;
            case ChangeTypeVO.Types.SINUSOIDAL:
                SinusoidalChangeVO sc = new SinusoidalChangeVO();
                sc.setStartValue(startValue);
                sc.setAmplitude(amplitude);
                sc.setOffset(offset);
                sc.setPeriod(period);
                sc.setPhaseShift(phaseShift);
                vo.setSinusoidalChange(sc);
                break;
        }
        return vo;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public double getMaxChange() {
        return maxChange;
    }

    public void setMaxChange(double maxChange) {
        this.maxChange = maxChange;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public String getAttractionPointXid() {
        return attractionPointXid;
    }

    public void setAttractionPointXid(String attractionPointXid) {
        this.attractionPointXid = attractionPointXid;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public boolean isRoll() {
        return roll;
    }

    public void setRoll(boolean roll) {
        this.roll = roll;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public double getPhaseShift() {
        return phaseShift;
    }

    public void setPhaseShift(double phaseShift) {
        this.phaseShift = phaseShift;
    }

    public String getStartValue() {
        return startValue;
    }

    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }

    @Override
    public String getModelType() {
        return TYPE_NAME;
    }

}
