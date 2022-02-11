/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.db.query.WideCallback;
import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.BucketsBucketCalculator;
import com.infiniteautomation.mango.quantize.TemporalAmountBucketCalculator;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.PointValueTimeDatabaseStream;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;


/**
 *
 * @author Terry Packer
 */
public abstract class AbstractMultiDataPointStatisticsQuantizerStream <T, INFO extends ZonedDateTimeRangeQueryInfo> extends PointValueTimeDatabaseStream<T, INFO> implements ChildStatisticsGeneratorCallback, WideCallback<IdPointValueTime> {

    protected final Map<Integer, DataPointStatisticsQuantizer<?>> quantizerMap;
    protected int count;

    //For our quantization
    protected final BucketCalculator bucketCalculator;
    protected Instant periodFrom;
    protected Instant periodTo;
    protected long periodToMillis; //For performance
    protected long currentTime;
    protected int currentDataPointId; //Track point change in order by ID queries

    public AbstractMultiDataPointStatisticsQuantizerStream(INFO info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap, dao);
        this.quantizerMap = new HashMap<>(voMap.size());
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
     * Check limit and maybe write the period stats
     */
    protected void writePeriodStats(DataPointRollupPeriodValue generator) throws QueryCancelledException{
        //Code limit
        try {
            if(info.getLimit() != null && count >= info.getLimit())
                return;
            this.writer.writeDataPointValue(generator);
            count++;
        }catch(IOException e) {
            throw new QueryCancelledException(e);
        }
    }

    /**
     * Track and advance the quantizers to ensure when
     * we change points we finish the quantizer to fill in the periods
     *
     */
    protected void updateQuantizers(IdPointValueTime value) throws IOException, QueryCancelledException {
        long time = value.getTime();
        if(!info.isSingleArray()) {
            //In this query the values are returned in data point ID and time order
            //Advance the previous quantizer
            if(currentDataPointId != -1 && currentDataPointId != value.getSeriesId()) {
                DataPointStatisticsQuantizer<?> quant = this.quantizerMap.get(currentDataPointId);
                if(!quant.isDone())
                    quant.done();
            }
        }
        currentTime = time;
        currentDataPointId = value.getSeriesId();
    }

    protected void createQuantizerMap() {
        for(Entry<Integer, DataPointVO> entry : voMap.entrySet()) {
            DataPointVO vo = entry.getValue();
            DataPointStatisticsQuantizer<?> quantizer;
            if(info.getRollup() == RollupEnum.POINT_DEFAULT && vo.getRollup() == RollupEnum.NONE.getId()) {
                //Raw Data Stream
                quantizer = new NoStatisticsDataPointQuantizer(vo, getBucketCalculator(), this);
            }else {
                switch(vo.getPointLocator().getDataType()) {
                    case ALPHANUMERIC:
                        quantizer = new ValueChangeCounterDataPointQuantizer(vo, getBucketCalculator(), this);
                        break;
                    case BINARY:
                    case MULTISTATE:
                        quantizer = new StartsAndRuntimeListDataPointQuantizer(vo, getBucketCalculator(), this);
                        break;
                    case NUMERIC:
                        quantizer = new AnalogStatisticsDataPointQuantizer(vo, getBucketCalculator(), this);
                        break;
                    default:
                        throw new RuntimeException("Unknown Data Type: " + vo.getPointLocator().getDataType());
                }
            }
            this.quantizerMap.put(entry.getKey(), quantizer);
        }
    }

    /**
     * Create a Bucket Calculator
     */
    protected BucketCalculator getBucketCalculator(){
        if(this.info.getTimePeriod() == null){
            return  new BucketsBucketCalculator(info.getFrom(), info.getTo(), 1);
        }else{
            return new TemporalAmountBucketCalculator(info.getFrom(), info.getTo(), info.getTimePeriod().toTemporalAmount());
        }
    }
}
