/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter;
import com.serotonin.db.WideQueryCallback;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.quantize3.AbstractPointValueTimeQuantizer;
import com.serotonin.m2m2.view.quantize3.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;

/**
 *
 * @author Terry Packer
 */
public abstract class DataPointStatisticsQuantizer<T extends StatisticsGenerator> implements StatisticsGeneratorQuantizerCallback<T>, WideQueryCallback<IdPointValueTime>{

    protected AbstractPointValueTimeQuantizer<?> quantizer;
    protected final DataPointVO vo;
    protected final LimitCounter limiter;
    protected final PointValueTimeWriter writer;
    
    public DataPointStatisticsQuantizer(DataPointVO vo, LimitCounter limiter, PointValueTimeWriter writer) {
        this.vo = vo;
        this.limiter = limiter;
        this.writer = writer;
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#preQuery(java.lang.Object, boolean)
     */
    @Override
    public void preQuery(IdPointValueTime value, boolean bookend) throws IOException {
        quantizer.preQuery(value, bookend);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        quantizer.row(value, index);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#postQuery(java.lang.Object, boolean)
     */
    @Override
    public void postQuery(IdPointValueTime value, boolean bookend) throws IOException {
        quantizer.postQuery(value, bookend);
    }
}
