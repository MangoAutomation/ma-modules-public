/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.view.quantize3.AnalogStatisticsQuantizer;
import com.serotonin.m2m2.view.quantize3.BucketCalculator;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;

/**
 *
 * @author Terry Packer
 */
public class AnalogStatisticsDataPointQuantizer extends DataPointStatisticsQuantizer<AnalogStatistics>{


    public AnalogStatisticsDataPointQuantizer(DataPointVO vo, BucketCalculator calc, LimitCounter limiter, PointValueTimeWriter writer) {
        super(vo, limiter, writer);
        quantizer = new AnalogStatisticsQuantizer(calc, this);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback#quantizedStatistics(com.serotonin.m2m2.view.stats.StatisticsGenerator)
     */
    @Override
    public void quantizedStatistics(AnalogStatistics statisticsGenerator) {
        if(this.limiter.limited())
            return; //TODO Throw Exception here and catch higher up to close out stream
        try {
            this.writer.startWritePointValueTime();
            this.writer.writeStatistic(PointValueTimeWriter.VALUE, statisticsGenerator, vo);
            this.writer.writeTimestamp(statisticsGenerator.getPeriodStartTime());
            this.writer.endWritePointValueTime();
        } catch (IOException e) {
            throw new ShouldNeverHappenException(e.getMessage());
        }
    }

}
