package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity()
public class IncrementAnalogChangeModel extends VirtualPointLocatorModel {
	
	public IncrementAnalogChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.INCREMENT_ANALOG);
	}
	
	public IncrementAnalogChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.INCREMENT_ANALOG);
	}
	@Override
	public String getTypeName() {
		return IncrementAnalogChangeModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("min")
	public double getMin() {
	    return this.data.getIncrementAnalogChange().getMin();
	}

	@JsonSetter("min")
	public void setMin(double min) {
	    this.data.getIncrementAnalogChange().setMin(min);
	}

	@JsonGetter("max")
	public double getMax() {
	    return this.data.getIncrementAnalogChange().getMax();
	}

	@JsonSetter("max")
	public void setMax(double max) {
	    this.data.getIncrementAnalogChange().setMax(max);
	}

	@JsonGetter("change")
	public double getChange() {
	    return this.data.getIncrementAnalogChange().getChange();
	}

	@JsonSetter("change")
	public void setChange(double change) {
	    this.data.getIncrementAnalogChange().setChange(change);
	}

	@JsonGetter("roll")
	public boolean isRoll() {
	    return this.data.getIncrementAnalogChange().isRoll();
	}

	@JsonSetter("roll")
	public void setRoll(boolean roll) {
	    this.data.getIncrementAnalogChange().setRoll(roll);
	}



}
