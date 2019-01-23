/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO.Types;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.IntArrayPropertyEditor;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

/**
 * TODO this class should be removed and we should create
 * models for all the different types of VOs in the Point Locator.
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
@CSVEntity(typeName=VirtualPointLocatorModel.TYPE_NAME)
public class VirtualPointLocatorModel extends PointLocatorModel<VirtualPointLocatorVO>{

    public static final String TYPE_NAME = "PL.VIRTUAL";
	
    /**
	 * @param data
	 */
	public VirtualPointLocatorModel(VirtualPointLocatorVO data) {
		super(data);
	}

	public VirtualPointLocatorModel() {
		super(new VirtualPointLocatorVO());
	}

	@JsonSetter("dataType")
	@Override
	public void setDataTypeId(String dataType) {
	    this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}

	@JsonSetter("settable")
	@Override
	public void setSettable(boolean settable) { 
		this.data.setSettable(settable);
	}	
	
	
	@JsonGetter("changeType")
	@CSVColumnGetter(order=18, header="changeType")
	public String getChangeType() {
	    return ChangeTypeVO.CHANGE_TYPE_CODES.getCode(this.data.getChangeTypeId());
	}

	@JsonSetter("changeType")
	@CSVColumnSetter(order=18, header="changeType")
	public void setChangeType(String changeType) {
	    this.data.setChangeTypeId(ChangeTypeVO.CHANGE_TYPE_CODES.getId(changeType));
	}
	
	//Hack to allow all values in a CSV
	@CSVColumnGetter(order=19, header="maxChange")
	public double getMaxChange() {
		switch(this.data.getChangeTypeId()){
		case Types.ANALOG_ATTRACTOR:
			return this.data.getAnalogAttractorChange().getMaxChange();
		case Types.BROWNIAN:
			return this.data.getBrownianChange().getMaxChange();
		case Types.INCREMENT_ANALOG:
		case Types.ALTERNATE_BOOLEAN:
		case Types.INCREMENT_MULTISTATE:
		case Types.NO_CHANGE:
		case Types.RANDOM_ANALOG:
		case Types.RANDOM_BOOLEAN:
		case Types.RANDOM_MULTISTATE:
		case Types.SINUSOIDAL:
		default:
			return 0;
		}
	}

	@CSVColumnSetter(order=19, header="maxChange")
	public void setMaxChange(double maxChange) {
		this.data.getAnalogAttractorChange().setMaxChange(maxChange);
		this.data.getBrownianChange().setMaxChange(maxChange);
	}

	@CSVColumnGetter(order=20, header="volatility")
	public double getVolatility() {
	    return this.data.getAnalogAttractorChange().getVolatility();
	}

	@CSVColumnSetter(order=20, header="volatility")
	public void setVolatility(double volatility) {
	    this.data.getAnalogAttractorChange().setVolatility(volatility);
	}

	@CSVColumnGetter(order=21, header="attractionPointXid")
	public String getAttractionPointXid() {
		DataPointVO dpvo = DataPointDao.getInstance().get(this.data.getAnalogAttractorChange().getAttractionPointId());
		if(dpvo != null)
			return dpvo.getXid();
		return "";
	}

	@CSVColumnSetter(order=21, header="attractionPointXid")
	public void setAttractionPointXid(String attractionPointXid) {
		DataPointVO dpvo = DataPointDao.getInstance().getByXid(attractionPointXid);
		if(dpvo != null)
			this.data.getAnalogAttractorChange().setAttractionPointId(dpvo.getId());
	}
	
	@CSVColumnGetter(order=22, header="min")
	public double getMin() {
		switch(this.data.getChangeTypeId()){
		case Types.INCREMENT_ANALOG:
		    return this.data.getIncrementAnalogChange().getMin();
		case Types.BROWNIAN:
			return this.data.getBrownianChange().getMin();
		case Types.RANDOM_ANALOG:
			return this.data.getRandomAnalogChange().getMin();
		case Types.ALTERNATE_BOOLEAN:
		case Types.ANALOG_ATTRACTOR:
		case Types.INCREMENT_MULTISTATE:
		case Types.NO_CHANGE:
		case Types.RANDOM_BOOLEAN:
		case Types.RANDOM_MULTISTATE:
		case Types.SINUSOIDAL:
		default:
			return 0;
		}
	}

	@CSVColumnSetter(order=222, header="min")
	public void setMin(double min) {
	    this.data.getIncrementAnalogChange().setMin(min);
		this.data.getBrownianChange().setMin(min);
		this.data.getRandomAnalogChange().setMin(min);

	}

	@CSVColumnGetter(order=23, header="max")
	public double getMax() {
		switch(this.data.getChangeTypeId()){
		case Types.INCREMENT_ANALOG:
		    return this.data.getIncrementAnalogChange().getMax();
		case Types.BROWNIAN:
			return this.data.getBrownianChange().getMax();
		case Types.RANDOM_ANALOG:
			return this.data.getRandomAnalogChange().getMax();
		case Types.ALTERNATE_BOOLEAN:
		case Types.ANALOG_ATTRACTOR:
		case Types.INCREMENT_MULTISTATE:
		case Types.NO_CHANGE:
		case Types.RANDOM_BOOLEAN:
		case Types.RANDOM_MULTISTATE:
		case Types.SINUSOIDAL:
		default:
			return 0;
		}
	}

	@CSVColumnSetter(order=23, header="max")
	public void setMax(double max) {
	    this.data.getIncrementAnalogChange().setMax(max);
		this.data.getBrownianChange().setMax(max);
		this.data.getRandomAnalogChange().setMax(max);
	}
	
	@CSVColumnGetter(order=24, header="change")
	public double getChange() {
	    return this.data.getIncrementAnalogChange().getChange();
	}

	@CSVColumnSetter(order=24, header="change")
	public void setChange(double change) {
	    this.data.getIncrementAnalogChange().setChange(change);
	}

	@CSVColumnGetter(order=25, header="roll")
	public boolean isRoll() {
		switch(this.data.getChangeTypeId()){
		case Types.INCREMENT_ANALOG:
		    return this.data.getIncrementAnalogChange().isRoll();
		case Types.INCREMENT_MULTISTATE:
			return this.data.getIncrementMultistateChange().isRoll();
		case Types.ALTERNATE_BOOLEAN:
		case Types.ANALOG_ATTRACTOR:
		case Types.BROWNIAN:
		case Types.NO_CHANGE:
		case Types.RANDOM_ANALOG:
		case Types.RANDOM_BOOLEAN:
		case Types.RANDOM_MULTISTATE:
		case Types.SINUSOIDAL:
		default:
			return false;
		}
	}

	@CSVColumnSetter(order=25, header="roll")
	public void setRoll(boolean roll) {
	    this.data.getIncrementAnalogChange().setRoll(roll);
		this.data.getIncrementMultistateChange().setRoll(roll);
	}

	@CSVColumnGetter(order=26, header="values", editor=IntArrayPropertyEditor.class)
	public int[] getValues() {
		switch(this.data.getChangeTypeId()){
		case Types.INCREMENT_MULTISTATE:
			return this.data.getIncrementMultistateChange().getValues();
		case Types.RANDOM_MULTISTATE:
			return this.data.getRandomMultistateChange().getValues();
		case Types.INCREMENT_ANALOG:
		case Types.ALTERNATE_BOOLEAN:
		case Types.ANALOG_ATTRACTOR:
		case Types.BROWNIAN:
		case Types.NO_CHANGE:
		case Types.RANDOM_ANALOG:
		case Types.RANDOM_BOOLEAN:
		case Types.SINUSOIDAL:
		default:
			return new int[0];
		}	
	}

	@CSVColumnSetter(order=26, header="values",  editor=IntArrayPropertyEditor.class)
	public void setValues(int[] values) {
		this.data.getIncrementMultistateChange().setValues(values);
		this.data.getRandomMultistateChange().setValues(values);
	}
	
	@CSVColumnGetter(order=27, header="amplitude")
	public double getAmplitude() {
	    return this.data.getSinusoidalChange().getAmplitude();
	}

	@CSVColumnSetter(order=27, header="amplitude")
	public void setAmplitude(double amplitude) {
	    this.data.getSinusoidalChange().setAmplitude(amplitude);
	}

	@CSVColumnGetter(order=28, header="offset")
	public double getOffset() {
	    return this.data.getSinusoidalChange().getOffset();
	}

	@CSVColumnSetter(order=28, header="offset")
	public void setOffset(double offset) {
	    this.data.getSinusoidalChange().setOffset(offset);
	}

	@CSVColumnGetter(order=29, header="period")
	public double getPeriod() {
	    return this.data.getSinusoidalChange().getPeriod();
	}

	@CSVColumnSetter(order=29, header="period")
	public void setPeriod(double period) {
	    this.data.getSinusoidalChange().setPeriod(period);
	}

	@JsonIgnore
	@CSVColumnGetter(order=30, header="phaseShift")
	public double getPhaseShift() {
	    return this.data.getSinusoidalChange().getPhaseShift();
	}

	@CSVColumnSetter(order=30, header="phaseShift")
	public void setPhaseShift(double phaseShift) {
	    this.data.getSinusoidalChange().setPhaseShift(phaseShift);
	}

	@CSVColumnGetter(order=31, header="startValue")
	public String getStartValue() {
		switch(this.data.getChangeTypeId()){
		case Types.INCREMENT_ANALOG:
		    return this.data.getIncrementAnalogChange().getStartValue();
		case Types.ALTERNATE_BOOLEAN:
			return this.data.getAlternateBooleanChange().getStartValue();
		case Types.ANALOG_ATTRACTOR:
			return this.data.getAnalogAttractorChange().getStartValue();
		case Types.BROWNIAN:
			return this.data.getBrownianChange().getStartValue();
		case Types.INCREMENT_MULTISTATE:
			return this.data.getIncrementMultistateChange().getStartValue();
		case Types.NO_CHANGE:
			return this.data.getNoChange().getStartValue();
		case Types.RANDOM_ANALOG:
			return this.data.getRandomAnalogChange().getStartValue();
		case Types.RANDOM_BOOLEAN:
			return this.data.getRandomBooleanChange().getStartValue();
		case Types.RANDOM_MULTISTATE:
			return this.data.getRandomMultistateChange().getStartValue();
		case Types.SINUSOIDAL:
			return this.data.getSinusoidalChange().getStartValue();
		default:
			return "0";
		}
		
	}

	@CSVColumnSetter(order=31, header="startValue")
	public void setStartValue(String startValue) {
	    this.data.getIncrementAnalogChange().setStartValue(startValue);
		this.data.getAlternateBooleanChange().setStartValue(startValue);
		this.data.getAnalogAttractorChange().setStartValue(startValue);
		this.data.getBrownianChange().setStartValue(startValue);
		this.data.getIncrementMultistateChange().setStartValue(startValue);
		this.data.getNoChange().setStartValue(startValue);
		this.data.getRandomAnalogChange().setStartValue(startValue);
		this.data.getRandomBooleanChange().setStartValue(startValue);
		this.data.getRandomMultistateChange().setStartValue(startValue);
		this.data.getSinusoidalChange().setStartValue(startValue);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}
	
}
