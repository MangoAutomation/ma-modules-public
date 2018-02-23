/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.goebl.simplify.Simplify;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointVOPointValueTimeBookend;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.SimplifyPointValueExtractor;
import com.serotonin.log.LogStopWatch;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.PointValueDao;
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
    
    protected long currentTime; //For writing multiple points single array NoSQL
    protected final List<DataPointVOPointValueTimeBookend> currentValues;
    protected int currentDataPointId;
    //List of cached values per data point id, sorted in descending time order
    protected final Map<Integer, List<IdPointValueTime>> cache;
    protected final Map<Integer,LimitCounter> limiters;  //For use with cache so we don't return too many values, assuming that caches sizes are small this should have minimal effects
    protected final List<DataPointVOPointValueTimeBookend> bookends;
    
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
        this.dao.getLatestPointValues(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), !info.isSingleArray(), info.getLimit(), this);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideStartQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException{
        processRow(value, index, false, false, false);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream#finish()
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        //Write out all our current values and the final bookend
        if(info.isSingleArray() && voMap.size() > 1) {
            if(currentValues.size() > 0)
                writer.writeMultiplePointValuesAtSameTime(currentValues, currentValues.get(0).getPvt().getTime());
            if(bookends.size() > 0)
                writer.writeMultiplePointValuesAtSameTime(bookends, bookends.get(0).getPvt().getTime());
        }else {
            if(!info.isSingleArray()) {
                if(contentType == StreamContentType.JSON)
                    writer.writeEndArray();
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
    protected void processRow(IdPointValueTime value, int index, boolean firstBookend, boolean lastBookend, boolean cached) throws IOException {
        
        if(info.isUseCache() != PointValueTimeCacheControl.NONE && !cached)
            if(!processValueThroughCache(value, index, firstBookend, lastBookend))
                return;

        //Don't limit bookends and don't virtually limit non-cached requests
        if(info.useCache != PointValueTimeCacheControl.NONE && (!firstBookend && !lastBookend))
            if(limiters.get(value.getId()).limited())
                return;
        
        //Write it out/process it
        writeValue(new DataPointVOPointValueTimeBookend(this.voMap.get(value.getId()), value, firstBookend, lastBookend, cached));
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
                        writer.writeMultiplePointValuesAtSameTime(currentValues, currentValues.get(0).getPvt().getTime());
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
                    if(currentDataPointId != -1)
                        writer.writeEndArray();
                    writer.writeStartArray(this.voMap.get(value.getId()).getXid());
                    currentDataPointId = value.getId();
                }
            }
            writer.writePointValueTime(value);
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
    protected boolean processValueThroughCache(IdPointValueTime value, int index, boolean firstBookend, boolean lastBookend) throws IOException {
        List<IdPointValueTime> pointCache = this.cache.get(value.getId());
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
                processRow(value, index, false, false, true);
                index++;
                limitCount++;
                if(info.getLimit() != null && limitCount >= info.getLimit())
                    break;
            }
        }
    }
    
    /**
     * Simplify according to our requirements
     * 
     * TODO This currently only works for Numeric Points
     * 
     * @param list
     * @return
     */
    protected List<DataPointVOPointValueTimeBookend> simplify(
            List<DataPointVOPointValueTimeBookend> list) {
        LogStopWatch logStopWatch = new LogStopWatch();
        if(info.simplifyTolerance != null) {
            //TODO improve Simplify code to return a list
            Simplify<DataPointVOPointValueTimeBookend> simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
            DataPointVOPointValueTimeBookend[] simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), info.simplifyTolerance, info.simplifyHighQuality);
            logStopWatch.stop("Finished Simplify, tolerance: " + info.simplifyTolerance);
            return Arrays.asList(simplified);
        }else {
            if(list.size() < info.simplifyTarget)
                return list;
            
            //Compute target bounds as 10% of target
            int lowerTarget = info.simplifyTarget - (int)(info.simplifyTarget * 0.1);
            int upperTarget = info.simplifyTarget + (int)(info.simplifyTarget * 0.1);
            
            //Compute tolerance bounds and initial tolerance
            Double max = Double.MIN_VALUE;
            Double min = Double.MAX_VALUE;
            for(DataPointVOPointValueTimeBookend value : list) {
                if(value.getPvt().getDoubleValue() > max)
                    max = value.getPvt().getDoubleValue();
                if(value.getPvt().getDoubleValue() < min)
                    min = value.getPvt().getDoubleValue();
            }
            double difference = max - min;
            double tolerance = difference / 20d;
            double topBound = difference;
            double bottomBound = 0;
            
            //Determine max iterations we can allow
            int maxIterations = 100;
            int iteration = 1;
            
            Simplify<DataPointVOPointValueTimeBookend> simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
            DataPointVOPointValueTimeBookend[] simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), tolerance, info.simplifyHighQuality);
            DataPointVOPointValueTimeBookend[] best = simplified;
            while(simplified.length < lowerTarget || simplified.length > upperTarget) {
                
                if (simplified.length > info.simplifyTarget) {
                    bottomBound = tolerance;
                } else {
                    topBound = tolerance;
                }
                
                //Adjust tolerance
                tolerance = bottomBound + (topBound - bottomBound) / 2.0d;
                simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
                simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), tolerance, info.simplifyHighQuality);
                
                //Keep our best effort
                if(Math.abs(info.simplifyTarget - simplified.length) < Math.abs(info.simplifyTarget - best.length))
                    best = simplified;

                if(iteration > maxIterations) {
                    simplified = best;
                    break;
                }

                iteration++;
            }
            
            logStopWatch.stop("Finished Simplify, target: " + info.simplifyTarget + " actual " + simplified.length);
            return Arrays.asList(simplified);
        }
        
    }
}
