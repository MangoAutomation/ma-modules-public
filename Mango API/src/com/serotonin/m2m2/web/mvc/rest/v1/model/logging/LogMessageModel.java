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
	private String thread;
	private String message;
	private String[] stackTrace;
	private long time;
	
	public LogMessageModel(){ }
	
	/**
	 * @param level
	 * @param message
	 * @param time
	 */
	public LogMessageModel(String level, String thread, String message, String[] stackTrace, long time) {
		super();
		this.level = level;
		this.thread = thread;
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
	public String getThread() {
        return thread;
    }
    public void setThread(String thread) {
        this.thread = thread;
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
	
}
