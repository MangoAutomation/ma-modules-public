/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.util.Collections;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.AbstractDataQuantizer;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;

/**
 * @author Terry Packer
 *
 */
public class PointValueRollupCalculator extends AbstractPointValueRollupCalculator<PointValueTimeModel> implements QueryArrayStream<PointValueTimeModel>{

	private DataPointVO vo;

	public PointValueRollupCalculator(String host, int port, DataPointVO vo, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, DateTime from, DateTime to, Integer limit){
        super(host, port, useRendered, unitConversion, rollup, period, from, to, limit);
        this.vo = vo;
    }


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {

		DataValue startValue = this.getStartValue(vo.getId());
        BucketCalculator bc = this.getBucketCalculator(from, to);
        
        final AbstractDataQuantizer quantizer = createQuantizer(vo, startValue, bc, jgen);
		this.calculate(quantizer, vo.getId(), from, to);
	}

	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.AbstractPointValueRollupCalculator#generateStream(org.joda.time.DateTime, org.joda.time.DateTime, com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	protected void generateStream(DateTime from, DateTime to, JsonGenerator jgen) {
		DataValue startValue = this.getStartValue(vo.getId());
        BucketCalculator bc = this.getBucketCalculator(from, to);
        
        final AbstractDataQuantizer quantizer = createQuantizer(vo, startValue, bc, jgen);
		this.calculate(quantizer, vo.getId(), from, to);
	}


	@Override
	public void generateStream(DateTime from, DateTime to, CSVPojoWriter<PointValueTimeModel> writer){

		DataValue startValue = this.getStartValue(vo.getId());
		BucketCalculator bc = this.getBucketCalculator(from, to);
        
        final AbstractDataQuantizer quantizer = createQuantizer(vo, startValue, bc, writer, false, true);
		this.calculate(quantizer, vo.getId(), from, to);
	}



	@Override
	protected LongPair getStartEndTimes(){
		return DaoRegistry.pointValueDao.getStartAndEndTime(Collections.singletonList(vo.getId()));
	}

}
