/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.view.quantize2.BucketsBucketCalculator;
import com.serotonin.m2m2.view.quantize2.TimePeriodBucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.IdPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.IdPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.AnalogStatisticsChildQuantizer;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentDataQuantizer;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ValueChangeCounterChildQuantizer;

/**
 * @author Terry Packer
 *
 */
public class IdPointValueRollupCalculator implements QueryArrayStream<PointValueTimeModel>{

	private static final Log LOG = LogFactory.getLog(IdPointValueRollupCalculator.class);
	
	private String host;
	private int port;
	private Map<Integer, DataPointVO> voMap;
	private boolean useRendered;
	private boolean unitConversion;
	private RollupEnum rollup;
	private TimePeriod period;
	private long from;
	private long to;
	
	public IdPointValueRollupCalculator(String host, int port, Map<Integer, DataPointVO> voMap, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, long from, long to){
		this.host = host;
		this.port = port;
		this.voMap = voMap;
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
	public void calculate(final ParentDataQuantizer quantizer, DateTime from, DateTime to, Map<Integer, DataValue> endValues){
		
        //Make the call to get the data and quantize it
        DaoRegistry.pointValueDao.getPointValuesBetween(new ArrayList<Integer>(this.voMap.keySet()), from.getMillis(), to.getMillis(),
                new MappedRowCallback<IdPointValueTime>() {
                    @Override
                    public void row(IdPointValueTime pvt, int row) {
                        quantizer.data(pvt.getDataPointId(), pvt.getValue(), pvt.getTime());
                    }
                });
        quantizer.done(endValues);
        return;
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		this.setupDates();
		DateTime startTime = this.getStartTime();
		DateTime endTime = this.getEndTime();
        BucketCalculator bc = this.getBucketCalculator(startTime, endTime);
        IdPointValueStatisticsQuantizerJsonCallback callback = new IdPointValueStatisticsQuantizerJsonCallback(this.host, this.port, jgen, 
        		this.voMap, this.useRendered,
				this.unitConversion, this.rollup);
		try {
			//Time ordered list of end values
			Map<Integer, DataValue> endValues = new HashMap<Integer, DataValue>(this.voMap.size());
			Iterator<Integer> it = this.voMap.keySet().iterator();
			ParentDataQuantizer quantizer = new ParentDataQuantizer(bc, callback);
			
			while(it.hasNext()){
				DataPointVO vo = this.voMap.get(it.next());
				DataValue startValue = this.getStartValue(vo);
				
		        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
		            quantizer.startQuantizer(vo.getId(), startValue, new AnalogStatisticsChildQuantizer(vo.getId(), quantizer));
		        }else {
		            quantizer.startQuantizer(vo.getId(), startValue, new ValueChangeCounterChildQuantizer(vo.getId(), quantizer));
		        }
		        endValues.put(vo.getId(), getEndValue(vo));
			}
			this.calculate(quantizer, startTime, endTime, endValues);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		this.setupDates();
		DateTime startTime = this.getStartTime();
		DateTime endTime = this.getEndTime();
        BucketCalculator bc = this.getBucketCalculator(startTime, endTime);
        IdPointValueStatisticsQuantizerCsvCallback callback = new IdPointValueStatisticsQuantizerCsvCallback(this.host, this.port, writer.getWriter(), 
        		this.voMap, this.useRendered,
				this.unitConversion, this.rollup);

			//Time ordered list of end values
        	Map<Integer, DataValue> endValues = new HashMap<Integer, DataValue>(this.voMap.size());
			Iterator<Integer> it = this.voMap.keySet().iterator();
			ParentDataQuantizer quantizer = new ParentDataQuantizer(bc, callback);
			while(it.hasNext()){
				DataPointVO vo = this.voMap.get(it.next());
				DataValue startValue = this.getStartValue(vo);
		        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
		            quantizer.startQuantizer(vo.getId(), startValue,
		            		new AnalogStatisticsChildQuantizer(vo.getId(), quantizer));
		        }else{
		            quantizer.startQuantizer(vo.getId(),startValue, 
		            		new ValueChangeCounterChildQuantizer(vo.getId(), quantizer));
		        }
		        endValues.put(vo.getId(), getEndValue(vo));
			}
			this.calculate(quantizer, startTime, endTime, endValues);

	}

	private void setupDates(){
        // Determine the start and end times.
        if (from == -1) {
            // Get the start and end from the point values table.
            LongPair lp = DaoRegistry.pointValueDao.getStartAndEndTime(new ArrayList<Integer>(this.voMap.keySet()));
            from = lp.getL1();
            to = lp.getL2();
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
		DateTime startTime = new DateTime(from);
		 //Round off the start period if we are using periodic rollup
        if(period != null)
        	startTime = DateUtils.truncateDateTime(startTime, TimePeriodType.convertFrom(this.period.getType()), this.period.getPeriods());
        return startTime;

	}
	private DateTime getEndTime(){
        return new DateTime(to);
	}
	
	private DataValue getStartValue(DataPointVO vo){
        // Determine the start and end values. This is important for
        // properly calculating average.
        PointValueTime startPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), from);
        //Try our best to get the closest value
        if(startPvt == null)
        	startPvt = DaoRegistry.pointValueDao.getPointValueBefore(vo.getId(), from);
        DataValue startValue = PointValueTime.getValue(startPvt);
        return startValue;
	}
	
	private DataValue getEndValue(DataPointVO vo){
		PointValueTime endPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), to);
        if(endPvt != null)
        	return endPvt.getValue();
        else
        	return null;
	}
	
}
