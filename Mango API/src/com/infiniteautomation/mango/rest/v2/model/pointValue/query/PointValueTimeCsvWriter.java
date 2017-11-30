/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeCsvWriter extends PointValueTimeJsonWriter{

    /**
     * @param info
     * @param jgen
     */
    public PointValueTimeCsvWriter(LatestQueryInfo info, JsonGenerator jgen) {
        super(info, jgen);
    }

    @Override
    public void writePointValueTime(DataPointVO vo, PointValueTime pvt, boolean bookend, boolean cached) throws IOException {
        this.jgen.writeStartObject();
        
        if(info.isMultiplePointsPerArray()) {
            writeStringField("xid", vo.getXid());
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
        }else {
            if(info.isSingleArray()) {
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
            }else {
                //Use XIDs
                writeStringField("xid", vo.getXid());
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
        }
        this.jgen.writeEndObject();
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeStatsAsObject(DataPointStatisticsGenerator periodStats) throws IOException{
        this.writeStartObject();
        writeTimestamp(periodStats.getGenerator().getPeriodStartTime());
        DataPointVO vo = periodStats.getVo();
        if(info.isMultiplePointsPerArray()) {
            writeStringField("xid", vo.getXid());
            writeStatistic(vo.getXid() + "." + info.getRollup().name(), periodStats.getGenerator(), periodStats.getVo());
        }else {
            if(!info.isSingleArray())
                writeStringField("xid", vo.getXid());
            writeStatistic(info.getRollup().name(), periodStats.getGenerator(), periodStats.getVo());
                
        }
        this.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp)  throws IOException{
        this.jgen.writeStartObject();
        writeTimestamp(timestamp);
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            String xid = value.vo.getXid();
            if(info.isMultiplePointsPerArray()) {
                //XID columns
                writeDataValue(xid, value.vo, value.pvt.getValue(), value.pvt.getTime());
                if(value.bookend)
                    writeBooleanField(xid + ".bookend", value.bookend);
                if(value.cached)
                    writeBooleanField(xid + ".cached", value.cached);
                if(info.isUseRendered())
                    writeStringField(xid + ".rendered", info.getRenderedString(value.vo, value.pvt));
            }else {
                
            }
        
        }
        this.jgen.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointStatsAtSameTime(List<DataPointStatisticsGenerator> periodStats, long timestamp) throws IOException{
        this.jgen.writeStartObject();
        writeTimestamp(timestamp);
        for(DataPointStatisticsGenerator gen : periodStats) {
            DataPointVO vo = gen.getVo();
            if(info.isMultiplePointsPerArray()) {
                writeStatistic(vo.getXid() + "." + info.getRollup(), gen.getGenerator(), gen.getVo());
            }else {
                throw new ShouldNeverHappenException("Implement me?");
            }
        }
        this.jgen.writeEndObject();
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeIntegral(java.lang.String, com.serotonin.m2m2.vo.DataPointVO, java.lang.Double)
     */
    @Override
    public void writeIntegral(String name, DataPointVO vo, Double integral) throws IOException {
        if (integral == null) {
            writeNullField(name);
        } else {
            writeDoubleField(name, integral);
            if(info.isUseRendered())
                writeStringField(vo.getXid() + ".rendered", info.getIntegralString(vo, integral));
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeAnalogStatistic(java.lang.String, com.serotonin.m2m2.vo.DataPointVO, java.lang.Double)
     */
    @Override
    public void writeAnalogStatistic(String name, DataPointVO vo, Double value) throws IOException {
        if (value == null) {
            writeNullField(name);
        } else {
            writeDoubleField(name, value);
            if(info.isUseRendered())
                writeStringField(vo.getXid() + ".rendered", info.getRenderedString(vo, value));
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#startWriteArray()
     */
    @Override
    public void writeStartArray(String name) throws IOException {
        jgen.writeStartArray();
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
