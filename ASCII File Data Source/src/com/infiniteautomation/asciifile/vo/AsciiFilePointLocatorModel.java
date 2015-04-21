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
	
	@JsonGetter("pointIdentifier")
	@CSVColumnGetter(order=13, header="pointIdentifier")
	public String getPointIdentifier() {
	    return this.data.getPointIdentifier();
	}

	@JsonSetter("pointIdentifier")
	@CSVColumnSetter(order=13, header="pointIdentifier")
	public void setPointIdentifier(String pointIdentifier) {
	    this.data.setPointIdentifier(pointIdentifier);
	}

	@JsonGetter("valueRegex")
	@CSVColumnGetter(order=14, header="valueRegex")
	public String getValueRegex() {
	    return this.data.getValueRegex();
	}

	@JsonSetter("valueRegex")
	@CSVColumnSetter(order=14, header="valueRegex")
	public void setValueRegex(String valueRegex) {
	    this.data.setValueRegex(valueRegex);
	}

	@JsonGetter("pointIdentifierIndex")
	@CSVColumnGetter(order=15, header="pointIdentifierIndex")
	public int getPointIdentifierIndex() {
	    return this.data.getPointIdentifierIndex();
	}

	@JsonSetter("pointIdentifierIndex")
	@CSVColumnSetter(order=15, header="pointIdentifierIndex")
	public void setPointIdentifierIndex(int pointIdentifierIndex) {
	    this.data.setPointIdentifierIndex(pointIdentifierIndex);
	}

	@JsonGetter("valueIndex")
	@CSVColumnGetter(order=16, header="valueIndex")
	public int getValueIndex() {
	    return this.data.getValueIndex();
	}

	@JsonSetter("valueIndex")
	@CSVColumnSetter(order=16, header="valueIndex")
	public void setValueIndex(int valueIndex) {
	    this.data.setValueIndex(valueIndex);
	}

	@JsonGetter("dataType")
	@CSVColumnGetter(order=17, header="dataType")
	public String getDataType() {
	    return DataTypes.CODES.getCode(this.data.getDataTypeId());
	}

	@JsonSetter("dataType")
	@CSVColumnSetter(order=17, header="dataType")
	public void setDataType(String dataType) {
	    this.data.setDataType(DataTypes.CODES.getId(dataType));
	}

	@JsonGetter("hasTimestamp")
	@CSVColumnGetter(order=18, header="hasTimestamp")
	public boolean isHasTimestamp() {
	    return this.data.getHasTimestamp();
	}

	@JsonSetter("hasTimestamp")
	@CSVColumnSetter(order=18, header="hasTimestamp")
	public void setHasTimestamp(boolean hasTimestamp) {
	    this.data.setHasTimestamp(hasTimestamp);
	}

	@JsonGetter("timestampIndex")
	@CSVColumnGetter(order=19, header="timestampIndex")
	public int getTimestampIndex() {
	    return this.data.getTimestampIndex();
	}

	@JsonSetter("timestampIndex")
	@CSVColumnSetter(order=19, header="timestampIndex")
	public void setTimestampIndex(int timestampIndex) {
	    this.data.setTimestampIndex(timestampIndex);
	}

	@JsonGetter("timestampFormat")
	@CSVColumnGetter(order=20, header="timestampFormat")
	public String getTimestampFormat() {
	    return this.data.getTimestampFormat();
	}

	@JsonSetter("timestampFormat")
	@CSVColumnSetter(order=20, header="timestampFormat")
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
