/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.thread;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Terry Packer
 *
 */
public class ThreadModel {
	
	private ThreadInfo info;
	
	private long id;
	private int priority;
	private String name;
	private long cpuTime;
	private long userTime;

	
	public ThreadModel(long id, int priority, String name, ThreadInfo info, long cpuTime, long userTime){
		this.id = id;
		this.priority = priority;
		this.name = name;
	    this.info = info;
		this.cpuTime = cpuTime;
		this.userTime = userTime;
	}
	
	public ThreadModel(long id, int priority, String name, long cpuTime, long userTime){
        this.id = id;
        this.priority = priority;
        this.name = name;
        this.cpuTime = cpuTime;
        this.userTime = userTime;
    }
	
	@JsonGetter("id")
	public long getId(){
		return id;
	}
	
	@JsonGetter("name")
	public String getName(){
		return name;
	}

	@JsonGetter("cpuTime")
	public long getCpuTime(){
		return this.cpuTime;
	}
	
	@JsonGetter("userTime")
	public long getUserTime(){
		return this.userTime;
	}

	@JsonGetter("state")
	public State getState(){
	    if(this.info != null)
	        return this.info.getThreadState();
	    else
	        return State.TERMINATED;
	}
	
	@JsonGetter("priority")
	public int getPriority(){
		return priority;
	}

	@JsonGetter("location")
	public StackTraceElement[] getLocation(){
	    if(info != null)
	        return this.info.getStackTrace();
	    else 
	        return null;
	}
	
	@JsonGetter("lockOwnerName")
	public String getLockOwnerName() {
	    if(info != null)
	        return this.info.getLockOwnerName();
	    else 
	        return null;
	}
	
	@JsonGetter("lockOwnerId")
	public long getLockOwnerId() {
	    if(info != null)
	        return this.info.getLockOwnerId();
	    else
	        return -1l;
	}
	
	@JsonGetter("lockInfo")
	public LockInfo getLockInfo() {
	    if(info != null)
	        return this.info.getLockInfo();
	    else
	        return null;
	}

}
