/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
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
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NonNumericPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NonNumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NumericPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class PointValueRollupCalculator implements QueryArrayStream<PointValueTimeModel>{

	private static final Log LOG = LogFactory.getLog(PointValueRollupCalculator.class);
	
	private String host;
	private int port;
	private DataPointVO vo;
	private boolean useRendered;
	private boolean unitConversion;
	private RollupEnum rollup;
	private TimePeriod period;
	private DateTime from;
	private DateTime to;

	public PointValueRollupCalculator(String host, int port, DataPointVO vo, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, DateTime from, DateTime to){
        this.host = host;
        this.port = port;
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
	public void calculate(final AbstractDataQuantizer quantizer, DateTime from, DateTime to){
		
        //Make the call to get the data and quantize it
        DaoRegistry.pointValueDao.getPointValuesBetween(vo.getId(), from.getMillis(), to.getMillis(),
                new MappedRowCallback<PointValueTime>() {
                    @Override
                    public void row(PointValueTime pvt, int row) {
                        quantizer.data(pvt);
                    }
                });
        
        quantizer.done(getEndValue());
        
        return;
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		
		this.setupDates();

		DataValue startValue = this.getStartValue();
		
		DateTime startTime = this.getStartTime();
		DateTime endTime = this.getEndTime();
        BucketCalculator bc = this.getBucketCalculator(startTime, endTime);
        
        final AbstractDataQuantizer quantizer;
        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            quantizer = new AnalogStatisticsQuantizer(bc, 
            		startValue,
            		new NumericPointValueStatisticsQuantizerJsonCallback(host, port, jgen, this.vo, this.useRendered, this.unitConversion, this.rollup));
        }else {
            if (!rollup.nonNumericSupport()) {
                LOG.warn("Invalid non-numeric rollup type: " + rollup);
                rollup = RollupEnum.FIRST; //Default to first
            }
            quantizer = new ValueChangeCounterQuantizer(bc, startValue,
            		new NonNumericPointValueStatisticsQuantizerJsonCallback(host, port, jgen, vo, useRendered, unitConversion, this.rollup));
        }
		
		this.calculate(quantizer, startTime, endTime);
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		this.setupDates();

		DataValue startValue = this.getStartValue();

		DateTime startTime = this.getStartTime();
		DateTime endTime = this.getEndTime();
        BucketCalculator bc = this.getBucketCalculator(startTime, endTime);
        
        final AbstractDataQuantizer quantizer;
        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            quantizer = new AnalogStatisticsQuantizer(bc, 
            		startValue,
            		new NumericPointValueStatisticsQuantizerCsvCallback(host, port, writer.getWriter(), this.vo, this.useRendered, this.unitConversion, this.rollup));
        }else {
            if (!rollup.nonNumericSupport()) {
                LOG.warn("Invalid non-numeric rollup type: " + rollup);
                rollup = RollupEnum.FIRST; //Default to first
            }
            quantizer = new ValueChangeCounterQuantizer(bc, startValue,
            		new NonNumericPointValueStatisticsQuantizerCsvCallback(host, port, writer.getWriter(), vo, useRendered, unitConversion, this.rollup));
        }
		
		this.calculate(quantizer, startTime, endTime);
	}

	private void setupDates(){
        // Determine the start and end times.
        if (from == null) {
            // Get the start and end from the point values table.
            LongPair lp = DaoRegistry.pointValueDao.getStartAndEndTime(Collections.singletonList(vo.getId()));
            from = new DateTime(lp.getL1());
            to = new DateTime(lp.getL2());
        }

	}
	
	/**
	 * Create a Bucket Calculator
	 * @return
	 */
	private BucketCalculator getBucketCalculator(DateTime startTime, DateTime endTime){
		
        if(this.period == null){
        	return  new BucketsBucketCalculator(startTime, endTime, 1);
        }else{
        	return new TimePeriodBucketCalculator(startTime, endTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        }
	}
	
	private DateTime getStartTime(){
		DateTime startTime = from;
		 //Round off the start period if we are using periodic rollup
        if(period != null)
        	startTime = DateUtils.truncateDateTime(startTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        return startTime;

	}
	private DateTime getEndTime(){
        return to;
	}
	private DataValue getStartValue(){
        // Determine the start and end values. This is important for
        // properly calculating average.
        PointValueTime startPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), from.getMillis());
        //Try our best to get the closest value
        if(startPvt == null)
        	startPvt = DaoRegistry.pointValueDao.getPointValueBefore(vo.getId(), from.getMillis());
        DataValue startValue = PointValueTime.getValue(startPvt);
        return startValue;
	}
	
	private DataValue getEndValue(){
		PointValueTime endPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), to.getMillis());
        return PointValueTime.getValue(endPvt);
	}
	
}
