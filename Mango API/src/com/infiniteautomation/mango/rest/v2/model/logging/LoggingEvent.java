/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.logging;

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
	private String className;
	private String methodName;
	private String lineNumber;



	public LoggingEvent(Level level, long timestamp, String message, String[] stackTrace, 
			String eventFileName, String className,
			String methodName, String lineNumber) {
		super();
		this.level = level;
		this.timestamp = timestamp;
		this.message = message;
		this.stackTrace = stackTrace;
		this.eventFileName = eventFileName;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
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

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean hasLocationInformation(){
		return ((methodName != null) && (className != null)&&(lineNumber!=null));
	}
}
