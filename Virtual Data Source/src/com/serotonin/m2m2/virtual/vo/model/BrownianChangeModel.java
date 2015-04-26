package com.serotonin.m2m2.virtual.vo.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;

@CSVEntity(typeName=BrownianChangeModelDefinition.TYPE_NAME)
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
	@CSVColumnGetter(order=14, header="min")
	public double getMin() {
	    return this.data.getBrownianChange().getMin();
	}

	@JsonSetter("min")
	@CSVColumnSetter(order=14, header="min")
	public void setMin(double min) {
	    this.data.getBrownianChange().setMin(min);
	}

	@JsonGetter("max")
	@CSVColumnGetter(order=15, header="max")
	public double getMax() {
	    return this.data.getBrownianChange().getMax();
	}

	@JsonSetter("max")
	@CSVColumnSetter(order=15, header="max")
	public void setMax(double max) {
	    this.data.getBrownianChange().setMax(max);
	}

	@JsonGetter("maxChange")
	@CSVColumnGetter(order=16, header="maxChange")
	public double getMaxChange() {
	    return this.data.getBrownianChange().getMaxChange();
	}

	@JsonSetter("maxChange")
	@CSVColumnSetter(order=16, header="maxChange")
	public void setMaxChange(double maxChange) {
	    this.data.getBrownianChange().setMaxChange(maxChange);
	}



}
