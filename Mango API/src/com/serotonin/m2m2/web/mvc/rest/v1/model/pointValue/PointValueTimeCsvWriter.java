/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Write a row into a CSV based on settings and point value
 * 
 * @author Terry Packer
 *
 */
public class PointValueTimeCsvWriter extends PointValueTimeWriter{
	
	private final String headers[];
	protected CSVWriter writer;
	protected boolean wroteHeaders = false;
	protected DataPointVO vo;
	protected boolean writeXid;
	

	/**
	 * 
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 */
	public PointValueTimeCsvWriter(String host, int port, CSVWriter writer, DataPointVO vo, boolean useRendered,
			boolean unitConversion) {
		this(host, port, writer, vo, useRendered, unitConversion, false, true);
	}
	
	/**
	 * 
	 * @param writer
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param writeXid
	 * @param writeHeaders
	 */
	public PointValueTimeCsvWriter(String host, int port, CSVWriter writer, DataPointVO vo, boolean useRendered,
			boolean unitConversion, boolean writeXid, boolean writeHeaders) {
		super(host, port, useRendered, unitConversion);
		this.writeXid = writeXid;
		if(writeXid)
			headers = new String[]{"xid", "value", "timestamp", "annotation"};
		else
			headers = new String[]{"value", "timestamp", "annotation"};
		
		this.vo = vo;
		this.writer = writer;
		if(!writeHeaders)
			this.wroteHeaders = true;
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
		writeLine(Double.toString(value), timestamp, annotation);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(int, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(int value, long timestamp,
			String annotation) throws IOException {
		writeLine(Integer.toString(value), timestamp, annotation);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(int, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(String value, long timestamp,
			String annotation) throws IOException {
		writeLine(value, timestamp, annotation);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(com.serotonin.m2m2.rt.dataImage.types.DataValue, long, java.lang.String)
	 */
	@Override
	public void writePointValueTime(DataValue value, long timestamp,
			String annotation, DataPointVO vo) throws IOException {
		if(!wroteHeaders)
			this.writeHeaders();
		
		if(value == null){
			writeLine("", timestamp, annotation);
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					writeLine(value.getStringValue(), timestamp, annotation);
				break;
				case DataTypes.BINARY:
					writeLine(Boolean.toString(value.getBooleanValue()), timestamp, annotation);
				break;
				case DataTypes.MULTISTATE:
					writeLine(Integer.toString(value.getIntegerValue()), timestamp, annotation);
				break;
				case DataTypes.NUMERIC:
					writeLine(Double.toString(value.getDoubleValue()), timestamp, annotation);
				break;
				default:
					writeLine(imageServletBuilder.buildAndExpand(timestamp, vo.getId()).toUri().toString(), timestamp, annotation);
				break;
			}
		}
	}

	/**
	 * Helper to write a line
	 * @param value
	 * @param timestamp
	 * @param annotation
	 */
	protected void writeLine(String value, long timestamp, String annotation){
		
		if(!wroteHeaders)
			this.writeHeaders();
		String[] nextLine;
		if(writeXid){
			nextLine = new String[4];
			nextLine[0] = this.vo.getXid();
			nextLine[1] = value;
			nextLine[2] = Long.toString(timestamp);
			nextLine[3] = annotation;
		}else{
			nextLine = new String[3];
			nextLine[0] = value;
			nextLine[1] = Long.toString(timestamp);
			nextLine[2] = annotation;
		}
		this.writer.writeNext(nextLine);
	}
	
	
}
