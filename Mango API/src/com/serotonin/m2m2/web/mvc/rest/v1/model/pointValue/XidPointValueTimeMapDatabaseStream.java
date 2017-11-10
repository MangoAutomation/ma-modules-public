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
import com.serotonin.m2m2.db.dao.PointValueDao;
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
public class XidPointValueTimeMapDatabaseStream implements ObjectStream<Map<String, List<PointValueTime>>>{
	
	private final Log LOG = LogFactory.getLog(XidPointValueTimeMapDatabaseStream.class);

	private String host;
	private int port;
	private boolean useRendered;
	private boolean unitConversion;
	private long from;
	private long to;
	private PointValueDao dao;
	private final Map<Integer,DataPointVO> pointMap;
	private final Integer limit;
	private final String dateTimeFormat;
	private final String timezone;
	

	/**
	 * 
	 * @param host
	 * @param port
	 * @param pointMap
	 * @param useRendered
	 * @param unitConversion
	 * @param from
	 * @param to
	 * @param dao
	 * @param limit
	 * @param dateTimeFormat - format for String dates or null for timestamp numbers
	 */
	public XidPointValueTimeMapDatabaseStream(String host, int port, Map<Integer,DataPointVO> pointMap, boolean useRendered,  boolean unitConversion, long from, long to, PointValueDao dao, Integer limit, String dateTimeFormat, String timezone) {
		this.host = host;
		this.port = port;
		this.pointMap = pointMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.from = from;
		this.to = to;
		this.dao = dao;
		this.limit = limit;
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
			DataPointVO vo = this.pointMap.get(it.next());
			try {
				jgen.writeArrayFieldStart(vo.getXid());
				PointValueTimeJsonStreamCallback callback = new PointValueTimeJsonStreamCallback(host, port, jgen, vo, useRendered, unitConversion, limit, dateTimeFormat, timezone);
				this.dao.getPointValuesBetween(vo.getId(), from, to, callback);
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
			PointValueTimeCsvStreamCallback callback = new PointValueTimeCsvStreamCallback(host, port, writer.getWriter(), vo, useRendered, unitConversion, true, writeHeaders, limit, dateTimeFormat, timezone);
			this.dao.getPointValuesBetween(vo.getId(), from, to, callback);
			writeHeaders = false;
		}
	}
}
