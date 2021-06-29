/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import java.io.IOException;

import com.goebl.simplify.Point;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointValueTime;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class DataPointRollupPeriodValue implements DataPointValueTime {

    private final DataPointStatisticsGenerator generator;
    private final RollupEnum rollup;

    public DataPointRollupPeriodValue(DataPointStatisticsGenerator generator, RollupEnum rollup) {
        this.generator = generator;
        this.rollup = rollup;
    }

    @Override
    public double getX() {
        return generator.getGenerator().getPeriodStartTime();
    }

    @Override
    public boolean isProcessable() {
        StatisticsGenerator statisticsGenerator = generator.getGenerator();
        if (statisticsGenerator instanceof ValueChangeCounter) {
            throw new ShouldNeverHappenException("Can't simplify Alphanumeric or Image data");
        } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList) statisticsGenerator;
            switch (rollup) {
                case START:
                    return isDataValueProcessable(stats.getStartValue());
                case FIRST:
                    return isDataValueProcessable(stats.getFirstValue());
                case LAST:
                    return isDataValueProcessable(stats.getLastValue());
                case COUNT:
                    return true;
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch (rollup) {
                case AVERAGE:
                    return isDoubleProcessable(stats.getAverage());
                case DELTA:
                    return true; // always double
                case MINIMUM:
                    return isDoubleProcessable(stats.getMinimumValue());
                case MAXIMUM:
                    return stats.getMaximumValue() != null;
                case ACCUMULATOR:
                    if (stats.getLastValue() == null)
                        return isDoubleProcessable(stats.getMaximumValue());
                    else
                        return isDoubleProcessable(stats.getLastValue());
                case SUM:
                    return true; // Always double
                case START:
                    return isDoubleProcessable(stats.getStartValue());
                case FIRST:
                    return isDoubleProcessable(stats.getFirstValue());
                case LAST:
                    return isDoubleProcessable(stats.getLastValue());
                case COUNT:
                    return true;
                case INTEGRAL:
                    return isDoubleProcessable(stats.getIntegral());
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof NoStatisticsGenerator) {
            throw new ShouldNeverHappenException("Fix this.");
        } else {
            throw new ShouldNeverHappenException("Unknown Stats type.");
        }
    }

    /**
     * Is the data value acceptable for Simplify?
     * @param startValue
     * @return
     */
    private boolean isDataValueProcessable(DataValue value) {
        if(value == null)
            return false;
        double doubleValue = value.getDoubleValue();
        if(Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))
            return false;
        return true;
    }

    /**
     * @param doubleValue
     * @return
     */
    private boolean isDoubleProcessable(Double value) {
        if(value == null)
            return false;
        if(value.isNaN() || value.isInfinite())
            return false;
        return true;
    }

    /**
     * This method can throw NPE, always check isValueNumber first!
     */
    @Override
    public double getY() {

        StatisticsGenerator statisticsGenerator = generator.getGenerator();
        if (statisticsGenerator instanceof ValueChangeCounter) {
            throw new ShouldNeverHappenException("Can't simplify Alphanumeric or Image data");
        } else if (statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList) statisticsGenerator;
            switch (rollup) {
                case START:
                    return stats.getStartValue().getDoubleValue();
                case FIRST:
                    return stats.getFirstValue().getDoubleValue();
                case LAST:
                    return stats.getLastValue().getDoubleValue();
                case COUNT:
                    return stats.getCount();
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch (rollup) {
                case AVERAGE:
                    return stats.getAverage();
                case DELTA:
                    return stats.getDelta();
                case MINIMUM:
                    return stats.getMinimumValue();
                case MAXIMUM:
                    return stats.getMaximumValue();
                case ACCUMULATOR:
                    if (stats.getLastValue() == null)
                        return stats.getMaximumValue();
                    else
                        return stats.getLastValue();
                case SUM:
                    return stats.getSum();
                case START:
                    return stats.getStartValue();
                case FIRST:
                    return stats.getFirstValue();
                case LAST:
                    return stats.getLastValue();
                case COUNT:
                    return stats.getCount();
                case INTEGRAL:
                    return stats.getIntegral();
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof NoStatisticsGenerator) {
            throw new ShouldNeverHappenException("Unsupported.");
        } else {
            throw new ShouldNeverHappenException("Unknown Stats type.");
        }
    }

    @Override
    public long getTime() {
        return generator.getGenerator().getPeriodStartTime();
    }

    @Override
    public void writeEntry(PointValueTimeWriter writer, boolean useXid, boolean allowTimestamp)
            throws IOException {
        for(PointValueField field : writer.getInfo().getFields()) {
            if(!allowTimestamp && field == PointValueField.TIMESTAMP)
                continue;
            field.writeValue(generator, writer.getInfo(), writer.getTranslations(), useXid, writer);
        }
    }

    @Override
    public int compareTo(Point that) {
        if (getX() < that.getX())
            return -1;
        if (getX() > that.getX())
            return 1;
        return 0;
    }

    @Override
    public DataPointVO getVo() {
        return generator.getVo();
    }

    @Override
    public String toString() {
        return "XID: " + generator.getVo().getXid();
    }
}
