/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeModel extends AbstractRestModel<PointValueTime>{

	@JsonProperty("dataType")
	private DataTypeEnum type;
	
	@JsonProperty("value")
	private Object value;
	
	@JsonProperty("timestamp")
	private long timestamp;
	
	@JsonProperty("annotation")
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
		this.value = data.getValue().getObjectValue();
		this.timestamp = data.getTime();
		
		if(data instanceof AnnotatedPointValueTime)
			this.annotation = ((AnnotatedPointValueTime) data).getAnnotation(Common.getTranslations());
		
	}

	
	
	
//	@JsonSetter("dataType")
//	public void setDataType(DataTypeEnum type){
//		if(this.data == null){
//			switch(type){
//			case ALPHANUMERIC:
//				this.data = new PointValueTime(new AlphanumericValue(null), 0);
//			case BINARY:
//				this.data = new PointValueTime(new BinaryValue(true), 0);
//			case MULTISTATE:
//				this.data = new PointValueTime(new MultistateValue(0), 0);
//			case NUMERIC:
//				this.data = new PointValueTime(new NumericValue(0), 0);
//			case IMAGE:
//				try {
//					this.data = new PointValueTime(new ImageValue(null), 0);
//				} catch (InvalidArgumentException e) {
//					e.printStackTrace();
//				}
//			}
//		}else{
//			//Set the data type
//			if(this.data.getValue().getDataType() != DataTypeEnum.convertFrom(type)){
//				
//			}
//		}
//	}
	
	
//	@JsonGetter("value")
//	public Object getValue(){
//		if(data == null)
//			return null;
//		else
//			switch(this.data.getValue().getDataType()){
//				case DataTypes.ALPHANUMERIC:
//				return this.data.getStringValue();
//				
//				case DataTypes.BINARY:
//				return this.data.getBooleanValue();
//				
//				case DataTypes.IMAGE:
//				return null; //Not supporting for now
//			
//				case DataTypes.MULTISTATE:
//				return this.data.getIntegerValue(); //Not supporting for now
//				
//				case DataTypes.NUMERIC:
//				return this.data.getDoubleValue();
//				
//				default:
//					throw new ShouldNeverHappenException("Unknown Data Type: " + this.data.getValue().getDataType());
//			}
//	}
//	@JsonSetter("value")
//	public void setValue(Object value){
//		if(this.data == null){
//			this.data = new PointValueTime(DataValue.objectToValue(value), 0);
//		}else{
//			if(this.data instanceof AnnotatedPointValueTime)
//				this.data = new AnnotatedPointValueTime(DataValue.objectToValue(value), this.data.getTime(), ((AnnotatedPointValueTime) this.data).getSourceMessage());
//			else
//				this.data = new PointValueTime(DataValue.objectToValue(value), this.data.getTime());
//		}
//	}
//
//	@JsonGetter("time")
//	public Date getDate(){
//		if(data == null)
//			return null;
//		else
//			return new Date(this.data.getTime());
//	}
//	@JsonSetter("time")
//	public void setTime(long time){
//		if(this.data == null){
//			this.data = new PointValueTime((DataValue)null,time);
//		}else{
//			if(this.data instanceof AnnotatedPointValueTime)
//				this.data = new AnnotatedPointValueTime(this.data.getValue(), time, ((AnnotatedPointValueTime) this.data).getSourceMessage());
//			else
//				this.data = new PointValueTime(this.data.getValue(), time);
//		}
//	}
//	@JsonGetter("timestamp")
//	public long getTime(){
//		return this.data.getTime();
//	}
//	
//	@JsonGetter("annotation")
//	public String getAnnotation(){
//		if(this.data instanceof AnnotatedPointValueTime){
//			return ((AnnotatedPointValueTime) this.data).getAnnotation(Common.getTranslations());
//		}else{
//			return null;
//		}
//	}
//	@JsonSetter("annotation")
//	public void setAnnotation(String annotation){
//		if(this.data == null){
//			this.data = new AnnotatedPointValueTime((DataValue)null, 0, new TranslatableMessage("common.default", annotation));
//		}else{
//			this.data = new AnnotatedPointValueTime(this.data.getValue(),
//					this.data.getTime(),
//					new TranslatableMessage("common.default", annotation));
//		}
//	}

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
		DataValue dataValue;
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
			default: 
				throw new ShouldNeverHappenException("Unsupported Data Type: " + this.type);
		}
		
		if(this.annotation != null)
			return new AnnotatedPointValueTime(dataValue, this.timestamp, new TranslatableMessage("common.default", this.annotation));
		else
			return new PointValueTime(dataValue, this.timestamp);
		
	}
	
	@Override
	public void validate(RestProcessResult<?> result) throws RestValidationFailedException{
		ProcessResult validation = new ProcessResult();
		
		validation.addContextualMessage("Point Value", "common.default", "Validation Not Implemented");
		
		
		if(validation.getHasMessages()){
			result.addRestMessage(HttpStatus.BAD_REQUEST, new TranslatableMessage("common.default", "Validation failed"));
			result.addValidationMessages(validation);
			throw new RestValidationFailedException(this, result);
		}
	}
	
	
}
