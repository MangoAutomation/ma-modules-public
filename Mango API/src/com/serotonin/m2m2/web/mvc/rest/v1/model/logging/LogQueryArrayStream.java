/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.logging;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.QueryModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

/**
 * @author Terry Packer
 *
 */
public class LogQueryArrayStream implements QueryArrayStream<LogMessageModel>{

	private String filename;
	private QueryModel query;
	
	public LogQueryArrayStream(String filename, QueryModel query){
		this.filename = filename;
		this.query = query;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {

		MangoLogFilePatternReceiver receiver = new MangoLogFilePatternReceiver(query, jgen);

		try {
			File logsDir = Common.getLogsDir();
			String logsPath = logsDir.getAbsolutePath() + "/";

		    receiver.setLogFormat("LEVEL TIMESTAMP (CLASS.METHOD:LINE) - MESSAGE"); //"%-5p %d{ISO8601} (%C.%M:%L) - %m %n"
		    receiver.setFileURL("file://" + logsPath + filename);
		    receiver.setUseCurrentThread(true);

		    //Start the parsing
		    receiver.activateOptions();
	    } finally {
	        receiver.shutdown();
	    }
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<LogMessageModel> writer)
			throws IOException {
		//No Op For now
	}

}
