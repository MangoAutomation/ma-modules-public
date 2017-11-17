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
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * Concrete Implementation for a single data point
 *
 * @author Terry Packer
 */
public class SinglePointValueTimeDatabaseStream extends PointValueTimeDatabaseStream<PointValueTimeModel>{

    
    
    public SinglePointValueTimeDatabaseStream(ZonedDateTimeRangeQueryInfo info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#preQuery(java.lang.Object)
     */
    @Override
    public void preQuery(IdPointValueTime value, boolean bookend) throws IOException{
        writer.writePointValueTime(this.voMap.get(value.getId()), value, bookend);
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
    }
    
}
