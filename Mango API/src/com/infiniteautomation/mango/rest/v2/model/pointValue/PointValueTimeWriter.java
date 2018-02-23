/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import javax.measure.unit.Unit;

import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * Base class for all classes to stream PointValueTime 
 * 
 * @author Terry Packer
 *
 */
public abstract class PointValueTimeWriter {

    public final static String TIMESTAMP = "timestamp";
    public final static String ROLLUP = "rollup";
    public final static String VALUE = "value";
    public final static String RENDERED = "rendered";
    public final static String ANNOTATION = "annotation";
    public final static String INTEGRAL = "integral";
    public final static String RENDERED_INTEGRAL = "renderedIntegral";
    public final static String BOOKEND = "bookend"; //Virtual point on the ends of the list
    public final static String CACHED = "cached";
	
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
    
	protected final LatestQueryInfo info;
	protected final Translations translations;
	
	public PointValueTimeWriter(LatestQueryInfo info){
	    this.info = info;
	    this.translations = Common.getTranslations();
	}
    
	/* Methods Used for Point Values */
	
	public abstract void writeStringField(String name, String value) throws IOException;
	public abstract void writeDoubleField(String name, Double value) throws IOException;
	public abstract void writeIntegerField(String name, Integer value) throws IOException;
	public abstract void writeLongField(String name, Long value) throws IOException;
	public abstract void writeBooleanField(String name, Boolean value) throws IOException;
	public abstract void writeNullField(String name) throws IOException;
    public abstract void writeStartArray() throws IOException;
	public abstract void writeStartArray(String name) throws IOException;
    public abstract void writeEndArray() throws IOException;
    public abstract void writeStartObject() throws IOException;
    public abstract void writeEndObject() throws IOException;

    /**
     * @param vo
     * @param value
     * @param b
     * @throws IOException 
     */
    protected void writeEntry(DataPointVOPointValueTimeBookend value, boolean useXid, boolean allowTimestamp) throws IOException {
        for(PointValueField field : info.getFields()) {
            if(!allowTimestamp && field == PointValueField.TIMESTAMP)
                continue;
            field.writeValue(value, info, translations, useXid, this);
        }
    }
    
    protected void writeEntry(DataPointStatisticsGenerator periodStats, boolean useXid, boolean allowTimestamp) throws IOException {
        for(PointValueField field : info.getFields()) {
            if(!allowTimestamp && field == PointValueField.TIMESTAMP)
                continue;
            field.writeValue(periodStats, info, translations, useXid, this);
        }
    }
    
    /**
     * 
     * @param name
     * @param vo
     * @param value
     * @param timestamp - Only used for generating Image File Links
     * @throws IOException
     */
    protected void writeDataValue(String name, DataPointVO vo, DataValue value, Long timestamp, boolean rendered) throws IOException{
        if(rendered) {
            writeStringField(name, info.getRenderedString(vo, value));
        }else {
            if(value == null) {
                writeNullField(name);
            }else
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
    }
    
    public void writeTimestamp(long timestamp) throws IOException {
        if (info.getDateTimeFormatter() == null)
            writeLongField(TIMESTAMP, timestamp);
        else
            writeStringField(TIMESTAMP, info.getDateTimeString(timestamp));
    }
	
    public void writeIntegral(String name, DataPointVO vo, Double integral, boolean rendered) throws IOException {
        if(rendered) {
            writeStringField(name, info.getIntegralString(vo, integral));
        }else {
            if (integral == null) {
                writeNullField(name);
            } else {
                writeDoubleField(name, integral);
            }
        }
    }
    
    public void writeAnalogStatistic(String name, DataPointVO vo, Double value, boolean rendered) throws IOException {
        if(rendered) {
            writeStringField(name, info.getRenderedString(vo, value));
        }else {
            if (value == null) {
                writeNullField(name);
            } else {
                writeDoubleField(name, value);
            }
        }
    }
    
    public void writeAccumulator(String name, DataPointVO vo, AnalogStatistics stats, boolean rendered) throws IOException {
        Double accumulatorValue = stats.getLastValue();
        if (accumulatorValue == null) {
            accumulatorValue = stats.getMaximumValue();
        }
        writeAnalogStatistic(name, vo, accumulatorValue, rendered);
    }

    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo, boolean rendered)
            throws IOException {

        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
            writeDataValue(RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
            writeDataValue(RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime(), rendered);
            writeDataValue(RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime(), rendered);
            writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
        } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList)statisticsGenerator;
            writeDataValue(RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
            writeDataValue(RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime(), rendered);
            writeDataValue(RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime(), rendered);
            writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            writeAccumulator(RollupEnum.ACCUMULATOR.name(), vo, stats, rendered);
            writeAnalogStatistic(RollupEnum.AVERAGE.name(), vo, stats.getAverage(), rendered);
            writeAnalogStatistic(RollupEnum.DELTA.name(), vo, stats.getDelta(), rendered);
            writeAnalogStatistic(RollupEnum.MINIMUM.name(), vo, stats.getMinimumValue(), rendered);
            writeAnalogStatistic(RollupEnum.MAXIMUM.name(), vo, stats.getMaximumValue(), rendered);
            writeAnalogStatistic(RollupEnum.SUM.name(), vo, stats.getSum(), rendered);
            writeAnalogStatistic(RollupEnum.START.name(), vo, stats.getStartValue(), rendered);
            writeAnalogStatistic(RollupEnum.FIRST.name(), vo, stats.getFirstValue(), rendered);
            writeAnalogStatistic(RollupEnum.LAST.name(), vo, stats.getLastValue(), rendered);
            writeIntegral(RollupEnum.INTEGRAL.name(), vo, stats.getIntegral(), rendered);
            writeIntegerField(RollupEnum.COUNT.name(), stats.getCount());
        }
    }
    
    public void writeStatistic(String name, StatisticsGenerator statisticsGenerator, DataPointVO vo, boolean rendered) throws IOException{
        
        if(info.getRollup() == RollupEnum.ALL) {
            writeAllStatistics(statisticsGenerator, vo, rendered);
            return;
        }

        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
            switch(info.getRollup()){
                case START:
                    writeDataValue(name, vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
                break;
                case FIRST:
                    writeDataValue(name, vo, stats.getFirstValue(), stats.getFirstTime() == null ? 0 : stats.getFirstTime(), rendered);
                break;
                case LAST:
                    writeDataValue(name, vo, stats.getLastValue(), stats.getLastTime() == null ? 0 : stats.getLastTime(), rendered);
                break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type" + info.getRollup());
            }
        } else if(statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList) statisticsGenerator;
            switch(info.getRollup()){
                case START:
                    writeDataValue(name, vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
                break;
                case FIRST:
                    writeDataValue(name, vo, stats.getFirstValue(), stats.getFirstTime() == null ? 0 : stats.getFirstTime(), rendered);
                break;
                case LAST:
                    writeDataValue(name, vo, stats.getLastValue(), stats.getLastTime() == null ? 0 : stats.getLastTime(), rendered);
                break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type" + info.getRollup());
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch(info.getRollup()){
                case AVERAGE:
                    writeAnalogStatistic(name, vo, stats.getAverage(), rendered);
                break;
                case DELTA:
                    writeAnalogStatistic(name, vo, stats.getDelta(), rendered);
                break;
                case MINIMUM:
                    writeAnalogStatistic(name, vo, stats.getMinimumValue(), rendered);
                break;
                case MAXIMUM:
                    writeAnalogStatistic(name, vo, stats.getMaximumValue(), rendered);
                break;
                case ACCUMULATOR:
                    writeAccumulator(name, vo, stats, rendered);
                break;
                case SUM:
                    writeAnalogStatistic(name, vo, stats.getSum(), rendered);
                break;
                case START:
                    writeAnalogStatistic(name, vo, stats.getStartValue(), rendered);
                break;
                case FIRST:
                    writeAnalogStatistic(name, vo, stats.getFirstValue(), rendered);
                break;
                case LAST:
                    writeAnalogStatistic(name, vo, stats.getLastValue(), rendered);
                break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                break;
                case INTEGRAL:
                    writeIntegral(name, vo, stats.getIntegral(), rendered);
                break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type" + info.getRollup());
            }
        }
    }

    public abstract void writeMultiplePointValuesAtSameTime(List<DataPointVOPointValueTimeBookend> currentValues, long timestamp) throws IOException;
    public abstract void writeMultiplePointStatsAtSameTime(List<DataPointStatisticsGenerator> periodStats, long timestamp) throws IOException;
    public abstract void writeStatsAsObject(DataPointStatisticsGenerator generator) throws IOException;
    /**
     * General write of one point value time
     * @throws IOException
     */
    public abstract void writePointValueTime(DataPointVOPointValueTimeBookend value) throws IOException;

}
