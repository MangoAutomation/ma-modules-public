/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.PointValueDao.TimeOrder;
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
        if(info.isBookend()) {
            Collection<? extends DataPointVO> vos = new ArrayList<DataPointVO>(voMap.values());
            if (info.isSingleArray()) {
                this.dao.wideBookendQueryCombined(vos, info.getFromMillis(), info.getToMillis(), info.getLimit(), this);
            } else {
                this.dao.wideBookendQueryPerPoint(vos, info.getFromMillis(), info.getToMillis(), info.getLimit(), this);
            }
        }
        else {
            if (info.isSingleArray()) {
                this.dao.getPointValuesCombined(new ArrayList<>(voMap.values()), info.getFromMillis(), info.getToMillis(), info.getLimit(), TimeOrder.ASCENDING, this);
            } else {
                this.dao.getPointValuesPerPoint(new ArrayList<>(voMap.values()), info.getFromMillis(), info.getToMillis(), info.getLimit(), TimeOrder.ASCENDING, this);
            }
        }
    }

    @Override
    public void firstValue(IdPointValueTime value, boolean bookend) {
        processRow(value, bookend, false, false);
    }

    @Override
    public void lastValue(IdPointValueTime value, boolean bookend) {
        processRow(value, false, bookend, false);
    }

    /**
     * Does this point's time fit within our query range
     */
    @Override
    protected boolean includeCachedPoint(PointValueTime pvt) {
        return pvt.getTime() >= info.getFromMillis() && pvt.getTime() < info.getToMillis();
    }

    @Override
    protected boolean processValueThroughCache(IdPointValueTime value, boolean firstBookend, boolean lastBookend) throws QueryCancelledException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getSeriesId());
        if(pointCache != null) {
            ListIterator<IdPointValueTime> it = pointCache.listIterator();
            while(it.hasNext()) {
                IdPointValueTime pvt = it.next();
                if(pvt.getTime() < value.getTime()) {
                    //Can't be a bookend
                    processRow(pvt, false, false, true);
                    it.remove();
                }else if(pvt.getTime() == value.getTime()) {
                    //Could be a bookend
                    processRow(pvt, firstBookend, lastBookend, true);
                    it.remove();
                    if(pointCache.size() == 0) {
                        this.cache.remove(value.getSeriesId());
                    }
                    return false;
                }else
                    break; //No more since we are in time order of the query
            }
            if(pointCache.size() == 0)
                this.cache.remove(value.getSeriesId());
        }
        return true;
    }

    @Override
    protected void processCacheOnly() throws QueryCancelledException {
        //Performance enhancement to return data within cache only
        Iterator<Integer> it = voMap.keySet().iterator();
        while(it.hasNext()) {
            Integer id = it.next();
            DataPointVO vo = voMap.get(id);
            List<IdPointValueTime> values = cache.get(id);
            if(values == null || values.size() == 0) {
                if(info.isBookend()) {
                    processRow(new IdPointValueTime(vo.getSeriesId(), null, info.getFromMillis()), true, false, true);
                    processRow(new IdPointValueTime(vo.getSeriesId(), null, info.getToMillis()), false, true, true);
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
                            IdPointValueTime bookend = new IdPointValueTime(value.getSeriesId(), null, info.getFromMillis());
                            processRow(bookend, true, false, true);
                            processRow(value, false, false, true);
                        }else
                            processRow(value, false, false, true);
                        first = false;
                    }else
                        processRow(value, false, false, true);
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
                            bookend = new AnnotatedIdPointValueTime(last.getSeriesId(), last.getValue(), info.getToMillis(),((IAnnotated)last).getSourceMessage());
                        else
                            bookend = new IdPointValueTime(last.getSeriesId(), last.getValue(), info.getToMillis());
                        processRow(bookend, false, true, true);
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
