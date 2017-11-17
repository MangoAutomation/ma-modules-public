/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.Map;

import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Concrete Implementation for a single data point
 *
 * @author Terry Packer
 */
public class MultiPointValueTimeDatabaseStream<T> extends PointValueTimeDatabaseStream<T>{
    
    public MultiPointValueTimeDatabaseStream(ZonedDateTimeRangeQueryInfo info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#preQuery(java.lang.Object)
     */
    @Override
    public void preQuery(IdPointValueTime value, boolean bookend) throws IOException{
        DataPointVO vo = this.voMap.get(value.getId());
        
        if(!info.isSingleArray())
            writer.startWriteArray(vo.getXid());

        writer.writePointValueTime(vo, value, bookend);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException{
        writer.writePointValueTime(this.voMap.get(value.getId()), value);
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#postQuery(java.lang.Object)
     */
    @Override
    public void postQuery(IdPointValueTime value, boolean bookend) throws IOException{
        writer.writePointValueTime(this.voMap.get(value.getId()), value, bookend);
        if(!info.isSingleArray())
            writer.endWriteArray();
    }
    
}
