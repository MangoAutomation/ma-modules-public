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
@CSVEntity()
abstract public class VirtualPointLocatorModel extends PointLocatorModel<VirtualPointLocatorVO>{

	/**
	 * @param data
	 */
	public VirtualPointLocatorModel(VirtualPointLocatorVO data) {
		super(data);
	}

	public VirtualPointLocatorModel() {
		super(new VirtualPointLocatorVO());
	}

	@JsonGetter("changeType")
	@CSVColumnGetter(order=13, header="changeType")
	public String getChangeType() {
	    return ChangeTypeVO.CHANGE_TYPE_CODES.getCode(this.data.getChangeTypeId());
	}

	@JsonSetter("changeType")
	@CSVColumnSetter(order=13, header="changeType")
	public void setChangeType(String changeType) {
	    this.data.setChangeTypeId(ChangeTypeVO.CHANGE_TYPE_CODES.getId(changeType));
	}


	@JsonSetter("dataType")
	@CSVColumnSetter(order=10, header="dataType")
	@Override
	public void setDataTypeId(String dataType) {
	    this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}

	@JsonSetter("settable")
	@CSVColumnSetter(order=11, header="settable")
	@Override
	public void setSettable(boolean settable) { 
		this.data.setSettable(settable);
	}	
	
	
	//Hack to allow all values in a CSV
	@JsonIgnore
	@CSVColumnGetter(order=14, header="maxChange")
	public double getMaxChange() {
	    return this.data.getAnalogAttractorChange().getMaxChange();
	}

	@JsonIgnore
	@CSVColumnSetter(order=14, header="maxChange")
	public void setMaxChange(double maxChange) {
	    this.data.getAnalogAttractorChange().setMaxChange(maxChange);
	}

	@JsonIgnore
	@CSVColumnGetter(order=15, header="volatility")
	public double getVolatility() {
	    return this.data.getAnalogAttractorChange().getVolatility();
	}

	@JsonIgnore
	@CSVColumnSetter(order=15, header="volatility")
	public void setVolatility(double volatility) {
	    this.data.getAnalogAttractorChange().setVolatility(volatility);
	}

	@JsonIgnore
	@CSVColumnGetter(order=16, header="attractionPointXid")
	public String getAttractionPointXid() {
		DataPointVO dpvo = DataPointDao.instance.get(this.data.getAnalogAttractorChange().getAttractionPointId());
		if(dpvo != null)
			return dpvo.getXid();
		return "";
	}

	@JsonIgnore
	@CSVColumnSetter(order=16, header="attractionPointXid")
	public void setAttractionPointXid(String attractionPointXid) {
		DataPointVO dpvo = DataPointDao.instance.getByXid(attractionPointXid);
		if(dpvo != null)
			this.data.getAnalogAttractorChange().setAttractionPointId(dpvo.getId());
	}
	
	
	@JsonIgnore
	@CSVColumnGetter(order=17, header="min")
	public double getMin() {
	    return this.data.getBrownianChange().getMin();
	}

	@JsonIgnore
	@CSVColumnSetter(order=17, header="min")
	public void setMin(double min) {
	    this.data.getBrownianChange().setMin(min);
	}

	@JsonIgnore
	@CSVColumnGetter(order=18, header="max")
	public double getMax() {
	    return this.data.getBrownianChange().getMax();
	}

	@JsonIgnore
	@CSVColumnSetter(order=18, header="max")
	public void setMax(double max) {
	    this.data.getBrownianChange().setMax(max);
	}
	
	@JsonIgnore
	@CSVColumnGetter(order=19, header="change")
	public double getChange() {
	    return this.data.getIncrementAnalogChange().getChange();
	}

	@JsonIgnore
	@CSVColumnSetter(order=19, header="change")
	public void setChange(double change) {
	    this.data.getIncrementAnalogChange().setChange(change);
	}

	@JsonIgnore
	@CSVColumnGetter(order=20, header="roll")
	public boolean isRoll() {
	    return this.data.getIncrementAnalogChange().isRoll();
	}

	@JsonIgnore
	@CSVColumnSetter(order=20, header="roll")
	public void setRoll(boolean roll) {
	    this.data.getIncrementAnalogChange().setRoll(roll);
	}
	
	@JsonIgnore
	@CSVColumnGetter(order=21, header="values", editor=IntArrayPropertyEditor.class)
	public int[] getValues() {
	    return this.data.getIncrementMultistateChange().getValues();
	}

	@JsonIgnore
	@CSVColumnSetter(order=21, header="values",  editor=IntArrayPropertyEditor.class)
	public void setValues(int[] values) {
	    this.data.getIncrementMultistateChange().setValues(values);
	}
	
	@JsonIgnore
	@CSVColumnGetter(order=22, header="amplitude")
	public double getAmplitude() {
	    return this.data.getSinusoidalChange().getAmplitude();
	}

	@JsonIgnore
	@CSVColumnSetter(order=22, header="amplitude")
	public void setAmplitude(double amplitude) {
	    this.data.getSinusoidalChange().setAmplitude(amplitude);
	}

	@JsonIgnore
	@CSVColumnGetter(order=23, header="offset")
	public double getOffset() {
	    return this.data.getSinusoidalChange().getOffset();
	}

	@JsonIgnore
	@CSVColumnSetter(order=23, header="offset")
	public void setOffset(double offset) {
	    this.data.getSinusoidalChange().setOffset(offset);
	}

	@JsonIgnore
	@CSVColumnGetter(order=24, header="period")
	public double getPeriod() {
	    return this.data.getSinusoidalChange().getPeriod();
	}

	@JsonIgnore
	@CSVColumnSetter(order=24, header="period")
	public void setPeriod(double period) {
	    this.data.getSinusoidalChange().setPeriod(period);
	}

	@JsonIgnore
	@CSVColumnGetter(order=25, header="phaseShift")
	public double getPhaseShift() {
	    return this.data.getSinusoidalChange().getPhaseShift();
	}

	@JsonIgnore
	@CSVColumnSetter(order=25, header="phaseShift")
	public void setPhaseShift(double phaseShift) {
	    this.data.getSinusoidalChange().setPhaseShift(phaseShift);
	}

	
	
}
