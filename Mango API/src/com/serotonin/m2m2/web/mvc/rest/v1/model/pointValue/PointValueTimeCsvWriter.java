/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeCsvWriter extends PointValueTimeWriter{

	private final Log LOG = LogFactory.getLog(PointValueTimeCsvWriter.class);
	
	private final String headers[] = {"value", "timestamp", "annotation"};
	protected CSVWriter writer;
	protected boolean wroteHeaders = false;
	
	/**
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 */
	public PointValueTimeCsvWriter(CSVWriter writer, DataPointVO vo, boolean useRendered,
			boolean unitConversion) {
		super(vo, useRendered, unitConversion);
		this.writer = writer;
	}
	
	public void writeHeaders(){
		this.writer.writeNext(headers);
		this.wroteHeaders = true;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(double, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(double value, long timestamp,
			String annotation) throws IOException {
		if(!wroteHeaders)
			this.writeHeaders();
		String[] nextLine = new String[3];
		nextLine[0] = Double.toString(value);
		nextLine[1] = Long.toString(timestamp);
		nextLine[2] = annotation;
		this.writer.writeNext(nextLine);
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(int, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(int value, long timestamp,
			String annotation) throws IOException {
		if(!wroteHeaders)
			this.writeHeaders();
		String[] nextLine = new String[3];
		nextLine[0] = Integer.toString(value);
		nextLine[1] = Long.toString(timestamp);
		nextLine[2] = annotation;
		this.writer.writeNext(nextLine);
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(int, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(String value, long timestamp,
			String annotation) throws IOException {
		if(!wroteHeaders)
			this.writeHeaders();
		String[] nextLine = new String[3];
		nextLine[0] = value;
		nextLine[1] = Long.toString(timestamp);
		nextLine[2] = annotation;
		this.writer.writeNext(nextLine);
		
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(com.serotonin.m2m2.rt.dataImage.types.DataValue, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(DataValue value, long timestamp,
			String annotation) throws IOException {
		if(!wroteHeaders)
			this.writeHeaders();
		
		String[] nextLine = new String[3];
		
		if(value == null){
			nextLine[0] = "";
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					nextLine[0] = value.getStringValue();
				break;
				case DataTypes.BINARY:
					nextLine[0] = Boolean.toString(value.getBooleanValue());
				break;
				case DataTypes.MULTISTATE:
					nextLine[0] = Integer.toString(value.getIntegerValue());
				break;
				case DataTypes.NUMERIC:
					nextLine[0] = Double.toString(value.getDoubleValue());
				break;
				default:
					nextLine[0] = "unsupported-value-type";
					LOG.error("Unsupported data type for Point Value Time: " + value.getDataType());
				break;
			}
		}
		nextLine[1] = Long.toString(timestamp);
		nextLine[2] = annotation;
		this.writer.writeNext(nextLine);
	}

}
