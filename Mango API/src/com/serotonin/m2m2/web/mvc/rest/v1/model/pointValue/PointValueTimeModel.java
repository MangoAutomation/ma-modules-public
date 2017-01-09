/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumn;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

/**
 * 
 * TODO This model needs a cleanup
 * @author Terry Packer
 *
 */
@CSVEntity
public class PointValueTimeModel extends AbstractRestModel<PointValueTime>{

	//TODO Use String and then the DataTypePropertyEditor for CSV
	@JsonProperty("dataType")
	@CSVColumn(header="dataType", order = 0)
	private DataTypeEnum type;
	
	//TODO Change this to a DataValue and fix up the PropertyEditor and create a model for a DataValue
	@JsonProperty("value")
	@CSVColumn(header="value", order = 1, editor=DataValuePropertyEditor.class)
	private Object value;
	
	@JsonProperty("timestamp")
	@CSVColumn(header="timestamp", order = 2)
	private long timestamp;
	
	@JsonProperty("annotation")
	@CSVColumn(header="annotation", order = 3)
	private String annotation;
	
	public PointValueTimeModel(){
		super(null);
	}
	/**
	 * 
	 * 
	 * @param data - PointValueTime object
	 */
	public PointValueTimeModel(PointValueTime data) {
		super(data);
		
		this.type = DataTypeEnum.convertTo(data.getValue().getDataType());
		if(type != DataTypeEnum.IMAGE){
			this.value = data.getValue().getObjectValue();
		}
		this.timestamp = data.getTime();
		
		if(data.isAnnotated())
			this.annotation = ((AnnotatedPointValueTime) data).getAnnotation(Common.getTranslations());
		
	}
	
	
	public DataTypeEnum getType() {
		return type;
	}
	public void setType(DataTypeEnum type) {
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public PointValueTime getData(){
		DataValue dataValue = null;
		switch(this.type){
			case ALPHANUMERIC:
				dataValue = new AlphanumericValue((String) this.value);
				break;
			case BINARY:
				dataValue = new BinaryValue((Boolean)this.value);
				break;
			case MULTISTATE:
				dataValue = new MultistateValue(((Number)this.value).intValue());
				break;
			case NUMERIC:
				dataValue = new NumericValue(((Number)this.value).doubleValue());
				break;
			case IMAGE:
				return this.data;
		}
		
		if(this.annotation != null)
			return new AnnotatedPointValueTime(dataValue, this.timestamp, new TranslatableMessage("common.default", this.annotation));
		else
			return new PointValueTime(dataValue, this.timestamp);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel#validate(com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult)
	 */
	@Override
	public boolean validate(){
		return true;
	}
	
	
}
