/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Write a row into a CSV based on settings and point value
 * 
 * @author Terry Packer
 *
 */
public class PointValueTimeCsvWriter extends PointValueTimeWriter {

    private final String headers[];
    protected CSVWriter writer;
    protected boolean wroteHeaders = false;
    protected boolean writeXid;
    
    /**
     * 
     * @param host
     * @param port
     * @param writer
     * @param useRendered
     * @param unitConversion
     * @param dateTimeFormat - format for string dates or null for epoch millis
     * @param timezone
     */
    public PointValueTimeCsvWriter(String host, int port, CSVWriter writer, boolean useRendered,
            boolean unitConversion, String dateTimeFormat, String timezone) {
        this(host, port, writer, useRendered, unitConversion, false, true, dateTimeFormat, timezone);
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
    public PointValueTimeCsvWriter(String host, int port, CSVWriter writer, boolean useRendered,
            boolean unitConversion, boolean writeXid, boolean writeHeaders, String dateTimeFormat, String timezone) {
        super(host, port, useRendered, unitConversion, dateTimeFormat, timezone);
        this.writeXid = writeXid;
        if (writeXid)
            headers = new String[] {"xid", "value", "timestamp", "annotation"};
        else
            headers = new String[] {"value", "timestamp", "annotation"};

        this.writer = writer;
        if (!writeHeaders)
            this.wroteHeaders = true;
    }

    public void writeHeaders() {
        this.writer.writeNext(headers);
        this.wroteHeaders = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(
     * double, long, java.lang.String)
     */
    @Override
    public void writePointValueTime(double value, long timestamp, String annotation, DataPointVO vo)
            throws IOException {
        writeLine(vo.getXid(), Double.toString(value), timestamp, annotation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(
     * int, long, java.lang.String)
     */
    @Override
    public void writePointValueTime(int value, long timestamp, String annotation, DataPointVO vo)
            throws IOException {
        writeLine(vo.getXid(), Integer.toString(value), timestamp, annotation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(
     * int, long, java.lang.String)
     */
    @Override
    public void writePointValueTime(String value, long timestamp, String annotation, DataPointVO vo)
            throws IOException {
        writeLine(vo.getXid(), value, timestamp, annotation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writePointValueTime(
     * com.serotonin.m2m2.rt.dataImage.types.DataValue, long, java.lang.String)
     */
    @Override
    public void writePointValueTime(DataValue value, long timestamp, String annotation,
            DataPointVO vo) throws IOException {
        if (!wroteHeaders)
            this.writeHeaders();

        if (value == null) {
            writeLine(vo.getXid(), "", timestamp, annotation);
        } else {
            switch (value.getDataType()) {
                case DataTypes.ALPHANUMERIC:
                    writeLine(vo.getXid(), value.getStringValue(), timestamp, annotation);
                    break;
                case DataTypes.BINARY:
                    writeLine(vo.getXid(), Boolean.toString(value.getBooleanValue()),
                            timestamp, annotation);
                    break;
                case DataTypes.MULTISTATE:
                    writeLine(vo.getXid(), Integer.toString(value.getIntegerValue()),
                            timestamp, annotation);
                    break;
                case DataTypes.NUMERIC:
                    writeLine(vo.getXid(), Double.toString(value.getDoubleValue()), timestamp,
                            annotation);
                    break;
                default:
                    writeLine(vo.getXid(), imageServletBuilder.buildAndExpand(timestamp, vo.getId())
                            .toUri().toString(), timestamp, annotation);
                    break;
            }
        }
    }

    /**
     * Helper to write a line
     * 
     * @param value
     * @param timestamp
     * @param annotation
     */
    protected void writeLine(String xid, String value, long timestamp, String annotation) {

        String timestampString;
        if (dateFormatter == null)
            timestampString = Long.toString(timestamp);
        else
            timestampString = writeTimestampString(timestamp);

        
        if (!wroteHeaders)
            this.writeHeaders();
        String[] nextLine;
        if (writeXid) {
            nextLine = new String[4];
            nextLine[0] = xid;
            nextLine[1] = value;
            nextLine[2] = timestampString;
            nextLine[3] = annotation;
        } else {
            nextLine = new String[3];
            nextLine[0] = value;
            nextLine[1] = timestampString;
            nextLine[2] = annotation;
        }
        this.writer.writeNext(nextLine);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeAllStatistics(
     * com.serotonin.m2m2.view.stats.ValueChangeCounter, com.serotonin.m2m2.vo.DataPointVO)
     */
    @Override
    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo)
            throws IOException {
        // TODO Implement somehow?
    }


}
