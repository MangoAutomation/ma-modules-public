/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
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
    
        //Can we use just the cache?
        if(info.isUseCache() == PointValueTimeCacheControl.CACHE_ONLY) {
            processCacheOnly();
            return;
        }

        //Do we need bookends?
        if(info.isBookend())
            this.dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
        else
            this.dao.getPointValuesBetween(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
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
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        processRow(value, index, bookend, false);
    }
    
    /**
     * Does this point's time fit within our query range
     * @param pvt
     * @return
     */
    @Override
    protected boolean includeCachedPoint(PointValueTime pvt) {
        return pvt.getTime() >= info.getFromMillis() && pvt.getTime() < info.getToMillis();
    }
    
    @Override
    protected boolean processValueThroughCache(IdPointValueTime value, int index, boolean bookend) throws IOException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getId());
        if(pointCache != null) {
            ListIterator<IdPointValueTime> it = pointCache.listIterator();
            while(it.hasNext()) {
                IdPointValueTime pvt = it.next();
                if(pvt.getTime() < value.getTime()) {
                    //Can't be a bookend
                    processRow(pvt, index, false, true);
                    it.remove();
                }else if(pvt.getTime() == value.getTime()) {
                    //Could be a bookend
                    processRow(pvt, index, bookend, true);
                    it.remove();
                    if(pointCache.size() == 0) {
                        this.cache.remove(value.getId());
                    }
                    return false;
                }else
                    break; //No more since we are in time order of the query
            }
            if(pointCache.size() == 0)
                this.cache.remove(value.getId());
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream#processCacheOnly()
     */
    @Override
    protected void processCacheOnly() throws IOException{
      //Performance enhancement to return data within cache only
        Iterator<Integer> it = voMap.keySet().iterator();
        int index = 0;
        while(it.hasNext()) {
            List<IdPointValueTime> values = cache.get(it.next());
            boolean first = true;
            int limitCount = 0;
            for(IdPointValueTime value : values) {
                if(first && info.isBookend()) {
                    //Send out first value as bookend if necessary
                    if(value.getTime() != info.getFromMillis()) {
                        IdPointValueTime bookend;
                        if(value.isAnnotated())
                            bookend = new AnnotatedIdPointValueTime(value.getId(), value.getValue(), info.getFromMillis(),((AnnotatedIdPointValueTime)value).getSourceMessage());
                        else
                            bookend = new IdPointValueTime(value.getId(), value.getValue(), info.getFromMillis());
                        processRow(bookend, index, true, true);
                        processRow(value, index, false, true);
                    }else
                        processRow(value, index, true, true);
                    first = false;
                }else
                    processRow(value, index, false, true);
                index++;
                limitCount++;
                if(info.getLimit() != null && limitCount >= info.getLimit())
                    break;
            }
            //Send out last value as bookend if necessary
            if(info.isBookend()) {
                IdPointValueTime last = values.get(values.size() - 1);
                if(last.getTime() != info.getToMillis()) {
                    IdPointValueTime bookend;
                    if(last.isAnnotated())
                        bookend = new AnnotatedIdPointValueTime(last.getId(), last.getValue(), info.getToMillis(),((AnnotatedIdPointValueTime)last).getSourceMessage());
                    else
                        bookend = new IdPointValueTime(last.getId(), last.getValue(), info.getToMillis());
                    processRow(bookend, index, true, true);
                }
            }
        }
    }
    
    @Override
    protected void sortCache(List<IdPointValueTime> cache) {
        Collections.sort(cache);
    }
}
