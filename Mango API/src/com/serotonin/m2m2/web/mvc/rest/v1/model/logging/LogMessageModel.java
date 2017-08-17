/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.logging;

/**
 * @author Terry Packer
 *
 */
public class LogMessageModel {
	
	private String level;
	private String classname;
	private String method;
	private Integer lineNumber;
	private String message;
	private String[] stackTrace;
	private long time;
	
	public LogMessageModel(){ }
	
	/**
	 * @param level
	 * @param message
	 * @param time
	 */
	public LogMessageModel(String level, String classname, String method, Integer lineNumber, String message, String[] stackTrace, long time) {
		super();
		this.level = level;
		this.classname = classname;
		this.method = method;
		this.lineNumber = lineNumber;
		this.message = message;
		this.stackTrace = stackTrace;
		this.time = time;
	}
	
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String[] getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	

}
