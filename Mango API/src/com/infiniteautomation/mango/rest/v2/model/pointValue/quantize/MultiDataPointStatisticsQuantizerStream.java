/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.infiniteautomation.mango.db.query.BookendQueryCallback;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.quantize3.BucketCalculator;
import com.serotonin.m2m2.view.quantize3.BucketsBucketCalculator;
import com.serotonin.m2m2.view.quantize3.TimePeriodBucketCalculator;
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

    
    public MultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.quantizerMap = new HashMap<>(voMap.size());
        this.periodStats = new LinkedHashMap<>();
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
        quantizer.firstValue(value, index, bookend);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.PVTQueryCallback#row(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.row(value, index);
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.db.query.BookendQueryCallback#lastValue(com.serotonin.m2m2.rt.dataImage.PointValueTime, int)
     */
    @Override
    public void lastValue(IdPointValueTime value, int index) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.lastValue(value, index);
        if(!info.isSingleArray())
            writer.writeEndArray();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(PointValueTimeWriter writer) throws IOException {
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
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
        if(info.isSingleArray() && voMap.size() > 1) {
            if(generators.size() > 0)
                this.writer.writeMultiplePointStatsAtSameTime(generators, generators.get(0).getGenerator().getPeriodStartTime());
        }else {
            for(DataPointStatisticsGenerator gen: generators)
                this.writer.writeStatsAsObject(gen);
        }
    }
    
    protected void writePeriodStats(DataPointStatisticsGenerator generator) throws IOException{
        this.writer.writeStatsAsObject(generator);
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
            Iterator<Long> it = this.periodStats.keySet().iterator();
            while(it.hasNext()) {
                entries = this.periodStats.get(it.next());
                if(entries.size() == voMap.size()) {
                    writePeriodStats(entries);
                    it.remove();
                }
            }
        }else {
            //Just write it out
            writePeriodStats(generator);
        }
        
    }
}
