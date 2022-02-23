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
                        return stats.getLastValue().getDoubleValue();
                case SUM:
                    return stats.getSum();
                case START:
                    return stats.getStartValue().getDoubleValue();
                case FIRST:
                    return stats.getFirstValue().getDoubleValue();
                case LAST:
                    return stats.getLastValue().getDoubleValue();
                case COUNT:
                    return stats.getCount();
                case INTEGRAL:
                    return stats.getIntegral();
                case ARITHMETIC_MEAN:
                    return stats.getArithmeticMean();
                case MINIMUM_IN_PERIOD:
                    return stats.getMinimumInPeriod();
                case MAXIMUM_IN_PERIOD:
                    return stats.getMaximumInPeriod();
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
        return Double.compare(getX(), that.getX());
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
