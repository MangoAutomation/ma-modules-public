/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *  Container to help map multi point queries produce statistics for each point
 *
 * @author Terry Packer
 */
public class MultiDataPointStatisticsQuantizerStream<T, INFO extends ZonedDateTimeRangeQueryInfo> extends AbstractMultiDataPointStatisticsQuantizerStream<T, INFO> {

    protected final LinkedHashMap<Long,List<DataPointStatisticsGenerator>> periodStats;

    public MultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);        
        this.periodStats = new LinkedHashMap<>();
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
