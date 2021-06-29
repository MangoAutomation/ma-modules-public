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
	 * @return
	 */
	public DataPointRT getTargetPoint() {
		return this.dprt;
	}

	/**
	 * @return
	 */
	public String getCondition() {
		return this.condition;
	}

	/**
	 * @return
	 */
	public String getTerminator() {
		return this.terminator;
	}

	/**
	 * @return
	 */
	public String[] getResults() {
		return this.results;
	}

	/**
	 * @return
	 */
	public int getNewValueCount() {
		return this.newValueCount;
	}

	/**
	 * @param k
	 * @return
	 */
	public String getResult(int k) {
		return this.results[k];
	}

}
