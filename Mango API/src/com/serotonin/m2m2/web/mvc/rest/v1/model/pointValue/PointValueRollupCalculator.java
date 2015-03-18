/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.view.quantize2.AbstractDataQuantizer;
import com.serotonin.m2m2.view.quantize2.AnalogStatisticsQuantizer;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.view.quantize2.BucketsBucketCalculator;
import com.serotonin.m2m2.view.quantize2.TimePeriodBucketCalculator;
import com.serotonin.m2m2.view.quantize2.ValueChangeCounterQuantizer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NonNumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class PointValueRollupCalculator implements JsonArrayStream{

	private static final Log LOG = LogFactory.getLog(PointValueRollupCalculator.class);
	
	private DataPointVO vo;
	private boolean useRendered;
	private boolean unitConversion;
	private RollupEnum rollup;
	private TimePeriod period;
	private long from;
	private long to;
	
	public PointValueRollupCalculator(DataPointVO vo, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, long from, long to){
		this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.rollup = rollup;
		this.period = period;
		this.from = from;
		this.to = to;
	}

	
	/**
	 * Calculate statistics, if TimePeriod is null the entire range will be used
	 * @return
	 */
	public void calculate(final JsonGenerator jgen){
		
        // Determine the start and end times.
        if (from == -1) {
            // Get the start and end from the point values table.
            LongPair lp = DaoRegistry.pointValueDao.getStartAndEndTime(Collections.singletonList(vo.getId()));
            from = lp.getL1();
            to = lp.getL2();
        }

        DateTime startTime = new DateTime(from);
        //Round off the start period if we are using periodic rollup
        if(period != null)
        	startTime = DateUtils.truncateDateTime(startTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        DateTime endTime = new DateTime(to);

        // Determine the start and end values. This is important for
        // properly calculating average.
        PointValueTime startPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), from);
        //Try our best to get the closest value
        if(startPvt == null)
        	startPvt = DaoRegistry.pointValueDao.getPointValueBefore(vo.getId(), from);
        DataValue startValue = PointValueTime.getValue(startPvt);
        PointValueTime endPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), to);
        DataValue endValue = PointValueTime.getValue(endPvt);
        
        BucketCalculator bc;
        if(this.period == null){
        	bc = new BucketsBucketCalculator(startTime, endTime, 1);
        }else{
        	bc = new TimePeriodBucketCalculator(startTime, endTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        }
        final AbstractDataQuantizer quantizer;
        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            quantizer = new AnalogStatisticsQuantizer(bc, 
            		startValue,
            		new NumericPointValueStatisticsQuantizerJsonCallback(jgen, this.vo, this.useRendered, this.unitConversion, this.rollup));
        }else {
            if (!rollup.nonNumericSupport()) {
                LOG.warn("Invalid non-numeric rollup type: " + rollup);
                rollup = RollupEnum.FIRST; //Default to first
            }
            quantizer = new ValueChangeCounterQuantizer(bc, startValue,
            		new NonNumericPointValueStatisticsQuantizerJsonCallback(jgen, vo, useRendered, unitConversion, this.rollup));
        }

        //Finally Make the call to get the data and quantize it
        DaoRegistry.pointValueDao.getPointValuesBetween(vo.getId(), from, to,
                new MappedRowCallback<PointValueTime>() {
                    @Override
                    public void row(PointValueTime pvt, int row) {
                        quantizer.data(pvt);
                    }
                });
        quantizer.done(endValue);
        
        
        return;
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		this.calculate(jgen);
	}
	
	
}
