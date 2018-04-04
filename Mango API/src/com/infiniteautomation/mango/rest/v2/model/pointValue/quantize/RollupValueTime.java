/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.goebl.simplify.NullValueException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.statistics.AnalogStatistics;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class RollupValueTime extends AbstractRollupValueTime {

    private final DataPointStatisticsGenerator generator;
    private final RollupEnum rollup;
    
    public RollupValueTime(DataPointStatisticsGenerator generator, RollupEnum rollup) {
        this.generator = generator;
        this.rollup = rollup;
    }
    
    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getX()
     */
    @Override
    public double getX() {
        return generator.getGenerator().getPeriodStartTime();
    }

    /* (non-Javadoc)
     * @see com.goebl.simplify.Point#getY()
     */
    @Override
    public double getY() throws NullValueException {
        //TODO Mango 3.4 what about null values?
        StatisticsGenerator statisticsGenerator = generator.getGenerator();
        if (statisticsGenerator instanceof ValueChangeCounter) {
            throw new ShouldNeverHappenException("Can't simplify Alphanumeric or Image data");
        } else if(statisticsGenerator instanceof StartsAndRuntimeList) {
            StartsAndRuntimeList stats = (StartsAndRuntimeList) statisticsGenerator;
            switch(rollup){
                case START:
                    if(stats.getStartValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getStartValue().getDoubleValue();
                case FIRST:
                    if(stats.getFirstValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getFirstValue().getDoubleValue();
                case LAST:
                    if(stats.getLastValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getLastValue().getDoubleValue();
                case COUNT:
                    return stats.getCount();
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        } else if (statisticsGenerator instanceof AnalogStatistics) {
            AnalogStatistics stats = (AnalogStatistics) statisticsGenerator;
            switch(rollup){
                case AVERAGE:
                    if(stats.getAverage().isNaN())
                        throw new NullValueException();
                    else
                        return stats.getAverage();
                case DELTA:
                    return stats.getDelta();
                case MINIMUM:
                    if(stats.getMinimumValue() == Double.NaN)
                        throw new NullValueException();
                    else
                        return stats.getMinimumValue();
                case MAXIMUM:
                    if(stats.getMaximumValue() == Double.NaN)
                        throw new NullValueException();
                    else
                        return stats.getMaximumValue();

                case ACCUMULATOR:
                    if(stats.getLastValue() == null)
                        if(stats.getMaximumValue() == null)
                            throw new NullValueException();
                        else
                            return stats.getMaximumValue();
                    else
                        return stats.getLastValue();
                case SUM:
                    return stats.getSum();
                case START:
                    if(stats.getStartValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getStartValue();
                case FIRST:
                    if(stats.getFirstValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getFirstValue();
                case LAST:
                    if(stats.getLastValue() == null)
                        throw new NullValueException();
                    else
                        return stats.getLastValue();
                case COUNT:
                    return stats.getCount();
                case INTEGRAL:
                    if(stats.getIntegral() == Double.NaN)
                        throw new NullValueException();
                    else
                        return stats.getIntegral();
                default:
                    throw new ShouldNeverHappenException("Unknown Rollup type " + rollup);
            }
        }else if(statisticsGenerator instanceof NoStatisticsGenerator) {
            throw new ShouldNeverHappenException("Fix this.");
        }else {
            throw new ShouldNeverHappenException("Unknown Stats type.");
        }
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.AbstractRollupValueTime#writePointValueTime(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void writePointValueTime(PointValueTimeWriter writer) throws IOException {
        writer.writeStatsAsObject(generator);
    }

}
