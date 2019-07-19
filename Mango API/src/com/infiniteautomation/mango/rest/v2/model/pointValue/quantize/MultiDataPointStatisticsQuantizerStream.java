/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.BucketsBucketCalculator;
import com.infiniteautomation.mango.quantize.TimePeriodBucketCalculator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointValueTime;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *  Container to help map multi point queries produce statistics for each point
 *
 * @author Terry Packer
 */
public class MultiDataPointStatisticsQuantizerStream<T, INFO extends ZonedDateTimeRangeQueryInfo> extends AbstractMultiDataPointStatisticsQuantizerStream<T, INFO> {

    //Cached statistic values intra period until all points are ready to be flushed at a timestamp
    protected final LinkedHashMap<Long,List<DataPointValueTime>> periodStats;
    
    private final Map<Integer, IdPointValueTimeRow> currentValueTimeMap;
    //So we can finish the statistics efficiently
    private long lastFullPeriodToMillis;
    //Track when we are moving to a new timestamp within time ordered queries
    private Long lastTime;
    
    public MultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);        
        this.periodStats = new LinkedHashMap<>();
        this.currentValueTimeMap = new HashMap<>();
        this.lastFullPeriodToMillis = periodToMillis;
    }

    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());        
        if(!info.isSingleArray())
            writer.writeStartArray(quantizer.vo.getXid());
        updateQuantizers(value);
        quantizer.firstValue(value, index, bookend);
    }

    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        updateQuantizers(value);
        
        if(info.isSingleArray() && voMap.size() > 1) {
            //Possibly fast forward as samples come in time order and we will not receive another value at this timestamp
            //this will keep our periodStats to a minimum
            if(lastTime != null && value.getTime() != lastTime) {

                //Fast forward to just before this time
                BucketCalculator bc; 
                if(this.info.getTimePeriod() == null) {
                    bc = new BucketsBucketCalculator(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFullPeriodToMillis), info.getZoneId()), ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.getTime()), info.getZoneId()), 1);
                }else{
                   bc = new TimePeriodBucketCalculator(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFullPeriodToMillis), info.getZoneId()), ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.getTime()), info.getZoneId()), TimePeriodType.convertFrom(this.info.getTimePeriod().getType()), this.info.getTimePeriod().getPeriods());
                }
                Instant currentPeriodTo = bc.getStartTime().toInstant();
                Instant end = bc.getEndTime().toInstant();
                while(currentPeriodTo.isBefore(end)) {
                    long nextTo = currentPeriodTo.toEpochMilli();
                    for(DataPointStatisticsQuantizer<?> q : quantizerMap.values()) {
                        q.fastForward(nextTo);
                    }
                    currentPeriodTo = bc.getNextPeriodTo().toInstant();
                }
                //Finish by forwarding to the point value time
                Iterator<Integer> it = this.currentValueTimeMap.keySet().iterator();
                while(it.hasNext()) {
                    Integer id = it.next();
                    DataPointStatisticsQuantizer<?> q = this.quantizerMap.get(id);
                    IdPointValueTimeRow row = this.currentValueTimeMap.get(id);
                    if(id == value.getId()) {
                        q.row(value, index);
                    }else {
                        if(row == null) {
                            //No values in this sample period
                            q.fastForward(value.getTime());
                        }else {
                            q.row(row.value, row.index);
                        }
                    }
                } 
            }else {
                //cache the value so as not to trigger quantization until all values are ready
                currentValueTimeMap.put(value.getId(), new IdPointValueTimeRow(value, index));
            }

            lastTime = value.getTime();
        }else {
            DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
            quantizer.row(value, index);
        }
    }

    @Override
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.lastValue(value, index, bookend);
        if(!info.isSingleArray()) {
            writer.writeEndArray();
            count = 0; //Reset for next array
        }
    }

    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), null, this);
    }
    
    protected void writePeriodStats(List<DataPointValueTime> generators) throws IOException {
        //Code limit
        //TODO Cancel query via Exception
        if(info.getLimit() != null && count >= info.getLimit())
            return;
        
        if(info.isSingleArray() && voMap.size() > 1) {
            if(generators.size() > 0)
                this.writer.writeDataPointValues(generators, generators.get(0).getTime());
        }else {
            for(DataPointValueTime gen: generators)
                this.writer.writeDataPointValue(gen);
        }
        count++;
    }

    @Override
    public void quantizedStatistics(DataPointStatisticsGenerator generator) throws IOException {
        //Collect the stats for this period
        if(info.isSingleArray() && voMap.size() > 1) {
            //Do we have any entries for this period
            List<DataPointValueTime> entries = this.periodStats.get(generator.getGenerator().getPeriodStartTime());
            if(entries == null) {
                entries = new ArrayList<>();
                this.periodStats.put(generator.getGenerator().getPeriodStartTime(), entries);
            }
            entries.add(new DataPointRollupPeriodValue(generator, info.getRollup()));
            if(entries.size() == voMap.size()) {
                this.periodStats.remove(generator.getGenerator().getPeriodStartTime());
                writePeriodStats(entries);
                this.lastFullPeriodToMillis = generator.getGenerator().getPeriodEndTime();
            }
        }else {
            //Just write it out
            writePeriodStats(new DataPointRollupPeriodValue(generator, info.getRollup()));
        }
    }
    
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        
        if(info.isSingleArray()  && voMap.size() > 1) {
            
            //Fast forward to end to fill any gaps at the end and stream out data in time
            BucketCalculator bc; 
            if(this.info.getTimePeriod() == null) {
                bc = new BucketsBucketCalculator(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFullPeriodToMillis), info.getZoneId()), info.getTo(), 1);
            }else{
               bc = new TimePeriodBucketCalculator(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFullPeriodToMillis), info.getZoneId()), info.getTo(), TimePeriodType.convertFrom(this.info.getTimePeriod().getType()), this.info.getTimePeriod().getPeriods());
            }
            Instant currentPeriodTo = bc.getStartTime().toInstant();
            Instant end = bc.getEndTime().toInstant();
            while(currentPeriodTo.isBefore(end)) {
                long nextTo = currentPeriodTo.toEpochMilli();
                for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values()) {
                    quant.fastForward(nextTo);
                }
                currentPeriodTo = bc.getNextPeriodTo().toInstant();
            }
            
            for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values()) {
                if(!quant.isDone())
                    quant.done();
            }
            
            //TODO This is likely not necessary
            Iterator<Long> it = this.periodStats.keySet().iterator();
            while(it.hasNext()) {
                List<DataPointValueTime> entries = this.periodStats.get(it.next());
                writePeriodStats(entries);
                it.remove();
            }
        }else {
            //The last data point may not have been done() as well as any with 0 data
            if(currentDataPointId != -1) {
                DataPointStatisticsQuantizer<?> quant = this.quantizerMap.get(currentDataPointId);
                if(!quant.isDone())
                    quant.done();
            }
            
            for(DataPointStatisticsQuantizer<?> q : this.quantizerMap.values())
                if(!q.isDone()) {
                    if(contentType == StreamContentType.JSON)
                        this.writer.writeStartArray(q.vo.getXid());
                    q.done();
                    if(contentType == StreamContentType.JSON)
                        this.writer.writeEndArray();
                }
        }
        super.finish(writer);
    }
    
    /**
     * 
     * Container for intra interval samples
     * 
     * @author Terry Packer
     *
     */
    private static final class IdPointValueTimeRow {
        final IdPointValueTime value;
        final int index;
        
        public IdPointValueTimeRow(IdPointValueTime value, int index) {
            this.value = value;
            this.index = index;
        }
        
    }
}
