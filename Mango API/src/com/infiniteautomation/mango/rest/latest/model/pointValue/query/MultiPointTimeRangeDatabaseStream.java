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

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
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

    @Override
    public void streamData(PointValueTimeWriter writer) throws QueryCancelledException, IOException {

        //Can we use just the cache?
        if(info.isUseCache() == PointValueTimeCacheControl.CACHE_ONLY) {
            processCacheOnly();
            return;
        }

        //Do we need bookends?
        if(info.isBookend())
            this.dao.wideBookendQuery(new ArrayList<DataPointVO>(voMap.values()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
        else
            this.dao.getPointValuesBetween(new ArrayList<DataPointVO>(voMap.values()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
    }

    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws QueryCancelledException {
        processRow(value, index, bookend, false, false);
    }

    @Override
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws QueryCancelledException {
        processRow(value, index, false, bookend, false);
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
    protected boolean processValueThroughCache(IdPointValueTime value, int index, boolean firstBookend, boolean lastBookend) throws QueryCancelledException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getId());
        if(pointCache != null) {
            ListIterator<IdPointValueTime> it = pointCache.listIterator();
            while(it.hasNext()) {
                IdPointValueTime pvt = it.next();
                if(pvt.getTime() < value.getTime()) {
                    //Can't be a bookend
                    processRow(pvt, index, false, false, true);
                    it.remove();
                }else if(pvt.getTime() == value.getTime()) {
                    //Could be a bookend
                    processRow(pvt, index, firstBookend, lastBookend, true);
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

    @Override
    protected void processCacheOnly() throws QueryCancelledException {
        //Performance enhancement to return data within cache only
        Iterator<Integer> it = voMap.keySet().iterator();
        int index = 0;
        while(it.hasNext()) {
            Integer id = it.next();
            List<IdPointValueTime> values = cache.get(id);
            if(values == null || values.size() == 0) {
                if(info.isBookend()) {
                    processRow(new IdPointValueTime(id, null, info.getFromMillis()), index++, true, false, true);
                    processRow(new IdPointValueTime(id, null, info.getToMillis()), index++, false, true, true);
                }
            }else {
                boolean first = true;
                int limitCount = 0;
                for(IdPointValueTime value : values) {
                    if(first && info.isBookend()) {
                        //Send out first value as bookend if necessary
                        if(value.getTime() != info.getFromMillis()) {
                            //The cache should have been pruned so the value is after the start of the query and thus a null bookend
                            // is sent
                            IdPointValueTime bookend = new IdPointValueTime(value.getId(), null, info.getFromMillis());
                            processRow(bookend, index, true, false, true);
                            processRow(value, index, false, false, true);
                        }else
                            processRow(value, index, false, false, true);
                        first = false;
                    }else
                        processRow(value, index, false, false, true);
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
                        if(last instanceof IAnnotated)
                            bookend = new AnnotatedIdPointValueTime(last.getId(), last.getValue(), info.getToMillis(),((IAnnotated)last).getSourceMessage());
                        else
                            bookend = new IdPointValueTime(last.getId(), last.getValue(), info.getToMillis());
                        processRow(bookend, index, false, true, true);
                    }
                }
            }
        }
    }

    @Override
    protected void sortCache(List<IdPointValueTime> cache) {
        Collections.sort(cache);
    }
}
