/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeDatabaseStream implements QueryArrayStream<PointValueTimeModel>{

	private String host;
	private int port;
	private DataPointVO vo;
	private boolean useRendered;
	private boolean unitConversion;
	private long from;
	private long to;
	private PointValueDao dao;
	
	/**
	 * @param id
	 * @param from
	 * @param to
	 */
	public PointValueTimeDatabaseStream(String host, int port, DataPointVO vo, boolean useRendered,  boolean unitConversion, long from, long to, PointValueDao dao) {
        this.host = host;
        this.port = port;
        this.vo = vo;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.from = from;
		this.to = to;
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		this.dao.getPointValuesBetween(vo.getId(), from, to, new PointValueTimeJsonStreamCallback(host, port, jgen, vo, useRendered, unitConversion));
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		this.dao.getPointValuesBetween(vo.getId(), from, to, new PointValueTimeCsvStreamCallback(host, port, writer.getWriter(), vo, useRendered, unitConversion, false, false));
	}

}
