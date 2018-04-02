/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.logging;

import org.apache.logging.log4j.Level;

/**
 * 
 * @author Terry Packer
 */
public class LoggingEvent {

	private Level level;
	private long timestamp;
	private String message;
	private String[] stackTrace;
	private String eventFileName;
	private String threadName;



	public LoggingEvent(Level level, long timestamp, String message, String[] stackTrace, 
			String eventFileName, String threadName) {
		this.level = level;
		this.timestamp = timestamp;
		this.message = message;
		this.stackTrace = stackTrace;
		this.eventFileName = eventFileName;
		this.threadName = threadName;
	}

	public void setLevel(Level level){
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}

	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	public long getTimeStamp() {
		return timestamp;
	}

	public void setStackTrace(String[] t){
		this.stackTrace = t;
	}
	public String[] getStackTrace(){
		return stackTrace;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEventFileName() {
		return eventFileName;
	}

	public void setEventFileName(String eventFileName) {
		this.eventFileName = eventFileName;
	}

	public String getThreadName() {
        return threadName;
    }
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    public long getTimestamp() {
		return timestamp;
	}
}
