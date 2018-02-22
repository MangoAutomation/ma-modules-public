/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeJsonWriter extends PointValueTimeWriter {

    protected final JsonGenerator jgen;

    public PointValueTimeJsonWriter(LatestQueryInfo info, JsonGenerator jgen) {
        super(info);
        this.jgen = jgen;
    }
    
    @Override
    public void writePointValueTime(DataPointVOPointValueTimeBookend value) throws IOException {
        this.jgen.writeStartObject();
        if(info.isMultiplePointsPerArray()) {
            this.jgen.writeObjectFieldStart(value.getVo().getXid());
            writeEntry(value, false, true);
            this.jgen.writeEndObject();
        }else {
            writeEntry(value, false, true);
        }
        this.jgen.writeEndObject();
    }
    
    /**
     * @param currentValues
     */
    @Override
    public void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp)  throws IOException{

        this.jgen.writeStartObject();
        //If we have a timestamp write it here
        if(info.fieldsContains(PointValueField.TIMESTAMP))
            writeTimestamp(timestamp);
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            if(info.isMultiplePointsPerArray()) {
                this.jgen.writeObjectFieldStart(value.getVo().getXid());
                writeEntry(value, false, false);
                this.jgen.writeEndObject();
            }else {
                writeEntry(value, false, false);
            }
        }
        this.jgen.writeEndObject();
    }
    
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeMultiplePointStatsAtSameTime(List<DataPointStatisticsGenerator> periodStats, long timestamp) throws IOException{
        this.jgen.writeStartObject();

        if(info.fieldsContains(PointValueField.TIMESTAMP))
            writeTimestamp(timestamp);
        
        for(DataPointStatisticsGenerator gen : periodStats) {
            if(info.isMultiplePointsPerArray()) {
                DataPointVO vo = gen.getVo();
                this.jgen.writeObjectFieldStart(vo.getXid());
                writeEntry(gen, false, false);
                this.jgen.writeEndObject();
            }else {
                throw new ShouldNeverHappenException("Implement me?");
            }
        }
        this.jgen.writeEndObject();
    }
    
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeStatsAsObject(DataPointStatisticsGenerator periodStats) throws IOException{
        this.jgen.writeStartObject();
        if(info.isMultiplePointsPerArray()) {
            this.jgen.writeObjectFieldStart(periodStats.getVo().getXid());
            writeEntry(periodStats, false, true);
            this.jgen.writeEndObject();
        }else {
            writeEntry(periodStats, false, true);
        }
        this.jgen.writeEndObject();
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
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#startWriteArray()
     */
    @Override
    public void writeStartArray(String name) throws IOException {
        jgen.writeArrayFieldStart(name);
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#endWriteArray()
     */
    @Override
    public void writeEndArray() throws IOException {
        jgen.writeEndArray();
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeStartArray()
     */
    @Override
    public void writeStartArray() throws IOException {
        this.jgen.writeStartArray();
    }



    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeStartObject()
     */
    @Override
    public void writeStartObject() throws IOException {
        this.jgen.writeStartObject();
    }



    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeEndObject()
     */
    @Override
    public void writeEndObject() throws IOException {
        this.jgen.writeEndObject();       
    }
}
