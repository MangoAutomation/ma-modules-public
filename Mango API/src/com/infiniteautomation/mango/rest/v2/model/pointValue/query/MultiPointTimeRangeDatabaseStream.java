/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Concrete Implementation for a single data point
 *
 * @author Terry Packer
 */
public class MultiPointTimeRangeDatabaseStream<T, INFO extends ZonedDateTimeRangeQueryInfo> extends MultiPointLatestDatabaseStream<T, INFO>{
    
    public MultiPointTimeRangeDatabaseStream(INFO info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
    
        //If there is a limit we can only use 1 point at a time for multiple array results
        if(info.getLimit() != null && isSql) {
            Iterator<Integer> it = voMap.keySet().iterator();
            while(it.hasNext()) {
                List<Integer> singleList = new ArrayList<>(1);
                singleList.add(it.next());
                if(info.isBookend())
                    this.dao.wideBookendQuery(singleList, info.getFromMillis(), info.getToMillis(), false, info.getLimit(), this);
                else
                    this.dao.getPointValuesBetween(singleList, info.getFromMillis(), info.getToMillis(), false, info.getLimit(), this);
            }
        }else {
            //Maybe NoSQL or no limit
            if(info.isBookend())
                this.dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
            else
                this.dao.getPointValuesBetween(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#firstValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int, boolean)
     */
    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        processRow(value, index, bookend, false);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index) throws IOException {
        processRow(value, index, true, false);
    }
}
