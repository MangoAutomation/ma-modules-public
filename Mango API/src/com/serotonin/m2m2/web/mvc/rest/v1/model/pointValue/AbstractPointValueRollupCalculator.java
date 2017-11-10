/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NonNumericPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NonNumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NumericPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.NumericPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * 
 * Base class to process rollups for point values
 * 
 * @author Terry Packer
 */
public abstract class AbstractPointValueRollupCalculator<T> {

	private static final Log LOG = LogFactory.getLog(AbstractPointValueRollupCalculator.class);
	
	protected final String host;
	protected final int port;
	protected final boolean useRendered;
	protected final boolean unitConversion;
	protected RollupEnum rollup;
	protected final TimePeriod period;
	protected DateTime from;
	protected DateTime to;
	protected final Integer limit;
	protected final PointValueDao pvd;
	protected final String dateTimeFormat;
	protected final String timezone;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 * @param period
	 * @param from
	 * @param to
	 * @param limit
	 * @param dateTimeFormat - output string date format, if null then epoch millis number
	 * @param timezone
	 */
	public AbstractPointValueRollupCalculator(String host, int port, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, DateTime from, DateTime to, Integer limit, String dateTimeFormat, String timezone){
        this.host = host;
        this.port = port;
        this.useRendered = useRendered;
        this.unitConversion = unitConversion;
        this.rollup = rollup;
        this.period = period;
        this.from = from;
        this.to = to;
        this.limit = limit;
        this.pvd = Common.databaseProxy.newPointValueDao();
        this.dateTimeFormat = dateTimeFormat;
        this.timezone = timezone;
    }

	protected abstract void generateStream(DateTime from, DateTime to, JsonGenerator jgen) throws IOException;
	protected abstract void generateStream(DateTime from, DateTime to, CSVPojoWriter<T> writer) throws IOException;

	/**
	 * Return the start/end times based on the data point(s) used
	 *   only called if from == null
	 * @return
	 */
	protected abstract LongPair getStartEndTimes();

	
	/**
	 * Call by api to stream JSON
	 * @param jgen
	 * @throws IOException 
	 */
	public void streamData(JsonGenerator jgen) throws IOException {
		this.setupDates();
		this.generateStream(from, to, jgen);
	}
	
	/**
	 * Call by api to stream CSV
	 * @param writer
	 * @throws IOException
	 */
	public void streamData(CSVPojoWriter<T> writer)throws IOException {
		this.setupDates();
		this.generateStream(from, to, writer);
	}

	
	/**
	 * Get the value at the start of the period or if there isn't one then return the closest value.
	 * @param dataPointId
	 * @return
	 */
	protected DataValue getStartValue(int dataPointId){
        PointValueTime startPvt = pvd.getPointValueAt(dataPointId, from.getMillis());
        if(startPvt == null)
        	startPvt = pvd.getPointValueBefore(dataPointId, from.getMillis());
        DataValue startValue = PointValueTime.getValue(startPvt);
        return startValue;
	}
	
	/**
	 * Create the proper quantizer for json generation
	 * @param startValue
	 * @param bc
	 * @param jgen
	 * @return
	 */
	protected AbstractDataQuantizer createQuantizer(DataPointVO vo, DataValue startValue, BucketCalculator bc, JsonGenerator jgen){
        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            return new AnalogStatisticsQuantizer(bc, 
            		startValue,
            		new NumericPointValueStatisticsQuantizerJsonCallback(host, port, jgen, vo, this.useRendered, this.unitConversion, this.rollup, this.limit, this.dateTimeFormat, this.timezone));
        }else {
            if (!rollup.nonNumericSupport()) {
                LOG.warn("Invalid non-numeric rollup type: " + rollup);
                rollup = RollupEnum.FIRST; //Default to first
            }
            return new ValueChangeCounterQuantizer(bc, startValue,
            		new NonNumericPointValueStatisticsQuantizerJsonCallback(host, port, jgen, vo, useRendered, unitConversion, this.rollup, this.limit, this.dateTimeFormat, this.timezone));
        }
	}
	
	/**
	 * Create the proper quantizer for csv generation
	 * @param vo
	 * @param startValue
	 * @param bc
	 * @param writer
	 * @return
	 */
	protected AbstractDataQuantizer createQuantizer(DataPointVO vo, DataValue startValue, 
			BucketCalculator bc, CSVPojoWriter<T> writer, boolean writeXidColumn,
			boolean writeHeaders){
        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
            return new AnalogStatisticsQuantizer(bc, 
            		startValue,
            		new NumericPointValueStatisticsQuantizerCsvCallback(host, port, writer.getWriter(), vo, this.useRendered, this.unitConversion, this.rollup, writeXidColumn, writeHeaders, this.limit, this.dateTimeFormat, this.timezone));
        }else {
            if (!rollup.nonNumericSupport()) {
                LOG.warn("Invalid non-numeric rollup type: " + rollup);
                rollup = RollupEnum.FIRST; //Default to first
            }
            return new ValueChangeCounterQuantizer(bc, startValue,
            		new NonNumericPointValueStatisticsQuantizerCsvCallback(host, port, writer.getWriter(), vo, useRendered, unitConversion, this.rollup, writeXidColumn, writeHeaders, this.limit, this.dateTimeFormat, this.timezone));
        }
    }
	
	/**
	 * Create a Bucket Calculator
	 * @return
	 */
	protected BucketCalculator getBucketCalculator(DateTime startTime, DateTime endTime){
		
        if(this.period == null){
            return  new BucketsBucketCalculator(startTime, endTime, 1);
        }else{
        	    return new TimePeriodBucketCalculator(startTime, endTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        }
	}
	
	/**
	 * Process the data through the quantizer for one data point
	 *
	 * @param quantizer
	 * @param dataPointId
	 * @param from
	 * @param to
	 */
	protected void calculate(final AbstractDataQuantizer quantizer, int dataPointId, DateTime from, DateTime to){
		
        //Make the call to get the data and quantize it
        pvd.getPointValuesBetween(dataPointId, from.getMillis(), to.getMillis(),
                new MappedRowCallback<PointValueTime>() {
                    @Override
                    public void row(PointValueTime pvt, int row) {
                        quantizer.data(pvt);
                    }
                });
        
        quantizer.done();
        
        return;
	}
	
    /**
     * Round off the period for rollups and ensure the date bounds are set
     */
    private void setupDates() {
        // Determine the start and end times.
        if (from == null) {
            // Get the start and end from the point values table.
            LongPair lp = getStartEndTimes();
            from = new DateTime(lp.getL1());
            if (to == null)
                to = new DateTime(lp.getL2());
        } else if (to == null) {
            to = new DateTime();
        }

        // Round off the period if we are using periodic rollup
        if (period != null) {
            from = DateUtils.truncateDateTime(from,
                    TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
            to = DateUtils.truncateDateTime(to, TimePeriodType.convertFrom(this.period.getType()),
                    this.period.getPeriods());
        }
    }
}
