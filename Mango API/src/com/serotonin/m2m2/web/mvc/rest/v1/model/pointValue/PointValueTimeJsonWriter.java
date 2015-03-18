/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import javax.measure.converter.ConversionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public abstract class PointValueTimeJsonWriter {
	private final Log LOG = LogFactory.getLog(PointValueTimeJsonWriter.class);

	protected JsonGenerator jgen;
	protected DataPointVO vo;
	protected boolean useRendered;
	protected boolean unitConversion;
	protected final String noDataMessage;
	
	public PointValueTimeJsonWriter(JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion){
		this.jgen = jgen;
		this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.noDataMessage = new TranslatableMessage("common.stats.noDataForPeriod").translate(Common.getTranslations());
	}
	
	protected void writePointValueTime(double value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	protected void writePointValueTime(int value, long timestamp, String annotation) throws IOException{
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
    	jgen.writeNumberField("value", value);
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
	
	protected void writeNonNull(Double value, long time) throws ConversionException, IOException{
		if(value == null){
			if(useRendered){
				this.writePointValueTime(new AlphanumericValue(""), time, this.noDataMessage);
			}else{
				this.writePointValueTime(0.0D, time, this.noDataMessage);
			}
		}else{
	    	if(useRendered){
	    		//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, new PointValueTime(value, time));
				this.writePointValueTime(new AlphanumericValue(textValue), time, null);
			}else if(unitConversion){
				//Convert Value, must be numeric
				this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value), time, null);
			}else{
				this.writePointValueTime(value, time, null);

			}
		}
	}
	
	protected void writeNonNull(DataValue value, long time) throws ConversionException, IOException{
		if(value == null){
			if(useRendered){
				this.writePointValueTime(new AlphanumericValue(""), time, this.noDataMessage);
			}else{
				this.writePointValueTime(0.0D, time, this.noDataMessage);
			}
		}else{
	    	if(useRendered){
	    		//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, new PointValueTime(value, time));
				this.writePointValueTime(new AlphanumericValue(textValue), time, null);
			}else if(unitConversion){
				//Convert Value, must be numeric
				if (value instanceof NumericValue)
					this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()), time, null);
				else
					this.writePointValueTime(value, time, null);
			}else{
				this.writePointValueTime(value, time, null);
			}
		}
	}
	
	protected void writeNonNullIntegral(Double integral, long time) throws IOException{
    	if(useRendered){
    		//Convert to Alphanumeric Value
    		if(integral != null){
    			String textValue = Functions.getIntegralText(vo, integral);
    			this.writePointValueTime(new AlphanumericValue(textValue), time, null);
    		}else{
    			this.writePointValueTime(new AlphanumericValue(""), time, null);
    		}
		}else{ //No conversion possible
			if(integral == null)
				this.writePointValueTime(0.0D, time, this.noDataMessage);  
			else
				this.writePointValueTime(integral, time, null);  
		}
	}
	
	protected void writePointValueTime(DataValue value, long timestamp,
			String annotation) throws IOException {
		
		jgen.writeStartObject();
		jgen.writeStringField("annotation", annotation);
		
		if(value == null){
			jgen.writeNullField("value");
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					jgen.writeStringField("value", value.getStringValue());
				break;
				case DataTypes.BINARY:
					jgen.writeBooleanField("value", value.getBooleanValue());
				break;
				case DataTypes.MULTISTATE:
					jgen.writeNumberField("value", value.getIntegerValue());
				break;
				case DataTypes.NUMERIC:
					jgen.writeNumberField("value", value.getDoubleValue());
				break;
				default:
					jgen.writeStringField("value","unsupported-value-type");
					LOG.error("Unsupported data type for Point Value Time: " + value.getDataType());
				break;
			}
		}
		
    	jgen.writeNumberField("timestamp", timestamp);
    	jgen.writeEndObject();
	}
}
