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

import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointValueTime;
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

    protected final LinkedHashMap<Long,List<DataPointValueTime>> periodStats;

    public MultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);        
        this.periodStats = new LinkedHashMap<>();
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
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.row(value, index);

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
        }else {
            //Just write it out
            writePeriodStats(new DataPointRollupPeriodValue(generator, info.getRollup()));
        }
    }
    
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        
        if(info.isSingleArray() && voMap.size() > 1) {
            //Fast forward to end to fill any gaps at the end
            for(DataPointStatisticsQuantizer<?> quant : this.quantizerMap.values())
                if(!quant.isDone())
                    quant.done();
            Iterator<Long> it = this.periodStats.keySet().iterator();
            while(it.hasNext()) {
                List<DataPointValueTime> entries = this.periodStats.get(it.next());
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
