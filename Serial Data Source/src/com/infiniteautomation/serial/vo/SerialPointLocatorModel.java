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
@CSVEntity(typeName=SerialPointLocatorModel.TYPE_NAME)
public class SerialPointLocatorModel extends PointLocatorModel<SerialPointLocatorVO>{

    public static final String TYPE_NAME = "PL.SERIAL";
    
	/**
	 * @param data
	 */
	public SerialPointLocatorModel(SerialPointLocatorVO vo) {
		super(vo);
	}

	public SerialPointLocatorModel() {
		super(new SerialPointLocatorVO());
	}

	@JsonGetter("pointIdentifier")
	@CSVColumnGetter(order=18, header="pointIdentifier")
	public String getPointIdentifier() {
	    return this.data.getPointIdentifier();
	}

	@JsonSetter("pointIdentifier")
	@CSVColumnSetter(order=18, header="pointIdentifier")
	public void setPointIdentifier(String pointIdentifier) {
	    this.data.setPointIdentifier(pointIdentifier);
	}

	@JsonGetter("valueRegex")
	@CSVColumnGetter(order=19, header="valueRegex")
	public String getValueRegex() {
	    return this.data.getValueRegex();
	}

	@JsonSetter("valueRegex")
	@CSVColumnSetter(order=19, header="valueRegex")
	public void setValueRegex(String valueRegex) {
	    this.data.setValueRegex(valueRegex);
	}

	@JsonGetter("valueIndex")
	@CSVColumnGetter(order=20, header="valueIndex")
	public int getValueIndex() {
	    return this.data.getValueIndex();
	}

	@JsonSetter("valueIndex")
	@CSVColumnSetter(order=20, header="valueIndex")
	public void setValueIndex(int valueIndex) {
	    this.data.setValueIndex(valueIndex);
	}

	@JsonSetter("dataType")
	@CSVColumnSetter(order=15, header="dataType")
	@Override
	public void setDataTypeId(String dataType) {
	    this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}


}
