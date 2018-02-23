/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.infiniteautomation.mango.db.query.BookendQueryCallback;
import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.BucketsBucketCalculator;
import com.infiniteautomation.mango.quantize.TimePeriodBucketCalculator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 *  Container to help map multi point queries produce statistics for each point
 *
 * @author Terry Packer
 */
public class MultiDataPointStatisticsQuantizerStream<T, INFO extends ZonedDateTimeRangeQueryInfo> extends PointValueTimeDatabaseStream<T, INFO> implements ChildStatisticsGeneratorCallback, BookendQueryCallback<IdPointValueTime>{

    protected final Map<Integer, DataPointStatisticsQuantizer<?>> quantizerMap;
    protected LinkedHashMap<Long,List<DataPointStatisticsGenerator>> periodStats;
    protected int count;

    //For our quantization
    protected final BucketCalculator bucketCalculator;
    protected Instant periodFrom;
    protected Instant periodTo;
    protected long periodToMillis; //For performance
    protected long currentTime;
    protected int currentDataPointId; //Track point change in order by ID queries
    
    public MultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.quantizerMap = new HashMap<>(voMap.size());
        this.periodStats = new LinkedHashMap<>();
        this.count = 0;
        
        //Setup for parent quantization, to fill gaps ect.
        this.bucketCalculator = getBucketCalculator();
        this.periodFrom = bucketCalculator.getStartTime().toInstant();
        this.periodTo = bucketCalculator.getNextPeriodTo().toInstant();
        this.periodToMillis = periodTo.toEpochMilli();
        this.currentTime = periodFrom.toEpochMilli();
        this.currentDataPointId = -1;
    }

    /**
     * Create a Bucket Calculator
     * @return
     */
    protected BucketCalculator getBucketCalculator(){
        if(this.info.getTimePeriod() == null){
            return  new BucketsBucketCalculator(info.getFrom(), info.getTo(), 1);
        }else{
           return new TimePeriodBucketCalculator(info.getFrom(), info.getTo(), TimePeriodType.convertFrom(this.info.getTimePeriod().getType()), this.info.getTimePeriod().getPeriods());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#firstValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int, boolean)
     */
    @Override
    public void firstValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());        
        if(!info.isSingleArray())
            writer.writeStartArray(quantizer.vo.getXid());
        updateQuantizers(value);
        quantizer.firstValue(value, index, bookend);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.PVTQueryCallback#row(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        updateQuantizers(value);
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.row(value, index);

    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.lastValue(value, index, bookend);
        if(!info.isSingleArray()) {
            writer.writeEndArray();
            count = 0; //Reset for next array
        }
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), null, this);
    }
    
    /**
     * @param value
     * @throws IOException 
     */
    protected void updateQuantizers(IdPointValueTime value) throws IOException {
        long time = value.getTime();
        if(!info.isSingleArray()) {
            //In this query the values are returned in data point ID and time order
            //Advance the previous quantizer 
            if(currentDataPointId != -1 && currentDataPointId != value.getId()) {
                DataPointStatisticsQuantizer<?> quant = this.quantizerMap.get(currentDataPointId);
                if(!quant.isDone())
                    quant.done();
            }
        }
        currentTime = time;
        currentDataPointId = value.getId();
    }
    
    protected void nextPeriod(long time) throws IOException {
        periodFrom = periodTo;
        periodTo = bucketCalculator.getNextPeriodTo().toInstant();
        periodToMillis = periodTo.toEpochMilli();
    }
    
    protected void createQuantizerMap() {
        for(Entry<Integer, DataPointVO> entry : voMap.entrySet()) {
            DataPointVO vo = entry.getValue();
            DataPointStatisticsQuantizer<?> quantizer;
            switch(vo.getPointLocator().getDataTypeId()) {
                case DataTypes.ALPHANUMERIC:
                case DataTypes.IMAGE:
                    quantizer = new ValueChangeCounterDataPointQuantizer(vo, getBucketCalculator(), this);
                break;
                case DataTypes.BINARY:
                case DataTypes.MULTISTATE:
                    quantizer = new StartsAndRuntimeListDataPointQuantizer(vo, getBucketCalculator(), this);
                break;
                case DataTypes.NUMERIC:
                    quantizer = new AnalogStatisticsDataPointQuantizer(vo, getBucketCalculator(), this);
                break;
                default:
                    throw new RuntimeException("Unknown Data Type: " + vo.getPointLocator().getDataTypeId());
            }
            
            this.quantizerMap.put(entry.getKey(), quantizer);
        }
    }

    
    protected void writePeriodStats(List<DataPointStatisticsGenerator> generators) throws IOException {
        //Code limit
        //TODO Cancel query via Exception
        if(info.getLimit() != null && count >= info.getLimit())
            return;
        
        if(info.isSingleArray() && voMap.size() > 1) {
            if(generators.size() > 0)
                this.writer.writeMultiplePointStatsAtSameTime(generators, generators.get(0).getGenerator().getPeriodStartTime());
        }else {
            for(DataPointStatisticsGenerator gen: generators)
                this.writer.writeStatsAsObject(gen);
        }
        count++;
    }
    
    protected void writePeriodStats(DataPointStatisticsGenerator generator) throws IOException{
        //Code limit
        //TODO Cancel query via Exception
        if(info.getLimit() != null && count >= info.getLimit())
            return;
        this.writer.writeStatsAsObject(generator);
        count++;
    }

    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.ChildStatisticsGeneratorCallback#quantizedStatistics(com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.DataPointStatisticsGenerator)
     */
    @Override
    public void quantizedStatistics(DataPointStatisticsGenerator generator) throws IOException {
        //Collect the stats for this period
        if(info.isSingleArray()) {
            //Do we have any entries for this period
            List<DataPointStatisticsGenerator> entries = this.periodStats.get(generator.getGenerator().getPeriodStartTime());
            if(entries == null) {
                entries = new ArrayList<>();
                this.periodStats.put(generator.getGenerator().getPeriodStartTime(), entries);
            }
            entries.add(generator);
        }else {
            //Just write it out
            writePeriodStats(generator);
        }
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeQueryStream#finish(com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter)
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        
        if(info.isSingleArray()) {
            //Fast forward to end to fill any gaps at the end
            for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values())
                if(!quant.isDone())
                    quant.done();
            Iterator<Long> it = this.periodStats.keySet().iterator();
            while(it.hasNext()) {
                List<DataPointStatisticsGenerator> entries = this.periodStats.get(it.next());
                writePeriodStats(entries);
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
}
