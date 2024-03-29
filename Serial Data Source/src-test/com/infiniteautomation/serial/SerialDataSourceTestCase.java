/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.serial;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;

/**
 * @author Terry Packer
 *
 */
public class SerialDataSourceTestCase {

	
	private DataPointRT dprt;
	private String condition;
	private String terminator;
	private int newValueCount;
	private String[] results;

	public SerialDataSourceTestCase(DataPointRT dprt, String condition,
			String terminator, int newValueCount, String[] results) {
		this.dprt = dprt;
		this.condition = condition;
		this.terminator = terminator;
		this.newValueCount = newValueCount;
		this.results = results;
	}

	/**
     */
	public DataPointRT getTargetPoint() {
		return this.dprt;
	}

	/**
     */
	public String getCondition() {
		return this.condition;
	}

	/**
     */
	public String getTerminator() {
		return this.terminator;
	}

	/**
     */
	public String[] getResults() {
		return this.results;
	}

	/**
     */
	public int getNewValueCount() {
		return this.newValueCount;
	}

	/**
     */
	public String getResult(int k) {
		return this.results[k];
	}

}
