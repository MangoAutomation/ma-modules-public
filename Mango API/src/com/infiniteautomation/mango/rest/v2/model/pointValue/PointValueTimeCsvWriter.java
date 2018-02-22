/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import javax.measure.unit.Unit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeCsvWriter extends PointValueTimeJsonWriter{

    public static final String DOT = ".";
    public static final String DOT_RENDERED = DOT + "rendered";
    public static final String DOT_BOOKEND = DOT + "bookend";
    public static final String DOT_CACHED = DOT + "cached";
    public static final String NAME = "name";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DATA_SOURCE_NAME = "dataSourceName";
    
    public static final String DOT_ANNOTATION = DOT + ANNOTATION;
    public static final String DOT_NAME = DOT + NAME;
    public static final String DOT_DEVICE_NAME = DOT + DEVICE_NAME;
    public static final String DOT_DATA_SOURCE_NAME = DOT + DATA_SOURCE_NAME;
    public static final String DOT_VALUE = DOT + VALUE;
    
    public static final String XID = "xid";
    
    protected final int pointCount;
    /**
     * @param info
     * @param jgen
     */
    public PointValueTimeCsvWriter(LatestQueryInfo info, int pointCount, JsonGenerator jgen) {
        super(info, jgen);
        this.pointCount = pointCount;
    }

    @Override
    public void writePointValueTime(DataPointVOPointValueTimeBookend value) throws IOException {
        this.jgen.writeStartObject();
        DataPointVO vo = value.getVo();
        PointValueTime pvt = value.getPvt();
        if(info.isMultiplePointsPerArray()) {
            writeStringField(XID, vo.getXid());
            if(pvt == null) {
                writeStringField(ANNOTATION, info.getNoDataMessage());
            }else {
                writeTimestamp(pvt.getTime());
                if(pvt.isAnnotated())
                    writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                if(value.isBookend())
                    writeBooleanField(BOOKEND, true);
                if(value.isCached())
                    writeBooleanField(CACHED, true);
                writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                if(info.isUseRendered())
                    writeStringField(RENDERED, info.getRenderedString(vo, pvt));
            }
        }else {
            if(info.isSingleArray()) {
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.getNoDataMessage());
                }else {
                    writeTimestamp(pvt.getTime());
                    if(pvt.isAnnotated())
                        writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                    if(value.isBookend())
                        writeBooleanField(BOOKEND, true);
                    if(value.isCached())
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
            }else {
                //Use XIDs
                writeStringField(XID, vo.getXid());
                if(pvt == null) {
                    writeStringField(ANNOTATION, info.getNoDataMessage());
                }else {
                    writeTimestamp(pvt.getTime());
                    if(pvt.isAnnotated())
                        writeStringField(ANNOTATION, ((AnnotatedIdPointValueTime) pvt).getAnnotation(translations));
                    if(value.isBookend())
                        writeBooleanField(BOOKEND, true);
                    if(value.isCached())
                        writeBooleanField(CACHED, true);
                    writeDataValue(VALUE, vo, pvt.getValue(), pvt.getTime());
                    if(info.isUseRendered())
                        writeStringField(RENDERED, info.getRenderedString(vo, pvt));
                }
            }
        }
        writeDataPointInfoColumns(value.getVo(), false);
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
            writeStringField(XID, vo.getXid());
            writeStatistic(vo.getXid() + DOT_VALUE, periodStats.getGenerator(), periodStats.getVo());
            writeDataPointInfoColumns(periodStats.getVo(), true);
        }else {
            if(!info.isSingleArray())
                writeStringField(XID, vo.getXid());
            writeStatistic(VALUE, periodStats.getGenerator(), periodStats.getVo());
            writeDataPointInfoColumns(periodStats.getVo(), false);
        }
        this.writeEndObject();
    }
    
    @Override
    public void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp)  throws IOException{
        this.jgen.writeStartObject();
        writeTimestamp(timestamp);
        for(DataPointVOPointValueTimeBookend value : currentValues) {
            String xid = value.getVo().getXid();
            if(info.isMultiplePointsPerArray()) {
                //XID columns
                writeDataValue(xid + DOT_VALUE, value.getVo(), value.getPvt().getValue(), value.getPvt().getTime());
                writeDataPointInfoColumns(value.getVo(), true);
                if(value.isBookend())
                    writeBooleanField(xid + DOT_BOOKEND, value.isBookend());
                if(value.isCached())
                    writeBooleanField(xid + DOT_CACHED, value.isCached());
                if(info.isUseRendered())
                    writeStringField(xid + DOT_RENDERED, info.getRenderedString(value.getVo(), value.getPvt()));
            }else {
                throw new ShouldNeverHappenException("Should not write multiple points here.");
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
                writeStatistic(vo.getXid() + DOT_VALUE, gen.getGenerator(), gen.getVo());
                writeDataPointInfoColumns(vo, true);
            }else {
                throw new ShouldNeverHappenException("Implement me?");
            }
        }
        this.jgen.writeEndObject();
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter#writeDataValue(java.lang.String, com.serotonin.m2m2.vo.DataPointVO, com.serotonin.m2m2.rt.dataImage.types.DataValue, long)
     */
    @Override
    protected void writeDataValue(String name, DataPointVO vo, DataValue value, long timestamp) throws IOException{
        if(value == null) {
            writeNullField(name);
        }else {
            switch(value.getDataType()) {
                case DataTypes.ALPHANUMERIC:
                    writeStringField(name, value.getStringValue());
                    break;
                case DataTypes.BINARY:
                    writeBooleanField(name, value.getBooleanValue());
                    break;
                case DataTypes.MULTISTATE:
                    writeIntegerField(name, value.getIntegerValue());
                    break;
                case DataTypes.NUMERIC:
                    if(vo.getRenderedUnit() != Unit.ONE)
                        writeDoubleField(name, vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()));
                    else
                        writeDoubleField(name, value.getDoubleValue());
                    break;
                case DataTypes.IMAGE:
                    writeStringField(name, info.writeImageLink(timestamp, vo.getId()));
                    break;
            }
        }
        if(info.isUseRendered()) {
            if(info.isSingleArray() && pointCount > 1)
                writeStringField(vo.getXid() + DOT_RENDERED, info.getRenderedString(vo, value));
            else
                writeStringField(RENDERED, info.getRenderedString(vo, value));
        }
        writeDataPointInfoColumns(vo, info.isSingleArray() && pointCount > 1);
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
            if(info.isUseRendered()) {
                if(info.isSingleArray() && pointCount > 1)
                    writeStringField(vo.getXid() + DOT_RENDERED, info.getIntegralString(vo, integral));
                else
                    writeStringField(RENDERED, info.getIntegralString(vo, integral));
            }
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
            if(info.isUseRendered()) {
                if(info.isSingleArray() && pointCount > 1)
                    writeStringField(vo.getXid() + DOT_RENDERED, info.getRenderedString(vo, value));
                else
                    writeStringField(RENDERED, info.getRenderedString(vo, value));
            }
        }
    }
    
    /**
     * Write name, deviceName, dataSourceName columns
     * @param vo
     * @param useXid
     * @throws IOException 
     */
    protected void writeDataPointInfoColumns(DataPointVO vo, boolean useXid) throws IOException {
        if(useXid) {
            for(DataPointField field : info.getExtraFields()) {
                writeStringField(vo.getXid() + DOT + field.getFieldName(), field.getFieldValue(vo));
            }
        }else {
            for(DataPointField field : info.getExtraFields()) {
                writeStringField(field.getFieldName(), field.getFieldValue(vo));
            }
        }
    }
    
    @Override
    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo)
            throws IOException {

        if(info.isSingleArray() && pointCount > 1) {
            if (statisticsGenerator instanceof ValueChangeCounter) {
                //We only need the timestamp here for image links
                ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
                writeDataValue(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime());
                writeDataValue(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime());
                writeDataValue(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime());
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
                StartsAndRuntimeList stats = (StartsAndRuntimeList)statisticsGenerator;
                writeDataValue(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime());
                writeDataValue(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime());
                writeDataValue(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime());
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof AnalogStatistics) {
                AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
                writeAccumulator(vo.getXid() + DOT + RollupEnum.ACCUMULATOR.name(), vo, stats);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.AVERAGE.name(), vo, stats.getAverage());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.DELTA.name(), vo, stats.getDelta());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.MINIMUM.name(), vo, stats.getMinimumValue());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.MAXIMUM.name(), vo, stats.getMaximumValue());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.SUM.name(), vo, stats.getSum());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue());
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue());
                writeIntegral(vo.getXid() + DOT + RollupEnum.INTEGRAL.name(), vo, stats.getIntegral());
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            }
        }else {
            if (statisticsGenerator instanceof ValueChangeCounter) {
                //We only need the timestamp here for image links
                ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
                writeDataValue(RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime());
                writeDataValue(RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime());
                writeDataValue(RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime());
                writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
                StartsAndRuntimeList stats = (StartsAndRuntimeList)statisticsGenerator;
                writeDataValue(RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime());
                writeDataValue(RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime());
                writeDataValue(RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime());
                writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof AnalogStatistics) {
                AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
                writeAccumulator(RollupEnum.ACCUMULATOR.name(), vo, stats);
                writeAnalogStatistic(RollupEnum.AVERAGE.name(), vo, stats.getAverage());
                writeAnalogStatistic(RollupEnum.DELTA.name(), vo, stats.getDelta());
                writeAnalogStatistic(RollupEnum.MINIMUM.name(), vo, stats.getMinimumValue());
                writeAnalogStatistic(RollupEnum.MAXIMUM.name(), vo, stats.getMaximumValue());
                writeAnalogStatistic(RollupEnum.SUM.name(), vo, stats.getSum());
                writeAnalogStatistic(RollupEnum.START.name(), vo, stats.getStartValue());
                writeAnalogStatistic(RollupEnum.FIRST.name(), vo, stats.getFirstValue());
                writeAnalogStatistic(RollupEnum.LAST.name(), vo, stats.getLastValue());
                writeIntegral(RollupEnum.INTEGRAL.name(), vo, stats.getIntegral());
                writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
            }
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
