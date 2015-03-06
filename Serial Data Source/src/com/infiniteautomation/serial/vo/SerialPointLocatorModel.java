/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

/**
 * @author Terry Packer
 *
 */
@CSVEntity(typeName=SerialPointLocatorModelDefinition.TYPE_NAME)
public class SerialPointLocatorModel extends PointLocatorModel<SerialPointLocatorVO>{

	/**
	 * @param data
	 */
	public SerialPointLocatorModel(SerialPointLocatorVO vo) {
		super(vo);
	}

	public SerialPointLocatorModel() {
		super(new SerialPointLocatorVO());
	}

	@JsonGetter()
	@CSVColumnGetter(order=8, header="pointIdentifier")
	public String getPointIdentifier() {
		return this.data.getPointIdentifier();
	}
	@CSVColumnSetter(order=8, header="pointIdentifier")
	@JsonSetter()
	public void setPointIdentifier(String pointIdentifier) {
		this.data.setPointIdentifier(pointIdentifier);
	}


	@CSVColumnGetter(order=9, header="valueRegex")
	@JsonGetter()
	public String getValueRegex() {
		return this.data.getValueRegex();
	}
	@CSVColumnSetter(order=9, header="valueRegex")
	@JsonSetter()
	public void setValueRegex(String valueRegex) {
		this.data.setValueRegex(valueRegex);
	}

	@CSVColumnGetter(order=10, header="valueIndex")
	@JsonGetter()
	public int getValueIndex() {
		return this.data.getValueIndex();
	}
	@CSVColumnSetter(order=10, header="valueIndex")
	@JsonSetter()
	public void setValueIndex(int valueIndex) {
		this.data.setValueIndex(valueIndex);
	}

	@CSVColumnGetter(order=11, header="dataType")
	@JsonGetter()
	public String getDataType() {
		return DataTypes.CODES.getCode(this.data.getDataTypeId());
	}
	@CSVColumnSetter(order=11, header="dataType")
	@JsonSetter()
	public void setDataType(String dataType) {
		this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return SerialPointLocatorModelDefinition.TYPE_NAME;
	}


}
