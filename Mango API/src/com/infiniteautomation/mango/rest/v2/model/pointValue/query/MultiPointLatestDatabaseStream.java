/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.PointValueDaoMetrics;
import com.serotonin.m2m2.db.dao.PointValueDaoSQL;
import com.serotonin.m2m2.rt.dataImage.AnnotatedIdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;

/**
 * 
 * Stream data in reverse time order before 'from' up to 'limit' number of samples
 *
 * @author Terry Packer
 */
public class MultiPointLatestDatabaseStream <T, INFO extends LatestQueryInfo> extends PointValueTimeDatabaseStream<T, INFO>{
    
    protected Map<Long, List<DataPointVOPointValueTimeBookend>> singleArrayValues; //For writing multiple points single array SQL
    protected long currentTime; //For writing multiple points single array NoSQL
    protected final List<DataPointVOPointValueTimeBookend> currentValues;
    protected final List<DataPointVOPointValueTimeBookend> finalValues;
    protected int currentDataPointId;
    protected final boolean isSql;
    //List of cached values per data point id, sorted in descending time order
    protected final Map<Integer, List<IdPointValueTime>> cache;
    protected final Map<Integer,LimitCounter> limiters;  //For use with cache so we don't return too many values, assuming that caches sizes are small this should have minimal effects
    
    public MultiPointLatestDatabaseStream(INFO info,
            Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.singleArrayValues = new LinkedHashMap<>();
        this.currentValues = new ArrayList<>(voMap.size());
        this.finalValues = new ArrayList<>(voMap.size());
        this.currentDataPointId = Common.NEW_ID;
        if(this.dao instanceof PointValueDaoMetrics) {
            PointValueDaoMetrics pvdm = (PointValueDaoMetrics)this.dao;
            this.isSql = pvdm.getBaseDao() instanceof PointValueDaoSQL;
        }else {
            this.isSql = this.dao instanceof PointValueDaoSQL;
        }
        if(info.isUseCache() != PointValueTimeCacheControl.NONE)
            cache = buildCache();
        else
            this.cache = null;
        this.limiters = new HashMap<>();
        for(Integer id : voMap.keySet())
            this.limiters.put(id, new LimitCounter(info.getLimit()));
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
        
        //Are we to use just the cache?
        if(info.isUseCache() == PointValueTimeCacheControl.CACHE_ONLY) {
            processCacheOnly();
            return;
        }
        
        if(info.getLimit() != null && isSql) {
            //If there is a limit we can only use 1 point at a time in SQL
            Iterator<Integer> it = voMap.keySet().iterator();
            while(it.hasNext()) {
                List<Integer> singleList = new ArrayList<>(1);
                singleList.add(it.next());
                this.dao.getLatestPointValues(singleList, info.getFromMillis(), false, info.getLimit(), this);
            }
        }else {
            //Maybe NoSQL or no limit
            this.dao.getLatestPointValues(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), !info.isSingleArray(), info.getLimit(), this);
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException{
        processRow(value, index, false, false);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream#finish()
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        if(info.isSingleArray() && info.getLimit() != null && isSql) {
            writeSingleArray(writer);
        }else {
            //Write out all our current values and the final bookend
            if(info.isSingleArray() && voMap.size() > 0) {
                if(currentValues.size() > 0)
                    writer.writeMultiplePointValuesAtSameTime(currentValues, currentValues.get(0).pvt.getTime());
               if(finalValues.size() > 0)
                   writer.writeMultiplePointValuesAtSameTime(finalValues, finalValues.get(0).getPvt().getTime());
            }else {
                if(voMap.size() > 1) {
                    if(contentType == StreamContentType.JSON)
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
    protected void processRow(IdPointValueTime value, int index, boolean bookend, boolean cached) throws IOException {
        
        if(info.isUseCache() != PointValueTimeCacheControl.NONE && !cached)
            if(!processValueThroughCache(value, index, bookend))
                return;

        //Don't limit bookends and don't virtually limit non-cached requests
        if(info.useCache != PointValueTimeCacheControl.NONE && !bookend)
            if(limiters.get(value.getId()).limited())
                return;
        
        if(info.isSingleArray() && info.getLimit() != null && isSql && voMap.size() > 1) {
            //Must collate in memory
            addToSingleArray(value, bookend, cached);
        }else {
            if(info.isSingleArray() && voMap.size() > 1) {
                if(currentTime == value.getTime())
                    currentValues.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value, bookend, cached));
                else {
                    if(currentValues.size() > 0) {
                        writer.writeMultiplePointValuesAtSameTime(currentValues, currentValues.get(0).pvt.getTime());
                        currentValues.clear();
                    }
                    currentTime = value.getTime();
                    currentValues.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value, bookend, cached));
                }
            }else {
                if(!info.isSingleArray()) {
                    //Writing multi-array, could be a multi-array of 1 though
                    if(currentDataPointId != value.getId()) {
                        if(currentDataPointId != -1)
                            writer.writeEndArray();
                        writer.writeStartArray(this.voMap.get(value.getId()).getXid());
                        currentDataPointId = value.getId();
                    }
                }
                writer.writePointValueTime(this.voMap.get(value.getId()), value, bookend, cached);
            }
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
    protected boolean processValueThroughCache(IdPointValueTime value, int index, boolean bookend) throws IOException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getId());
        if(pointCache != null) {
            ListIterator<IdPointValueTime> it = pointCache.listIterator();
            while(it.hasNext()) {
                IdPointValueTime pvt = it.next();
                if(pvt.getTime() > value.getTime()) {
                    //Can't be a bookend
                    processRow(pvt, index, false, true);
                    it.remove();
                }else if(pvt.getTime() == value.getTime()) {
                    //Could be a bookend
                    processRow(pvt, index, bookend, true);
                    it.remove();
                    if(pointCache.size() == 0)
                        this.cache.remove(value.getId());
                    return false;
                }else
                    break; //No more since we are in time order of the query
            }
            if(pointCache.size() == 0)
                this.cache.remove(value.getId());
        }
        return true;
    }
    
    protected void writeSingleArray(PointValueTimeWriter writer) throws IOException {
        //Write out all our values collated
        for(List<DataPointVOPointValueTimeBookend> values : this.singleArrayValues.values()) {
            if(values.size() > 0)
                writer.writeMultiplePointValuesAtSameTime(values, values.get(0).pvt.getTime());
        }
    }
    
    /**
     * @param value
     */
    protected void addToSingleArray(IdPointValueTime value, boolean bookend, boolean cached) {
      //Must collate in memory
        List<DataPointVOPointValueTimeBookend> values = singleArrayValues.get(value.getTime());
        if(values == null) {
            values = new ArrayList<DataPointVOPointValueTimeBookend>();
            singleArrayValues.put(value.getTime(), values);
        }
        values.add(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value, bookend, cached));
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
                        if(pvt.isAnnotated())
                            idPvtCache.add(new AnnotatedIdPointValueTime(id, pvt.getValue(), pvt.getTime(), ((AnnotatedPointValueTime)pvt).getSourceMessage()));
                        else
                            idPvtCache.add(new IdPointValueTime(id, pvt.getValue(), pvt.getTime()));
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
    protected void processCacheOnly() throws IOException{
        //Performance enhancement to return data within cache only
        Iterator<Integer> it = voMap.keySet().iterator();
        int index = 0;
        while(it.hasNext()) {
            List<IdPointValueTime> values = cache.get(it.next());
            int limitCount = 0;
            for(IdPointValueTime value : values) {
                processRow(value, index, false, true);
                index++;
                limitCount++;
                if(limitCount >= info.getLimit())
                    break;
            }
        }
    }
}
