/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.rollup;

import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeQueryArrayStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.view.quantize3.BucketCalculator;
import com.serotonin.m2m2.view.quantize3.BucketsBucketCalculator;
import com.serotonin.m2m2.view.quantize3.TimePeriodBucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * Base Class for Rollup Calculation Streams
 *
 * @author Terry Packer
 */
public abstract class PointValueTimeRollupStream<T> extends PointValueTimeQueryArrayStream<T> {

    protected final PointValueDao dao;
    
    public PointValueTimeRollupStream(ZonedDateTimeRangeQueryInfo info, Map<Integer, DataPointVO> voMap, PointValueDao dao) {
        super(info, voMap);
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
}
