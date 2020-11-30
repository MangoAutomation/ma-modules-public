/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointValueTime;
import com.infiniteautomation.mango.rest.latest.model.pointValue.LimitCounter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * Stream data in reverse time order before 'from' up to 'limit' number of samples
 *
 * @author Terry Packer
 */
public class MultiPointLatestDatabaseStream <T, INFO extends LatestQueryInfo> extends PointValueTimeDatabaseStream<T, INFO>{

    protected long currentTime; //For writing multiple points single array NoSQL
    protected final List<DataPointValueTime> currentValues;
    protected int currentDataPointId;
    //List of cached values per data point id, sorted in descending time order
    protected final Map<Integer, List<IdPointValueTime>> cache;
    protected final Map<Integer, LimitCounter> limiters;  //For use with cache so we don't return too many values, assuming that caches sizes are small this should have minimal effects
    protected final List<DataPointValueTime> bookends;

    public MultiPointLatestDatabaseStream(INFO info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);

        this.currentValues = new ArrayList<>(voMap.size());
        this.currentDataPointId = Common.NEW_ID;
        if(info.isUseCache() != PointValueTimeCacheControl.NONE)
            cache = buildCache();
        else
            this.cache = null;
        this.limiters = new HashMap<>();
        for(Integer id : voMap.keySet())
            this.limiters.put(id, new LimitCounter(info.getLimit()));
        this.bookends = new ArrayList<>(voMap.size());
    }

    @Override
    public void streamData(PointValueTimeWriter writer) throws QueryCancelledException, IOException {

        //Are we to use just the cache?
        if(info.isUseCache() == PointValueTimeCacheControl.CACHE_ONLY) {
            processCacheOnly();
            return;
        }
        this.dao.getLatestPointValues(new ArrayList<DataPointVO>(voMap.values()), info.getFromMillis(), !info.isSingleArray(), info.getLimit(), this);
    }

    @Override
    public void row(IdPointValueTime value, int index) throws QueryCancelledException{
        processRow(value, index, false, false, false);
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        //Write out all our current values and the final bookend
        if(info.isSingleArray() && voMap.size() > 1) {
            if(currentValues.size() > 0)
                writer.writeDataPointValues(currentValues, currentValues.get(0).getTime());
            if(bookends.size() > 0)
                writer.writeDataPointValues(bookends, bookends.get(0).getTime());
        }else {
            if(!info.isSingleArray()) {
                if(contentType == StreamContentType.JSON) {
                    if(this.currentDataPointId != Common.NEW_ID)
                        writer.writeEndArray();
                }
            }
        }
        super.finish(writer);
    }

    /**
     * Common row processing logic
     * @param value
     * @param index
     * @param bookend
     * @throws IOException
     */
    protected void processRow(IdPointValueTime value, int index, boolean firstBookend, boolean lastBookend, boolean cached) throws QueryCancelledException {
        try {
            if(info.isUseCache() != PointValueTimeCacheControl.NONE && !cached)
                if(!processValueThroughCache(value, index, firstBookend, lastBookend))
                    return;

            //Don't limit bookends and don't virtually limit non-cached requests
            if(info.useCache != PointValueTimeCacheControl.NONE && (!firstBookend && !lastBookend))
                if(limiters.get(value.getSeriesId()).limited())
                    return;

            //Write it out/process it
            writeValue(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getSeriesId()), value, firstBookend, lastBookend, cached));
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    /**
     * Write the value or collate it based on our output structure
     * @param value
     * @throws IOException
     */
    protected void writeValue(DataPointVOPointValueTimeBookend value) throws IOException {

        if(info.isSingleArray() && voMap.size() > 1) {
            if(value.isLastBookend())
                bookends.add(value);
            else {
                if(currentTime == value.getTime())
                    currentValues.add(value);
                else {
                    if(currentValues.size() > 0) {
                        writer.writeDataPointValues(currentValues, currentValues.get(0).getTime());
                        currentValues.clear();
                    }
                    currentTime = value.getTime();
                    currentValues.add(value);
                }
            }
        }else {
            if(!info.isSingleArray()) {
                //Writing multi-array, could be a multi-array of 1 though
                if(currentDataPointId != value.getId()) {
                    if(currentDataPointId != Common.NEW_ID)
                        writer.writeEndArray();
                    writer.writeStartArray(this.voMap.get(value.getId()).getXid());
                    currentDataPointId = value.getId();
                }
            }
            writer.writeDataPointValue(value);
        }
    }

    /**
     * Write out any cached values that would be equal to or between the time of the incomming
     *   point value and the next one to be returned by the query.
     *   this should be called before processing this value
     * @param value
     * @param bookend
     * @return true to continue to process the incoming value, false if it was a bookend that was replaced via the cache
     * @throws IOException
     */
    protected boolean processValueThroughCache(IdPointValueTime value, int index, boolean firstBookend, boolean lastBookend) throws QueryCancelledException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getSeriesId());
        if(pointCache != null) {
            ListIterator<IdPointValueTime> it = pointCache.listIterator();
            while(it.hasNext()) {
                IdPointValueTime pvt = it.next();
                if(pvt.getTime() > value.getTime()) {
                    //Can't be a bookend
                    processRow(pvt, index, false, false, true);
                    it.remove();
                }else if(pvt.getTime() == value.getTime()) {
                    //Could be a bookend
                    processRow(pvt, index, firstBookend, lastBookend, true);
                    it.remove();
                    if(pointCache.size() == 0)
                        this.cache.remove(value.getSeriesId());
                    return false;
                }else
                    break; //No more since we are in time order of the query
            }
            if(pointCache.size() == 0)
                this.cache.remove(value.getSeriesId());
        }
        return true;
    }

    /**
     * Does this point's time fit within our query range
     * @param pvt
     * @return
     */
    protected boolean includeCachedPoint(PointValueTime pvt) {
        return pvt.getTime() < info.getFromMillis();
    }

    /**
     * Sort the cache based on our query info
     * @param cache
     */
    protected void sortCache(List<IdPointValueTime> cache) {
        Collections.sort(cache,
                new Comparator<IdPointValueTime>() {
            // Compare such that data sets are returned in time
            // descending order
            // which turns out is opposite of compare to method for
            // PointValueTime objects
            @Override
            public int compare(IdPointValueTime o1,
                    IdPointValueTime o2) {
                if (o1.getTime() < o2.getTime())
                    return 1;
                if (o1.getTime() > o2.getTime())
                    return -1;
                return 0;
            }
        });
    }
    /**
     * Build the cache based on our Query Info
     * @param voMap
     * @param limit
     * @return
     */
    protected Map<Integer, List<IdPointValueTime>> buildCache() {
        Map<Integer, List<IdPointValueTime>> map = new HashMap<>();
        for(Integer id : voMap.keySet()) {
            DataPointRT rt = Common.runtimeManager.getDataPoint(id);
            if(rt != null) {
                List<PointValueTime> cache;
                if(info.getLimit() != null)
                    cache = rt.getCacheCopy(info.getLimit());
                else
                    cache = rt.getCacheCopy();
                List<IdPointValueTime> idPvtCache = new ArrayList<>(cache.size());
                for(PointValueTime pvt : cache) {
                    if(includeCachedPoint(pvt)) {
                        if(pvt instanceof IAnnotated)
                            idPvtCache.add(new AnnotatedIdPointValueTime(id, pvt.getValue(), pvt.getTime(), ((IAnnotated)pvt).getSourceMessage()));
                        else
                            idPvtCache.add(new IdPointValueTime(rt.getVO().getSeriesId(), pvt.getValue(), pvt.getTime()));
                    }
                }

                if(!idPvtCache.isEmpty()) {
                    sortCache(idPvtCache);
                    map.put(id, idPvtCache);
                }
            }
        }
        return map;
    }

    /**
     * Process all data from the cache respecting the query restrictions
     * @throws IOException
     */
    protected void processCacheOnly() throws QueryCancelledException {
        //Performance enhancement to return data within cache only
        Iterator<Integer> it = voMap.keySet().iterator();
        int index = 0;
        while(it.hasNext()) {
            List<IdPointValueTime> values = cache.get(it.next());
            int limitCount = 0;
            for(IdPointValueTime value : values) {
                processRow(value, index, false, false, true);
                index++;
                limitCount++;
                if(info.getLimit() != null && limitCount >= info.getLimit())
                    break;
            }
        }
    }
}
