/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.asciifile.vo;


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
@CSVEntity(typeName=AsciiFilePointLocatorModelDefinition.TYPE_NAME)
public class AsciiFilePointLocatorModel extends PointLocatorModel<AsciiFilePointLocatorVO>{

	/**
	 * @param data
	 */
	public AsciiFilePointLocatorModel(AsciiFilePointLocatorVO data) {
		super(data);
	}
	public AsciiFilePointLocatorModel() {
		super(new AsciiFilePointLocatorVO());
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
	
	@CSVColumnGetter(order=10, header="pointIdentifierIndex")
	@JsonGetter()
	public int getPointIdentifierIndex() {
		return this.data.getPointIdentifierIndex();
	}
	@CSVColumnSetter(order=10, header="pointIdentifierIndex")
	@JsonSetter()
	public void setPointIdentifierIndex(int pointIdentifierIndex) {
		this.data.setPointIdentifierIndex(pointIdentifierIndex);
	}
	
	@CSVColumnGetter(order=11, header="valueIndex")
	@JsonGetter()
	public int getValueIndex() {
		return this.data.getValueIndex();
	}
	@CSVColumnSetter(order=11, header="valueIndex")
	@JsonSetter()
	public void setValueIndex(int valueIndex) {
		this.data.setValueIndex(valueIndex);
	}
	
	@CSVColumnGetter(order=12, header="dataType")
	@JsonGetter()
	public String getDataType() {
		return DataTypes.CODES.getCode(this.data.getDataTypeId());
	}
	@CSVColumnSetter(order=12, header="dataType")
	@JsonSetter()
	public void setDataType(String dataType) {
		this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}
	
	@CSVColumnGetter(order=13, header="hasTimestamp")
	@JsonGetter()
	public boolean isHasTimestamp() {
		return this.data.getHasTimestamp();
	}
	@CSVColumnSetter(order=13, header="hasTimestamp")
	@JsonSetter()
	public void setHasTimestamp(boolean hasTimestamp) {
		this.data.setHasTimestamp(hasTimestamp);
	}
	@CSVColumnGetter(order=14, header="timestampIndex")
	@JsonGetter()
	public int getTimestampIndex() {
		return this.data.getTimestampIndex();
	}
	@CSVColumnSetter(order=14, header="timestampIndex")
	@JsonSetter()
	public void setTimestampIndex(int timestampIndex) {
		this.data.setTimestampIndex(timestampIndex);
	}
	
	@CSVColumnGetter(order=15, header="timestampFormat")
	@JsonGetter()
	public String getTimestampFormat() {
		return this.data.getTimestampFormat();
	}
	@CSVColumnSetter(order=15, header="timestampFormat")
	@JsonSetter()
	public void setTimestampFormat(String timestampFormat) {
		this.data.setTimestampFormat(timestampFormat);
	}
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return AsciiFilePointLocatorModelDefinition.TYPE_NAME;
	}

}
