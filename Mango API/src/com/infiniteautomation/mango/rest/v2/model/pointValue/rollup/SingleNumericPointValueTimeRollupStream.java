/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.rollup;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.view.quantize3.AnalogStatisticsQuantizer;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class SingleNumericPointValueTimeRollupStream extends SinglePointValueTimeRollupStream<AnalogStatistics>{

    /**
     * @param info
     * @param voMap
     * @param dao
     */
    public SingleNumericPointValueTimeRollupStream(ZonedDateTimeRangeQueryInfo info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.rollup.SinglePointValueTimeRollupStream#getQuantizer()
     */
    @Override
    protected AnalogStatisticsQuantizer getQuantizer() {
        return new AnalogStatisticsQuantizer(getBucketCalculator(), this);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback#quantizedStatistics(com.serotonin.m2m2.view.stats.StatisticsGenerator)
     */
    @Override
    public void quantizedStatistics(AnalogStatistics statisticsGenerator) {
        if(this.limiter.limited())
            return; //TODO Throw Exception here and catch higher up to close out stream
        DataPointVO vo = this.voMap.get(this.voMap.keySet().iterator().next());
        try {
            this.writer.startWritePointValueTime();
            this.writer.writeTimestamp(statisticsGenerator.getPeriodStartTime());
            this.writer.writeStatistic(PointValueTimeWriter.VALUE, statisticsGenerator, vo);
            this.writer.endWritePointValueTime();
        } catch (IOException e) {
            throw new ShouldNeverHappenException(e.getMessage());
        }
        
    }

}
