/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import javax.measure.unit.Unit;

import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
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
    
	
	public void writePointValueTime(DataPointVO vo, PointValueTime pvt) throws IOException {
	    writePointValueTime(vo, pvt, false, false);
	}
	
	/**
	 * General write of one point value time
	 * @param vo
	 * @param pvt
	 * @param bookend
	 * @param cached
	 * @throws IOException
	 */
    public abstract void writePointValueTime(DataPointVO vo, PointValueTime pvt, boolean bookend, boolean cached) throws IOException;
    
    /**
     * 
     * @param name
     * @param vo
     * @param value
     * @param timestamp - Only used for generating Image File Links
     * @throws IOException
     */
    protected void writeDataValue(String name, DataPointVO vo, DataValue value, long timestamp) throws IOException{
        if(value == null) {
            writeStringField(ANNOTATION, info.getNoDataMessage());
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
    
    public void writeTimestamp(long timestamp) throws IOException {
        if (info.getDateTimeFormatter() == null)
            writeLongField(TIMESTAMP, timestamp);
        else
            writeStringField(TIMESTAMP, info.getDateTimeString(timestamp));
    }
	
    public void writeIntegral(String name, DataPointVO vo, Double integral) throws IOException {
        if (integral == null) {
            writeStringField(ANNOTATION, info.getNoDataMessage());
            writeNullField(name);
        } else {
            writeDoubleField(name, integral);
            if(info.isUseRendered())
                writeStringField(RENDERED, info.getIntegralString(vo, integral));
        }
    }
    
    public void writeAnalogStatistic(String name, DataPointVO vo, Double value) throws IOException {
        if (value == null) {
            writeStringField(ANNOTATION, info.getNoDataMessage());
            writeNullField(name);
        } else {
            writeDoubleField(name, value);
            if(info.isUseRendered())
                writeStringField(RENDERED, info.getRenderedString(vo, value));
        }
    }
    
    public void writeAccumulator(String name, DataPointVO vo, AnalogStatistics stats) throws IOException {
        Double accumulatorValue = stats.getLastValue();
        if (accumulatorValue == null) {
            accumulatorValue = stats.getMaximumValue();
        }
        writeAnalogStatistic(name, vo, accumulatorValue);
    }

    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo)
            throws IOException {

        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
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
    
    public void writeStatistic(String name, StatisticsGenerator statisticsGenerator, DataPointVO vo) throws IOException{
        
        if(info.getRollup() == RollupEnum.ALL) {
            writeAllStatistics(statisticsGenerator, vo);
            return;
        }

        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
            switch(info.getRollup()){
                case START:
                    writeDataValue(name, vo, stats.getStartValue(), stats.getPeriodStartTime());
                break;
                case FIRST:
                    writeDataValue(name, vo, stats.getFirstValue(), stats.getFirstTime());
                break;
                case LAST:
                    writeDataValue(name, vo, stats.getLastValue(), stats.getLastTime());
                break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type" + info.getRollup());
            }
        }else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch(info.getRollup()){
                case AVERAGE:
                    writeAnalogStatistic(name, vo, stats.getAverage());
                break;
                case DELTA:
                    writeAnalogStatistic(name, vo, stats.getDelta());
                break;
                case MINIMUM:
                    writeAnalogStatistic(name, vo, stats.getMinimumValue());
                break;
                case MAXIMUM:
                    writeAnalogStatistic(name, vo, stats.getMaximumValue());
                break;
                case ACCUMULATOR:
                    writeAccumulator(name, vo, stats);
                break;
                case SUM:
                    writeAnalogStatistic(name, vo, stats.getSum());
                break;
                case START:
                    writeAnalogStatistic(name, vo, stats.getStartValue());
                break;
                case FIRST:
                    writeAnalogStatistic(name, vo, stats.getFirstValue());
                break;
                case LAST:
                    writeAnalogStatistic(name, vo, stats.getLastValue());
                break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                break;
                case INTEGRAL:
                    writeIntegral(name, vo, stats.getIntegral());
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
     * @param e
     */
    public void sendServerErrorResponse(IOException e) {
        // TODO Auto-generated method stub
        
    }
}
