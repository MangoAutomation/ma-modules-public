/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.serotonin.ShouldNeverHappenException;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeCsvWriter extends PointValueTimeJsonWriter{
    
    protected final int pointCount;
    /**
     * @param info
     * @param jgen
     */
    public PointValueTimeCsvWriter(LatestQueryInfo info, int pointCount, JsonGenerator jgen) {
        super(info, jgen);
        this.pointCount = pointCount;
    }

//    @Override
//    public void writePointValueTime(DataPointVOPointValueTimeBookend value) throws IOException {
//        this.jgen.writeStartObject();
//        writeEntry(value, false);
//        this.jgen.writeEndObject();
//    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter#writeMultipleStatsAsObject(java.util.List)
     */
    @Override
    public void writeStatsAsObject(DataPointStatisticsGenerator periodStats) throws IOException{
        this.writeStartObject();
        if(info.isMultiplePointsPerArray()) {
            writeEntry(periodStats, true, true);
        }else {
            writeEntry(periodStats, false, true);
        }
        this.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp)  throws IOException{
        this.jgen.writeStartObject();
        boolean first = true;
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            if(info.isMultiplePointsPerArray()) {
                writeEntry(value, true, first);
            }else {
                throw new ShouldNeverHappenException("Should not write multiple points here.");
            }
            first = false;
        }
        this.jgen.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointStatsAtSameTime(List<DataPointStatisticsGenerator> periodStats, long timestamp) throws IOException{
        this.jgen.writeStartObject();
        boolean first = true;
        for(DataPointStatisticsGenerator gen : periodStats) {
            if(info.isMultiplePointsPerArray()) {
                writeEntry(gen, true, first);
            }else {
                throw new ShouldNeverHappenException("Implement me?");
            }
            first = false;
        }
        this.jgen.writeEndObject();
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
