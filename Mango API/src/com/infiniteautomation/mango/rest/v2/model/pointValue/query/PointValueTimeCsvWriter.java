/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;

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
    protected final Map<String, String> line;
    
    public PointValueTimeCsvWriter(ZonedDateTimeRangeQueryInfo info, CSVWriter writer) {
        this(info, writer, true);
    }

    public PointValueTimeCsvWriter(ZonedDateTimeRangeQueryInfo info, CSVWriter writer, boolean writeHeaders) {
        super(info);

        if (info.isUseXidAsFieldName())
            headers = new String[] {"xid", "value", "timestamp", "annotation"};
        else
            headers = new String[] {"value", "timestamp", "annotation"};

        this.writer = writer;
        if (!writeHeaders)
            this.wroteHeaders = true;
        
        //To retain ordering
        this.line = new LinkedHashMap<>();
    }

    public void writeHeaders() {
        this.writer.writeNext(headers);
        this.wroteHeaders = true;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#startWritePointValueTime()
     */
    @Override
    public void startWritePointValueTime() throws IOException {
        if (!wroteHeaders)
            this.writeHeaders();
        this.line.clear();
        //Populate the map
        for(String key : headers) {
            line.put(key, null);
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#endWritePointValueTime()
     */
    @Override
    public void endWritePointValueTime() throws IOException {
        String [] nextLine = new String[this.line.size()];
        Iterator<String> it = this.line.keySet().iterator();
        int i=0;
        while(it.hasNext()) {
            nextLine[i] = this.line.get(it.next());
            i++;
        }
        this.writer.writeNext(nextLine);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeStringField(java.lang.String, java.lang.String)
     */
    @Override
    public void writeStringField(String name, String value) throws IOException {
        this.line.put(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeDoubleField(java.lang.String, java.lang.Double)
     */
    @Override
    public void writeDoubleField(String name, Double value) throws IOException {
        this.line.put(name, value == null ? null : Double.toString(value));
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeIntegerField(java.lang.String, java.lang.Integer)
     */
    @Override
    public void writeIntegerField(String name, Integer value) throws IOException {
        this.line.put(name, value == null ? null : Integer.toString(value));
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeLongField(java.lang.String, java.lang.Long)
     */
    @Override
    public void writeLongField(String name, Long value) throws IOException {
        this.line.put(name, value == null ? null : Long.toString(value));
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeBooleanField(java.lang.String, java.lang.Boolean)
     */
    @Override
    public void writeBooleanField(String name, Boolean value) throws IOException {
        this.line.put(name, value == null ? null : Boolean.toString(value));
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeNullField(java.lang.String)
     */
    @Override
    public void writeNullField(String name) throws IOException {
        this.line.put(name, null);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#startWriteStatistics()
     */
    @Override
    public void startWriteStatistics() throws IOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#endWriteStatistics()
     */
    @Override
    public void endWriteStatistics() throws IOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#startWriteArray()
     */
    @Override
    public void startWriteArray(String name) throws IOException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#endWriteArray()
     */
    @Override
    public void endWriteArray() throws IOException {
        // TODO Auto-generated method stub
        
    }


}
