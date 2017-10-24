/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.AbstractDataQuantizer;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;

/**
 * Compute a Rollup Map for multiple data points of the form:
 * 
 * xid1 : [rolled up values],
 * xid2 : [rolled up values]
 * 
 * 
 * @author Terry Packer
 *
 */
public class XidPointValueMapRollupCalculator extends AbstractPointValueRollupCalculator<Map<String, List<PointValueTime>>> implements ObjectStream<Map<String, List<PointValueTime>>>{

	private final Map<Integer, DataPointVO> voMap;
	
	public XidPointValueMapRollupCalculator(String host, int port, Map<Integer, DataPointVO> voMap, boolean useRendered,  boolean unitConversion, RollupEnum rollup, TimePeriod period, DateTime from, DateTime to, Integer limit){
        super(host, port, useRendered, unitConversion, rollup, period, from, to, limit);
		this.voMap = voMap;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	protected void generateStream(DateTime from, DateTime to, JsonGenerator jgen) throws IOException {
		Iterator<Integer> it = this.voMap.keySet().iterator();
		while(it.hasNext()){
			DataPointVO vo = this.voMap.get(it.next());
			jgen.writeArrayFieldStart(vo.getXid());
			
			DataValue startValue = this.getStartValue(vo.getId());
	        BucketCalculator bc = this.getBucketCalculator(from, to);
	        
	        final AbstractDataQuantizer quantizer = createQuantizer(vo, startValue, bc, jgen);
			this.calculate(quantizer, vo.getId(), from, to);

			jgen.writeEndArray();
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	protected void generateStream(DateTime from, DateTime to, CSVPojoWriter<Map<String, List<PointValueTime>>> writer){

		Iterator<Integer> it = this.voMap.keySet().iterator();
		boolean writeHeaders = true;
		
		while(it.hasNext()){
			DataPointVO vo = this.voMap.get(it.next());
			DataValue startValue = this.getStartValue(vo.getId());

	        BucketCalculator bc = this.getBucketCalculator(from, to);
	        
	        final AbstractDataQuantizer quantizer = createQuantizer(vo, startValue, bc, writer, true, writeHeaders);
			this.calculate(quantizer, vo.getId(), from, to);
			
			//Only write the headers on the first iteration
			writeHeaders = false;
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.AbstractPointValueRollupCalculator#getStartEndTimes()
	 */
	@Override
	protected LongPair getStartEndTimes() {
		return pvd.getStartAndEndTime(new ArrayList<Integer>(this.voMap.keySet()));
	}
}
