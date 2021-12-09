/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.io.IOException;
import java.util.List;

import javax.measure.unit.Unit;

import com.infiniteautomation.mango.rest.latest.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.infiniteautomation.mango.statistics.StartsAndRuntime;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.IValueTime;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

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

    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String START = "start";
    public static final String COUNT = "count";

    public static final String ACCUMULATOR = "accumulator";
    public static final String DELTA = "delta";
    public static final String AVERAGE = "average";
    public static final String MAXIMUM = "maximum";
    public static final String MINIMUM = "minimum";
    public static final String SUM = "sum";


    public static final String STARTS = "starts";
    public static final String RUNTIME = "runtime";
    public static final String PROPORTION = "proportion";
    public static final String STARTS_AND_RUNTIMES = "startsAndRuntimes";

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
    public abstract void writeStartObject(String name) throws IOException;
    public abstract void writeStartObject() throws IOException;
    public abstract void writeEndObject() throws IOException;

    /* Full Value Write Methods */
    /**
     * Write many values at the same time
     */
    public abstract void writeDataPointValues(List<DataPointValueTime> currentValues, long timestamp) throws IOException;

    /**
     * Write a single value
     */
    public abstract void writeDataPointValue(DataPointValueTime value) throws IOException;

    /**
     * @param timestamp - Only used for generating Image File Links
     * @param raw - do not use the rendered unit for conversion
     */
    protected void writeDataValue(String name, DataPointVO vo, DataValue value, Long timestamp, boolean rendered, boolean raw) throws IOException{
        if(rendered) {
            writeStringField(name, info.getRenderedString(vo, value));
        }else {
            if(value == null) {
                writeNullField(name);
            }else
                switch(value.getDataType()) {
                    case ALPHANUMERIC:
                        writeStringField(name, value.getStringValue());
                        break;
                    case BINARY:
                        writeBooleanField(name, value.getBooleanValue());
                        break;
                    case MULTISTATE:
                        writeIntegerField(name, value.getIntegerValue());
                        break;
                    case NUMERIC:
                        if(vo.getRenderedUnit() != Unit.ONE && !raw)
                            writeDoubleField(name, vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()));
                        else
                            writeDoubleField(name, value.getDoubleValue());
                        break;
                }
        }
    }

    public void writeTimestamp(Long timestamp) throws IOException {
        if(timestamp == null)
            writeNullField(TIMESTAMP);
        else
            writeTimestamp((long)timestamp);
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

    public void writeAnalogStatistic(String name, DataPointVO vo, Double value, boolean rendered, boolean raw) throws IOException {
        if(rendered) {
            writeStringField(name, info.getRenderedString(vo, value));
        }else {
            if (value == null) {
                writeNullField(name);
            } else {
                if(vo.getRenderedUnit() != Unit.ONE && !raw)
                    writeDoubleField(name, vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value));
                else
                    writeDoubleField(name, value);
            }
        }
    }

    public void writeAccumulator(String name, DataPointVO vo, AnalogStatistics stats, boolean rendered, boolean raw) throws IOException {
        Double accumulatorValue = stats.getLastValue();
        if (accumulatorValue == null) {
            accumulatorValue = stats.getMaximumValue();
        }
        writeAnalogStatistic(name, vo, accumulatorValue, rendered, raw);
    }

    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo, boolean rendered, boolean raw)
            throws IOException {

        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
            if(stats.getStartValue() != null) {
                //TODO There is a known bug here where images returned with a virtual time stamp i.e. period start will generate URLS that 404
                // when accessed because the ImageValueServlet requires a time stamp to match a value in the database.
                writeStartObject(START);
                writeTimestamp(stats.getPeriodStartTime());
                writeDataValue(VALUE, vo, stats.getStartValue(), stats.getPeriodStartTime(), false, raw);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getStartValue(), stats.getPeriodStartTime(), true, raw);
                writeEndObject();
            }else {
                writeNullField(START);
            }

            if(stats.getFirstValue() != null) {
                writeStartObject(FIRST);
                writeTimestamp(stats.getFirstTime());
                if(rendered)
                    writeDataValue(VALUE, vo, stats.getFirstValue(), stats.getFirstTime(), false, false);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getFirstValue(), stats.getFirstTime(), true, false);
                writeEndObject();
            }else {
                writeNullField(FIRST);
            }

            if(stats.getLastValue() != null) {
                writeStartObject(LAST);
                writeTimestamp(stats.getLastTime());
                writeDataValue(VALUE, vo, stats.getLastValue(), stats.getLastTime(), false, false);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getLastValue(), stats.getLastTime(), true, false);
                writeEndObject();
            }else {
                writeNullField(LAST);
            }
            writeIntegerField(COUNT, stats.getCount());
        } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList)statisticsGenerator;
            if(stats.getStartValue() != null) {
                writeStartObject(START);
                writeTimestamp(stats.getPeriodStartTime());
                writeDataValue(VALUE, vo, stats.getStartValue(), stats.getPeriodStartTime(), false, false);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getStartValue(), stats.getPeriodStartTime(), true, false);
                writeEndObject();
            }else {
                writeNullField(START);
            }

            if(stats.getFirstValue() != null) {
                writeStartObject(FIRST);
                writeTimestamp(stats.getFirstTime());
                writeDataValue(VALUE, vo, stats.getFirstValue(), stats.getFirstTime(), false, false);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getFirstValue(), stats.getFirstTime(), true, false);
                writeEndObject();
            }else {
                writeNullField(FIRST);
            }

            if(stats.getLastValue() != null) {
                writeStartObject(LAST);
                writeTimestamp(stats.getLastTime());
                writeDataValue(VALUE, vo, stats.getLastValue(), stats.getLastTime(), false, false);
                if(rendered)
                    writeDataValue(RENDERED, vo, stats.getLastValue(), stats.getLastTime(), true, false);
                writeEndObject();
            }else {
                writeNullField(LAST);
            }
            writeIntegerField(COUNT, stats.getCount());
            if(stats.getData().size() > 0) {
                writeStartArray(STARTS_AND_RUNTIMES);
                for(StartsAndRuntime item : stats.getData()) {
                    writeStartObject();
                    writeDataValue(VALUE, vo, item.getDataValue(), stats.getPeriodStartTime(), false, false);
                    if(rendered)
                        writeDataValue(RENDERED, vo, item.getDataValue(), stats.getPeriodStartTime(), true, false);
                    writeIntegerField(STARTS, item.getStarts());
                    writeLongField(RUNTIME, item.getRuntime());
                    writeDoubleField(PROPORTION, item.getProportion());
                    writeEndObject();
                }
                writeEndArray();
            }else {
                writeNullField(STARTS_AND_RUNTIMES);
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            Double accumulatorValue = stats.getLastValue();
            if (accumulatorValue == null) {
                accumulatorValue = stats.getMaximumValue();
            }

            if(accumulatorValue != null) {
                writeStartObject(ACCUMULATOR);
                writeTimestamp(stats.getPeriodStartTime());
                writeAnalogStatistic(VALUE, vo, accumulatorValue, false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, accumulatorValue, true, raw);
                writeEndObject();
            }else {
                writeNullField(ACCUMULATOR);
            }

            if(stats.getAverage() != null) {
                writeStartObject(AVERAGE);
                writeTimestamp(stats.getPeriodStartTime());
                writeAnalogStatistic(VALUE, vo, stats.getAverage(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getAverage(), true, raw);
                writeEndObject();
            }else {
                writeNullField(AVERAGE);
            }

            writeStartObject(DELTA);
            writeTimestamp(stats.getPeriodStartTime());
            writeAnalogStatistic(VALUE, vo, stats.getDelta(), false, raw);
            if(rendered)
                writeAnalogStatistic(RENDERED, vo, stats.getDelta(), true, raw);
            writeEndObject();

            if(stats.getMinimumValue() != null) {
                writeStartObject(MINIMUM);
                writeTimestamp(stats.getMinimumTime());
                writeAnalogStatistic(VALUE, vo, stats.getMinimumValue(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getMinimumValue(), true, raw);
                writeEndObject();
            }else {
                writeNullField(MINIMUM);
            }
            if(stats.getMaximumValue() != null) {
                writeStartObject(MAXIMUM);
                writeTimestamp(stats.getMaximumTime());
                writeAnalogStatistic(VALUE, vo, stats.getMaximumValue(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getMaximumValue(), true, raw);
                writeEndObject();
            }else {
                writeNullField(MAXIMUM);
            }

            writeStartObject(SUM);
            writeTimestamp(stats.getPeriodStartTime());
            writeAnalogStatistic(VALUE, vo, stats.getSum(), false, raw);
            if(rendered)
                writeAnalogStatistic(RENDERED, vo, stats.getSum(), true, raw);
            writeEndObject();

            if(stats.getStartValue() != null) {
                writeStartObject(START);
                writeTimestamp(stats.getPeriodStartTime());
                writeAnalogStatistic(VALUE, vo, stats.getStartValue(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getStartValue(), true, raw);
                writeEndObject();
            }else {
                writeNullField(START);
            }

            if(stats.getFirstValue() != null) {
                writeStartObject(FIRST);
                writeTimestamp(stats.getFirstTime());
                writeAnalogStatistic(VALUE, vo, stats.getFirstValue(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getFirstValue(), true, raw);
                writeEndObject();
            }else {
                writeNullField(FIRST);
            }

            if(stats.getLastValue() != null) {
                writeStartObject(LAST);
                writeTimestamp(stats.getLastTime());
                writeAnalogStatistic(VALUE, vo, stats.getLastValue(), false, raw);
                if(rendered)
                    writeAnalogStatistic(RENDERED, vo, stats.getLastValue(), true, raw);
                writeEndObject();
            }else {
                writeNullField(LAST);
            }

            if(stats.getIntegral() != null) {
                writeStartObject(INTEGRAL);
                writeTimestamp(stats.getPeriodStartTime());
                writeAnalogStatistic(VALUE, vo, stats.getIntegral(), false, raw);
                if(rendered)
                    writeStringField(RENDERED, info.getIntegralString(vo, stats.getIntegral()));
                writeEndObject();
            }else {
                writeNullField(INTEGRAL);
            }
            writeIntegerField(COUNT, stats.getCount());
        }
    }

    public void writeStatistic(String name, StatisticsGenerator statisticsGenerator, DataPointVO vo, boolean rendered, boolean raw) throws IOException{

        if(info.getRollup() == RollupEnum.ALL) {
            writeAllStatistics(statisticsGenerator, vo, rendered, raw);
            return;
        }else if(info.getRollup() == RollupEnum.POINT_DEFAULT){
            writeStatistic(name, statisticsGenerator, vo, rendered, raw, RollupEnum.convertTo(vo.getRollup()));
        }else {
            writeStatistic(name, statisticsGenerator, vo, rendered, raw, info.getRollup());
        }
    }


    /**
     * Write a rollup
     */
    protected void writeStatistic(String name, StatisticsGenerator statisticsGenerator,
            DataPointVO vo, boolean rendered, boolean raw, RollupEnum rollup) throws IOException {
        if (statisticsGenerator instanceof ValueChangeCounter) {
            //We only need the timestamp here for image links
            ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
            switch(rollup){
                case START:
                    writeDataValue(name, vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered, raw);
                    break;
                case FIRST:
                    writeDataValue(name, vo, stats.getFirstValue(), stats.getFirstTime() == null ? 0 : stats.getFirstTime(), rendered, raw);
                    break;
                case LAST:
                    writeDataValue(name, vo, stats.getLastValue(), stats.getLastTime() == null ? 0 : stats.getLastTime(), rendered, raw);
                    break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if(statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList) statisticsGenerator;
            switch(rollup){
                case START:
                    writeDataValue(name, vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered, raw);
                    break;
                case FIRST:
                    writeDataValue(name, vo, stats.getFirstValue(), stats.getFirstTime() == null ? 0 : stats.getFirstTime(), rendered, raw);
                    break;
                case LAST:
                    writeDataValue(name, vo, stats.getLastValue(), stats.getLastTime() == null ? 0 : stats.getLastTime(), rendered, raw);
                    break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch(rollup){
                case AVERAGE:
                    writeAnalogStatistic(name, vo, stats.getAverage(), rendered, raw);
                    break;
                case DELTA:
                    writeAnalogStatistic(name, vo, stats.getDelta(), rendered, raw);
                    break;
                case MINIMUM:
                    writeAnalogStatistic(name, vo, stats.getMinimumValue(), rendered, raw);
                    break;
                case MAXIMUM:
                    writeAnalogStatistic(name, vo, stats.getMaximumValue(), rendered, raw);
                    break;
                case ACCUMULATOR:
                    writeAccumulator(name, vo, stats, rendered, raw);
                    break;
                case SUM:
                    writeAnalogStatistic(name, vo, stats.getSum(), rendered, raw);
                    break;
                case START:
                    writeAnalogStatistic(name, vo, stats.getStartValue(), rendered, raw);
                    break;
                case FIRST:
                    writeAnalogStatistic(name, vo, stats.getFirstValue(), rendered, raw);
                    break;
                case LAST:
                    writeAnalogStatistic(name, vo, stats.getLastValue(), rendered, raw);
                    break;
                case COUNT:
                    writeIntegerField(name, stats.getCount());
                    break;
                case INTEGRAL:
                    writeIntegral(name, vo, stats.getIntegral(), rendered);
                    break;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        }else if(statisticsGenerator instanceof NoStatisticsGenerator) {
            NoStatisticsGenerator stats = (NoStatisticsGenerator)statisticsGenerator;
            if(stats.getValues().size() > 0) {
                for(IValueTime v : stats.getValues()) {
                    writeDataValue(name, vo, v.getValue(), v.getTime(), rendered, raw);
                }
            }else {
                writeNullField(name);
            }
        }
    }

    public LatestQueryInfo getInfo() {
        return info;
    }

    public Translations getTranslations() {
        return translations;
    }
}
