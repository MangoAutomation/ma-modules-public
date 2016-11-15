/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Stream Point Values in 2 formats:
 * 
 * JSON:multiple values per time entry in one large array
 * 
 * CSV: 1 point per column
 * 
 * 
 * @author Terry Packer
 *
 */
public class IdPointValueTimeLatestPointValueFacadeStream implements QueryArrayStream<PointValueTimeModel>{

	private String host;
	private int port;
	private boolean useRendered;
	private boolean unitConversion;
	private int limit;
	private boolean useCache;
	private final Map<Integer,DataPointVO> pointMap;
	
	/**
	 * @param id
	 * @param from
	 * @param to
	 */
	public IdPointValueTimeLatestPointValueFacadeStream(String host, int port, Map<Integer,DataPointVO> pointMap, boolean useRendered,  boolean unitConversion, int limit, boolean useCache) {
		this.host = host;
		this.port = port;
		this.pointMap = pointMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.limit = limit;
		this.useCache = useCache;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		IdPointValueTimeJsonStreamCallback callback = new IdPointValueTimeJsonStreamCallback(host, port, jgen, pointMap, useRendered, unitConversion);
		//Sadly in this scenario we must collect all the data and then order it
		List<IdPointValueTime> ipvts = new ArrayList<IdPointValueTime>();
		Iterator<Integer> it = this.pointMap.keySet().iterator();
		while(it.hasNext()){
			DataPointVO vo = this.pointMap.get(it.next());
			PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), useCache);

			List<PointValueTime> pvts = pointValueFacade.getLatestPointValues(limit);
			for(PointValueTime pvt : pvts)
				ipvts.add(new IdPointValueTime(vo.getId(), pvt.getValue(), pvt.getTime()));
		}

		//Sort it all
		Collections.sort(ipvts, new Comparator<IdPointValueTime>(){
			@Override
			public int compare(IdPointValueTime o1, IdPointValueTime o2) {
				return Long.compare(o1.getTime(), o2.getTime());
			}
		});
		for(int i=0; i<ipvts.size(); i++)
			callback.row(ipvts.get(i), i);
		callback.finish();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		IdPointValueTimeCsvStreamCallback callback = new IdPointValueTimeCsvStreamCallback(host, port, writer.getWriter(), pointMap, useRendered, unitConversion);
		//Sadly in this scenario we must collect all the data and then order it
		List<IdPointValueTime> ipvts = new ArrayList<IdPointValueTime>();
		Iterator<Integer> it = this.pointMap.keySet().iterator();
		while(it.hasNext()){
			DataPointVO vo = this.pointMap.get(it.next());
			PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), useCache);

			List<PointValueTime> pvts = pointValueFacade.getLatestPointValues(limit);
			for(PointValueTime pvt : pvts)
				ipvts.add(new IdPointValueTime(vo.getId(), pvt.getValue(), pvt.getTime()));
		}

		//Sort it all
		Collections.sort(ipvts, new Comparator<IdPointValueTime>(){
			@Override
			public int compare(IdPointValueTime o1, IdPointValueTime o2) {
				return Long.compare(o1.getTime(), o2.getTime());
			}
		});
		for(int i=0; i<ipvts.size(); i++)
			callback.row(ipvts.get(i), i);
		callback.finish();
	}

}
