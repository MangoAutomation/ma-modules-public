/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
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
    public void writePointValueTime(DataPointVO vo, PointValueTime pvt, boolean bookend, boolean cached) throws IOException {
        this.jgen.writeStartObject();
        
        if(info.isMultiplePointsPerArray()) {
            this.jgen.writeObjectFieldStart(vo.getXid());
            if(pvt == null) {
                writeStringField(ANNOTATION, info.noDataMessage);
            }else {
                writeTimestamp(pvt.getTime());
                if(pvt.isAnnotated())
                    writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                if(bookend)
                    writeBooleanField(BOOKEND, true);
                if(cached)
                    writeBooleanField(CACHED, true);
                writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                if(info.isUseRendered())
                    writeStringField(RENDERED, info.getRenderedString(vo, pvt));
            }
            this.jgen.writeEndObject();
        }else {
            if(pvt == null) {
                writeStringField(ANNOTATION, info.noDataMessage);
            }else {
                writeTimestamp(pvt.getTime());
                if(pvt.isAnnotated())
                    writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                if(bookend)
                    writeBooleanField(BOOKEND, true);
                if(cached)
                    writeBooleanField(CACHED, true);
                writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                if(info.isUseRendered())
                    writeStringField(RENDERED, info.getRenderedString(vo, pvt));
            }
        }
        this.jgen.writeEndObject();
    }
    
    /**
     * @param currentValues
     */
    @Override
    public void writeMultiplePointsAsObject(List<DataPointVOPointValueTimeBookend> currentValues)  throws IOException{
        if(currentValues.size() == 0)
            return;
        
        this.jgen.writeStartObject();
        writeTimestamp(currentValues.get(0).pvt.getTime());
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            PointValueTime pvt = value.pvt;
            DataPointVO vo = value.vo;
            boolean bookend = value.bookend; 
            boolean cached = value.cached;
            if(info.isMultiplePointsPerArray()) {
                this.jgen.writeObjectFieldStart(vo.getXid());
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.noDataMessage);
                }else {
                    if(pvt.isAnnotated())
                        writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                    if(bookend)
                        writeBooleanField(BOOKEND, true);
                    if(cached)
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
                this.jgen.writeEndObject();
            }else {
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.noDataMessage);
                }else {
                    if(bookend)
                        writeBooleanField(BOOKEND, true);
                    if(cached)
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
            }
        }
        this.jgen.writeEndObject();
    }
    
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeMultipleStatsAsObject(List<DataPointStatisticsGenerator> periodStats) throws IOException{
        if(periodStats.size() == 0)
            return;
        this.jgen.writeStartObject();
        writeTimestamp(periodStats.get(0).getGenerator().getPeriodStartTime());
        for(DataPointStatisticsGenerator gen : periodStats) {
            DataPointVO vo = gen.getVo();
            if(info.isMultiplePointsPerArray()) {
                this.jgen.writeObjectFieldStart(vo.getXid());
                writeStatistic(VALUE, gen.getGenerator(), gen.getVo());
                this.jgen.writeEndObject();
            }else {
                
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
        writeTimestamp(periodStats.getGenerator().getPeriodStartTime());
        DataPointVO vo = periodStats.getVo();
        if(info.isMultiplePointsPerArray()) {
            this.jgen.writeObjectFieldStart(vo.getXid());
            writeStatistic(VALUE, periodStats.getGenerator(), periodStats.getVo());
            this.jgen.writeEndObject();
        }else {
            writeStatistic(VALUE, periodStats.getGenerator(), periodStats.getVo());
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
