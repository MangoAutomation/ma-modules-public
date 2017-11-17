/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeCsvWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeQueryStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.db.WideQueryCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.view.quantize3.BucketCalculator;
import com.serotonin.m2m2.view.quantize3.BucketsBucketCalculator;
import com.serotonin.m2m2.view.quantize3.TimePeriodBucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 *  Container to help map multi point queries produce statistics for each point
 *
 * @author Terry Packer
 */
public class MultiDataPointStatisticsQuantizerStream<T> extends PointValueTimeQueryStream<T> implements WideQueryCallback<IdPointValueTime>{

    protected final Map<Integer, DataPointStatisticsQuantizer<?>> quantizerMap;
    protected final PointValueDao dao;
    
    public MultiDataPointStatisticsQuantizerStream(ZonedDateTimeRangeQueryInfo info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap);
        this.quantizerMap = new HashMap<>(voMap.size());
        this.dao = dao;
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
    
    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#preQuery(java.lang.Object, boolean)
     */
    @Override
    public void preQuery(IdPointValueTime value, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());        
        if(!info.isSingleArray())
            writer.startWriteArray(quantizer.vo.getXid());

        quantizer.preQuery(value, bookend);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#row(java.lang.Object, int)
     */
    @Override
    public void row(IdPointValueTime value, int index) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.row(value, index);
    }

    /* (non-Javadoc)
     * @see com.serotonin.db.WideQueryCallback#postQuery(java.lang.Object, boolean)
     */
    @Override
    public void postQuery(IdPointValueTime value, boolean bookend) throws IOException {
        DataPointStatisticsQuantizer<?> quantizer = this.quantizerMap.get(value.getId());
        quantizer.postQuery(value, bookend);
        
        if(!info.isSingleArray())
            writer.endWriteArray();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
     */
    @Override
    public void streamData(JsonGenerator jgen) throws IOException {
        this.streamType = StreamType.JSON;
        this.writer = new PointValueTimeJsonWriter(info, jgen);
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
     */
    @Override
    public void streamData(CSVPojoWriter<T> writer) throws IOException {
        this.streamType = StreamType.CSV;
        this.writer = new PointValueTimeCsvWriter(info, writer.getWriter());
        createQuantizerMap();
        dao.wideBookendQuery(new ArrayList<Integer>(voMap.keySet()), info.getFromMillis(), info.getToMillis(), !info.isSingleArray(), info.getLimit(), this);
    }
    
    protected void createQuantizerMap() {
        for(Entry<Integer, DataPointVO> entry : voMap.entrySet()) {
            DataPointVO vo = entry.getValue();
            DataPointStatisticsQuantizer<?> quantizer;
            switch(vo.getPointLocator().getDataTypeId()) {
                case DataTypes.ALPHANUMERIC:
                case DataTypes.BINARY:
                case DataTypes.MULTISTATE:
                case DataTypes.IMAGE:
                case DataTypes.NUMERIC:
                    quantizer = new AnalogStatisticsDataPointQuantizer(vo, getBucketCalculator(), new LimitCounter(info.getLimit()), writer);
                break;
                default:
                    throw new RuntimeException("Unknown Data Type: " + vo.getPointLocator().getDataTypeId());
            }
            
            this.quantizerMap.put(entry.getKey(), quantizer);
        }
    }
    
}
