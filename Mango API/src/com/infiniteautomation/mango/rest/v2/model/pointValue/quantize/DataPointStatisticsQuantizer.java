/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.infiniteautomation.mango.db.query.BookendQueryCallback;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.quantize3.AbstractPointValueTimeQuantizer;
import com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public abstract class DataPointStatisticsQuantizer<T extends StatisticsGenerator> implements StatisticsGeneratorQuantizerCallback<StatisticsGenerator>, BookendQueryCallback<IdPointValueTime>{

    protected final ChildStatisticsGeneratorCallback callback;
    protected AbstractPointValueTimeQuantizer<?> quantizer;
    protected final DataPointVO vo;
    
    public DataPointStatisticsQuantizer(DataPointVO vo, ChildStatisticsGeneratorCallback callback) {
        this.vo = vo;
        this.callback = callback;
    }
    
    public void fastForward(long time) throws IOException {
        this.quantizer.fastForward(time);
    }
    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#firstValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int, boolean)
     */
    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        quantizer.firstValue(value, index, bookend);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.PVTQueryCallback#row(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        quantizer.row(value, index);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index) throws IOException {
        quantizer.lastValue(value, index);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.PVTQueryCallback#cancelled(java.io.IOException)
     */
    @Override
    public void cancelled(IOException e) {
        this.callback.cancelled(e);
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback#quantizedStatistics(com.serotonin.m2m2.view.stats.StatisticsGenerator)
     */
    @Override
    public void quantizedStatistics(StatisticsGenerator statisticsGenerator) throws IOException {
        this.callback.quantizedStatistics(new DataPointStatisticsGenerator(vo, statisticsGenerator));
    }
}
