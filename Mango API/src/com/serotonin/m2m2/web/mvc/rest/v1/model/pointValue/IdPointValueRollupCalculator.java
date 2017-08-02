/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.IdPointValueStatisticsQuantizerCsvCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.IdPointValueStatisticsQuantizerJsonCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.AnalogStatisticsChildQuantizer;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentDataQuantizer;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ValueChangeCounterChildQuantizer;

/**
 * @author Terry Packer
 *
 */
public class IdPointValueRollupCalculator extends AbstractPointValueRollupCalculator<PointValueTimeModel> implements QueryArrayStream<PointValueTimeModel>{

	private final Map<Integer, DataPointVO> voMap;
	
	public IdPointValueRollupCalculator(String host, int port, Map<Integer, DataPointVO> voMap, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, DateTime from, DateTime to, Integer limit){
        super(host, port, useRendered, unitConversion, rollup, period, from, to, limit);
		this.voMap = voMap;
	}

	
	/**
	 * Calculate statistics, if TimePeriod is null the entire range will be used
	 * @return
	 */
	public void calculate(final ParentDataQuantizer quantizer, DateTime from, DateTime to){
		
        //Make the call to get the data and quantize it
        DaoRegistry.pointValueDao.getPointValuesBetween(new ArrayList<Integer>(this.voMap.keySet()), from.getMillis(), to.getMillis(),
                new MappedRowCallback<IdPointValueTime>() {
                    @Override
                    public void row(IdPointValueTime pvt, int row) {
                        quantizer.data(pvt.getId(), pvt.getValue(), pvt.getTime());
                    }
                });
        quantizer.done();
        return;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.AbstractPointValueRollupCalculator#generateStream(org.joda.time.DateTime, org.joda.time.DateTime, com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	protected void generateStream(DateTime from, DateTime to, JsonGenerator jgen) {

        BucketCalculator bc = this.getBucketCalculator(from, to);
        IdPointValueStatisticsQuantizerJsonCallback callback = new IdPointValueStatisticsQuantizerJsonCallback(this.host, this.port, jgen, 
        		this.voMap, this.useRendered,
				this.unitConversion, this.rollup, this.limit);

		Iterator<Integer> it = this.voMap.keySet().iterator();
		ParentDataQuantizer quantizer = new ParentDataQuantizer(bc, callback);
		
		while(it.hasNext()){
			DataPointVO vo = this.voMap.get(it.next());
			DataValue startValue = this.getStartValue(vo.getId());
			
	        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
	            quantizer.startQuantizer(vo.getId(), startValue, new AnalogStatisticsChildQuantizer(vo.getId(), quantizer));
	        }else {
	            quantizer.startQuantizer(vo.getId(), startValue, new ValueChangeCounterChildQuantizer(vo.getId(), quantizer));
	        }
		}
		this.calculate(quantizer, from, to);
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.AbstractPointValueRollupCalculator#generateStream(org.joda.time.DateTime, org.joda.time.DateTime, com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	protected void generateStream(DateTime from, DateTime to, CSVPojoWriter<PointValueTimeModel> writer) {
        BucketCalculator bc = this.getBucketCalculator(from, to);
        IdPointValueStatisticsQuantizerCsvCallback callback = new IdPointValueStatisticsQuantizerCsvCallback(this.host, this.port, writer.getWriter(), 
        		this.voMap, this.useRendered,
				this.unitConversion, this.rollup);

			Iterator<Integer> it = this.voMap.keySet().iterator();
			ParentDataQuantizer quantizer = new ParentDataQuantizer(bc, callback);
			while(it.hasNext()){
				DataPointVO vo = this.voMap.get(it.next());
				DataValue startValue = this.getStartValue(vo.getId());
		        if (vo.getPointLocator().getDataTypeId() == DataTypes.NUMERIC) {
		            quantizer.startQuantizer(vo.getId(), startValue,
		            		new AnalogStatisticsChildQuantizer(vo.getId(), quantizer));
		        }else{
		            quantizer.startQuantizer(vo.getId(),startValue, 
		            		new ValueChangeCounterChildQuantizer(vo.getId(), quantizer));
		        }
			}
			this.calculate(quantizer, from, to);
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.AbstractPointValueRollupCalculator#getStartEndTimes()
	 */
	@Override
	protected LongPair getStartEndTimes() {
		return  DaoRegistry.pointValueDao.getStartAndEndTime(new ArrayList<Integer>(this.voMap.keySet()));
	}
	
}
