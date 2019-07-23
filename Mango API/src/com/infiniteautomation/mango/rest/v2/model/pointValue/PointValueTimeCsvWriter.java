/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeCsvWriter extends PointValueTimeJsonWriter {
    
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
    public void writeDataPointValue(DataPointValueTime value) throws IOException {
        this.jgen.writeStartObject();
        if(info.isMultiplePointsPerArray()) {
            //We don't want to embed this as an object like the Json Writer does
            value.writeEntry(this, true, true);
        }else {
            value.writeEntry(this, false, true);
        }
        this.jgen.writeEndObject();
    }
    
    /**
     * @param currentValues
     */
    @Override
    public void writeDataPointValues(List<DataPointValueTime> currentValues, long timestamp)  throws IOException{
        this.jgen.writeStartObject();
        boolean first = true;
        for(DataPointValueTime value : currentValues) {
            if(info.isMultiplePointsPerArray()) {
                value.writeEntry(this, true, first);
            }else {
                throw new ShouldNeverHappenException("Should not write multiple points here.");
            }
            first = false;
        }
        this.jgen.writeEndObject();
    }
    
    @Override
    public void writeAllStatistics(StatisticsGenerator statisticsGenerator, DataPointVO vo, boolean rendered)
            throws IOException {
        if(info.isMultiplePointsPerArray()) {
            if (statisticsGenerator instanceof ValueChangeCounter) {
                //We only need the timestamp here for image links
                ValueChangeCounter stats = (ValueChangeCounter) statisticsGenerator;
                writeDataValue(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
                writeDataValue(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime(), rendered);
                writeDataValue(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime(), rendered);
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
                StartsAndRuntimeList stats = (StartsAndRuntimeList)statisticsGenerator;
                writeDataValue(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue(), stats.getPeriodStartTime(), rendered);
                writeDataValue(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue(), stats.getFirstTime(), rendered);
                writeDataValue(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue(), stats.getLastTime(), rendered);
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            } else if (statisticsGenerator instanceof AnalogStatistics) {
                AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
                writeAccumulator(vo.getXid() + DOT + RollupEnum.ACCUMULATOR.name(), vo, stats, rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.AVERAGE.name(), vo, stats.getAverage(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.DELTA.name(), vo, stats.getDelta(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.MINIMUM.name(), vo, stats.getMinimumValue(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.MAXIMUM.name(), vo, stats.getMaximumValue(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.SUM.name(), vo, stats.getSum(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.START.name(), vo, stats.getStartValue(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.FIRST.name(), vo, stats.getFirstValue(), rendered);
                writeAnalogStatistic(vo.getXid() + DOT + RollupEnum.LAST.name(), vo, stats.getLastValue(), rendered);
                writeIntegral(vo.getXid() + DOT + RollupEnum.INTEGRAL.name(), vo, stats.getIntegral(), rendered);
                writeIntegerField(vo.getXid() + DOT + RollupEnum.COUNT.name(), stats.getCount());
            }            
        }else {
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
    }   

    @Override
    public void writeStartArray(String name) throws IOException {
        //Don't write an array field as this is not supported in CSV
        jgen.writeStartArray();
    }
}
