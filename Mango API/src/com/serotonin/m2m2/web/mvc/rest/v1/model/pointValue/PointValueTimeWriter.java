/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import javax.measure.converter.ConversionException;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
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
public abstract class PointValueTimeWriter {

	protected DataPointVO vo;
	protected boolean useRendered;
	protected boolean unitConversion;
	protected final String noDataMessage;
	
	public PointValueTimeWriter(DataPointVO vo, boolean useRendered, boolean unitConversion){
		this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.noDataMessage = new TranslatableMessage("common.stats.noDataForPeriod").translate(Common.getTranslations());
	}
	
	public abstract void writePointValueTime(double value, long timestamp, String annotation) throws IOException;
	
	public abstract void writePointValueTime(int value, long timestamp, String annotation) throws IOException;

	public abstract void writePointValueTime(String value, long timestamp, String annotation) throws IOException;
	
	public abstract void writePointValueTime(DataValue value, long timestamp, String annotation) throws IOException;
	
	/**
	 * Write a Data Value that if null contains an annotation saying there is no data at this point in time
	 * - useful for Rollups with gaps in the data
	 * @param value
	 * @param time
	 * @throws ConversionException
	 * @throws IOException
	 */
	public void writeNonNullDouble(Double value, long time) throws ConversionException, IOException{
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
	
	/**
	 * Write a Data Value that if null contains an annotation saying there is no data at this point in time
	 * - useful for Rollups with gaps in the data
	 * @param value
	 * @param time
	 * @throws ConversionException
	 * @throws IOException
	 */
	public void writeNonNull(DataValue value, Long time) throws ConversionException, IOException{
		
		if(time == null)
			throw new ShouldNeverHappenException("Time cannot be null");
		
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
	
	public void writeNonNullIntegral(Double integral, long time) throws IOException{
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
	
	
}
