/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;

/**
 * Stream point values from multiple points packaged into a map of point xids to PointValueTime arrays
 * 
 * CSV Format is point per column 
 * 
 * @author Terry Packer
 *
 */
public class XidPointValueTimeLatestPointFacadeStream implements ObjectStream<Map<String, List<PointValueTime>>>{
	
	private final Log LOG = LogFactory.getLog(XidPointValueTimeLatestPointFacadeStream.class);

	private String host;
	private int port;
	private boolean useRendered;
	private boolean unitConversion;
	private int limit;
	private boolean useCache;
	private final Map<Integer,DataPointVO> pointMap;
	private final String dateTimeFormat;
	private final String timezone;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param pointMap
	 * @param useRendered
	 * @param unitConversion
	 * @param limit
	 * @param useCache
	 * @param dateTimeFormat - format for string dates, if null then epoch millis number
	 * @param timezone
	 */
	public XidPointValueTimeLatestPointFacadeStream(String host, int port, Map<Integer,DataPointVO> pointMap, boolean useRendered,  boolean unitConversion, int limit, boolean useCache, String dateTimeFormat, String timezone) {
		this.host = host;
		this.port = port;
		this.pointMap = pointMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.limit = limit;
		this.useCache = useCache;
		this.dateTimeFormat = dateTimeFormat;
		this.timezone = timezone;

	}

	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		Iterator<Integer> it = this.pointMap.keySet().iterator();
		while(it.hasNext()){
			try {
				DataPointVO vo = this.pointMap.get(it.next());
				PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), useCache);
				PointValueTimeJsonStreamCallback callback = new PointValueTimeJsonStreamCallback(host, port, jgen, vo, useRendered, unitConversion, null, dateTimeFormat, timezone);

				jgen.writeArrayFieldStart(vo.getXid());
				List<PointValueTime> pvts = pointValueFacade.getLatestPointValues(limit);
				for(int i=0; i<pvts.size(); i++)
					callback.row(pvts.get(i), i);
				jgen.writeEndArray();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<Map<String, List<PointValueTime>>> writer)
			throws IOException {
		Iterator<Integer> it = this.pointMap.keySet().iterator();
		boolean writeHeaders = true;
		while(it.hasNext()){
			DataPointVO vo = this.pointMap.get(it.next());
			PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), useCache);
			PointValueTimeCsvStreamCallback callback = new PointValueTimeCsvStreamCallback(host, port, writer.getWriter(), vo, useRendered, unitConversion, true, writeHeaders, null, dateTimeFormat, timezone);
			List<PointValueTime> pvts = pointValueFacade.getLatestPointValues(limit);
			for(int i=0; i<pvts.size(); i++)
				callback.row(pvts.get(i), i);
			writeHeaders = false;
		}
	}
}
