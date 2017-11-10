/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.measure.converter.ConversionException;

import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public abstract class PointValueTimeWriter {

	protected boolean useRendered;
	protected boolean unitConversion;
	protected final String noDataMessage;
	protected UriComponentsBuilder imageServletBuilder;
	protected final DateTimeFormatter dateFormatter;  //Write a timestamp or string date
	protected final ZoneId zoneId;
	
	public PointValueTimeWriter(String host, int port, boolean useRendered, boolean unitConversion, String dateTimeFormat, String timezone){
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.noDataMessage = new TranslatableMessage("common.stats.noDataForPeriod").translate(Common.getTranslations());
		
		//If we are an image type we should build the URLS
		imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/hst{ts}_{id}.jpg");
		if(Common.envProps.getBoolean("ssl.on", false))
			imageServletBuilder.scheme("https");
		else
			imageServletBuilder.scheme("http");
		imageServletBuilder.host(host);
		imageServletBuilder.port(port);
		if(dateTimeFormat != null) {
		    this.dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
		    if(timezone == null)
		        this.zoneId = TimeZone.getDefault().toZoneId();
		    else
		        this.zoneId = ZoneId.of(timezone);
		}else {
            this.dateFormatter = null;
            this.zoneId = null;
		}
		
	}
	
	public abstract void writePointValueTime(double value, long timestamp, String annotation, DataPointVO vo) throws IOException;
	
	public abstract void writePointValueTime(int value, long timestamp, String annotation, DataPointVO vo) throws IOException;

	public abstract void writePointValueTime(String value, long timestamp, String annotation, DataPointVO vo) throws IOException;
	
	public abstract void writePointValueTime(DataValue value, long timestamp, String annotation, DataPointVO vo) throws IOException;
	
	/**
	 * Write a Data Value that if null contains an annotation saying there is no data at this point in time
	 * - useful for Rollups with gaps in the data
	 * @param value
	 * @param time
	 * @throws ConversionException
	 * @throws IOException
	 */
	public void writeNonNullDouble(Double value, long time, DataPointVO vo) throws ConversionException, IOException{
		if(value == null){
			if(useRendered){
				this.writePointValueTime(new AlphanumericValue(""), time, this.noDataMessage, vo);
			}else{
				this.writePointValueTime(0.0D, time, this.noDataMessage, vo);
			}
		}else{
	    	if(useRendered){
	    		//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, new PointValueTime(value, time));
				this.writePointValueTime(new AlphanumericValue(textValue), time, null, vo);
			}else if(unitConversion){
				//Convert Value, must be numeric
				this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value), time, null, vo);
			}else{
				this.writePointValueTime(value, time, null, vo);

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
	public void writeNonNull(DataValue value, Long time, DataPointVO vo) throws ConversionException, IOException{
		
		if(time == null)
			throw new ShouldNeverHappenException("Time cannot be null");
		
		if(value == null){
			if(useRendered){
				this.writePointValueTime(new AlphanumericValue(""), time, this.noDataMessage, vo);
			}else{
				this.writePointValueTime(0.0D, time, this.noDataMessage, vo);
			}
		}else{
	    	if(useRendered){
	    		//Convert to Alphanumeric Value
				String textValue = Functions.getRenderedText(vo, new PointValueTime(value, time));
				this.writePointValueTime(new AlphanumericValue(textValue), time, null, vo);
			}else if(unitConversion){
				//Convert Value, must be numeric
				if (value instanceof NumericValue)
					this.writePointValueTime(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()), time, null,vo );
				else
					this.writePointValueTime(value, time, null, vo);
			}else{
				this.writePointValueTime(value, time, null, vo);
			}
		}
	}
	
	/**
	 * Write an image using its timestamp
	 * @param value
	 * @param imageTimestamp
	 * @param timestamp
	 * @param vo
	 * @throws IOException
	 */
	public void writeNonNullImage(DataValue value, Long imageTimestamp, Long timestamp, DataPointVO vo) throws IOException{
		if(timestamp == null)
			throw new ShouldNeverHappenException("Time cannot be null");
		
		if(value == null){
			if(useRendered){
				this.writePointValueTime(new AlphanumericValue(""), timestamp, this.noDataMessage, vo);
			}else{
				this.writePointValueTime(0.0D, timestamp, this.noDataMessage, vo);
			}
		}else{
			this.writePointValueTime(value, imageTimestamp, null, vo);
		}

	}
	
	public void writeNonNullIntegral(Double integral, long time, DataPointVO vo) throws IOException{
    	if(useRendered){
    		//Convert to Alphanumeric Value
    		if(integral != null){
    			String textValue = Functions.getIntegralText(vo, integral);
    			this.writePointValueTime(new AlphanumericValue(textValue), time, null, vo);
    		}else{
    			this.writePointValueTime(new AlphanumericValue(""), time, null, vo);
    		}
		}else{ //No conversion possible
			if(integral == null)
				this.writePointValueTime(0.0D, time, this.noDataMessage, vo);  
			else
				this.writePointValueTime(integral, time, null, vo);  
		}
	}

	/**
	 * Write out all statistics into one entry
	 * 
	 * @param statisticsGenerator
	 * @param vo
	 */
	public abstract void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo) throws IOException;
	
	public String writeTimestampString(long timestamp) {
	    return dateFormatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId));
	}
	
}
