/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.BucketsBucketCalculator;
import com.infiniteautomation.mango.quantize.TimePeriodBucketCalculator;
import com.infiniteautomation.mango.rest.latest.model.pointValue.DataPointValueTime;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriodType;
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
    public void firstValue(IdPointValueTime value, boolean bookend) {
        try {
            DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
            if(!info.isSingleArray())
                writer.writeStartArray(quantizer.vo.getXid());
            updateQuantizers(value);
            quantizer.firstValue(value, bookend);
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void accept(IdPointValueTime value) {
        try {
            updateQuantizers(value);

            if(info.isSingleArray() && voMap.size() > 1) {
                //Possibly fast forward as samples come in time order and we will not receive another value at this timestamp
                //this will keep our periodStats to a minimum
                if(lastTime != null && value.getTime() != lastTime) {
                    //Finish by forwarding to the point value time
                    Iterator<Integer> it = this.currentValueTimeMap.keySet().iterator();
                    while(it.hasNext()) {
                        Integer id = it.next();
                        DataPointStatisticsQuantizer<?> q = this.quantizerMap.get(id);
                        IdPointValueTimeRow row = this.currentValueTimeMap.get(id);
                        it.remove();
                        if(row == null) {
                            //No values in this sample period
                            q.fastForward(lastTime);
                        }else {
                            q.accept(row.value);
                        }
                    }
                    currentValueTimeMap.put(value.getSeriesId(), new IdPointValueTimeRow(value));
                }else {
                    //cache the value so as not to trigger quantization until all values are ready
                    currentValueTimeMap.put(value.getSeriesId(), new IdPointValueTimeRow(value));
                }

                lastTime = value.getTime();
            }else {
                DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
                quantizer.accept(value);
            }
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void lastValue(IdPointValueTime value, boolean bookend) {
        try {
            DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getSeriesId());
            IdPointValueTimeRow row = this.currentValueTimeMap.remove(value.getSeriesId());
            if(row != null) {
                quantizer.accept(row.value);
            }
            quantizer.lastValue(value, bookend);
            //This will definitely be the last time we see this point
            if(!info.isSingleArray()) {
                quantizer.done();
                writer.writeEndArray();
            }
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void streamData(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        createQuantizerMap();
        Collection<? extends DataPointVO> vos = new ArrayList<>(voMap.values());
        if (info.isSingleArray()) {
            dao.wideBookendQueryCombined(vos, info.getFromMillis(), info.getToMillis(), null, this);
        } else {
            dao.wideBookendQueryPerPoint(vos, info.getFromMillis(), info.getToMillis(), null, this);
        }
    }

    protected void writePeriodStats(List<DataPointValueTime> generators) throws QueryCancelledException {
        //Code limit
        try {
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
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    @Override
    public void quantizedStatistics(DataPointStatisticsGenerator generator) throws QueryCancelledException {
        //Collect the stats for this period
        if(info.isSingleArray() && voMap.size() > 1) {
            //Do we have any entries for this period
            List<DataPointValueTime> entries = this.periodStats.get(generator.getGenerator().getPeriodStartTime());
            if(entries == null) {
                entries = new ArrayList<>();
                this.periodStats.put(generator.getGenerator().getPeriodStartTime(), entries);
            }
            entries.add(new DataPointRollupPeriodValue(generator, getRollup(generator.getVo())));
            if(entries.size() == voMap.size()) {
                this.periodStats.remove(generator.getGenerator().getPeriodStartTime());
                writePeriodStats(entries);
                this.lastFullPeriodToMillis = generator.getGenerator().getPeriodEndTime();
            }
        }else {
            //Just write it out
            writePeriodStats(new DataPointRollupPeriodValue(generator, getRollup(generator.getVo())));
        }
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException {

        if(info.isSingleArray() && voMap.size() > 1) {

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
                if(!quant.isDone()) {
                    quant.done();
                }
            }

            //For any with 0 data TODO Check for is open?
            for(DataPointStatisticsQuantizer<?> q : this.quantizerMap.values()) {
                if(!q.isDone()) {
                    q.done();
                }
            }
        }
        super.finish(writer);
    }

    /**
     * Override as necessary, return rollup for this data point
     * @param vo
     * @return
     */
    protected RollupEnum getRollup(DataPointVO vo) {
        return info.getRollup();
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

        public IdPointValueTimeRow(IdPointValueTime value) {
            this.value = value;
        }

    }
}
