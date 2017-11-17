/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonWriter extends PointValueTimeWriter {

    protected final JsonGenerator jgen;

    public PointValueTimeJsonWriter(ZonedDateTimeRangeQueryInfo info, JsonGenerator jgen) {
        super(info);
        this.jgen = jgen;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#startWritePointValueTime()
     */
    @Override
    public void startWritePointValueTime() throws IOException {
        jgen.writeStartObject();
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#endWritePointValueTime()
     */
    @Override
    public void endWritePointValueTime() throws IOException {
        jgen.writeEndObject();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeStringField(java.lang.String, java.lang.String)
     */
    @Override
    public void writeStringField(String name, String value) throws IOException {
        this.jgen.writeStringField(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeDoubleField(java.lang.String, java.lang.Double)
     */
    @Override
    public void writeDoubleField(String name, Double value) throws IOException {
        this.jgen.writeNumberField(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeIntegerField(java.lang.String, java.lang.Integer)
     */
    @Override
    public void writeIntegerField(String name, Integer value) throws IOException {
        this.jgen.writeNumberField(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeLongField(java.lang.String, java.lang.Long)
     */
    @Override
    public void writeLongField(String name, Long value) throws IOException {
        this.jgen.writeNumberField(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeBooleanField(java.lang.String, java.lang.Boolean)
     */
    @Override
    public void writeBooleanField(String name, Boolean value) throws IOException {
        this.jgen.writeBooleanField(name, value);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#writeNullField(java.lang.String)
     */
    @Override
    public void writeNullField(String name) throws IOException {
        this.jgen.writeNullField(name);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#startWriteStatistics()
     */
    @Override
    public void startWriteStatistics() throws IOException {
        jgen.writeStartObject();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter#endWriteStatistics()
     */
    @Override
    public void endWriteStatistics() throws IOException {
        jgen.writeEndObject();
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#startWriteArray()
     */
    @Override
    public void startWriteArray(String name) throws IOException {
        jgen.writeArrayFieldStart(name);
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#endWriteArray()
     */
    @Override
    public void endWriteArray() throws IOException {
        jgen.writeEndArray();
    }
}
