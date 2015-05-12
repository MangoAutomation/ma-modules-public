package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity()
public class BrownianChangeModel extends VirtualPointLocatorModel {
	
	public BrownianChangeModel(VirtualPointLocatorVO data) {
		super(data);
		this.data.setChangeTypeId(ChangeTypeVO.Types.BROWNIAN);
	}
	
	public BrownianChangeModel() {
		super(new VirtualPointLocatorVO());
		this.data.setChangeTypeId(ChangeTypeVO.Types.BROWNIAN);
	}
	@Override
	public String getTypeName() {
		return BrownianChangeModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("min")
	public double getMin() {
	    return this.data.getBrownianChange().getMin();
	}

	@JsonSetter("min")
	public void setMin(double min) {
	    this.data.getBrownianChange().setMin(min);
	}

	@JsonGetter("max")
	public double getMax() {
	    return this.data.getBrownianChange().getMax();
	}

	@JsonSetter("max")
	public void setMax(double max) {
	    this.data.getBrownianChange().setMax(max);
	}

	@JsonGetter("maxChange")
	public double getMaxChange() {
	    return this.data.getBrownianChange().getMaxChange();
	}

	@JsonSetter("maxChange")
	public void setMaxChange(double maxChange) {
	    this.data.getBrownianChange().setMaxChange(maxChange);
	}
}
