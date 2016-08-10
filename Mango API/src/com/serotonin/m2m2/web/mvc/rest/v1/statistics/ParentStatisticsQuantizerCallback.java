/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import java.util.Map;

import com.serotonin.m2m2.view.stats.StatisticsGenerator;

/**
 * @author Terry Packer
 *
 */
public interface ParentStatisticsQuantizerCallback {

	/**
	 * @param periodStatsMap
	 * @param periodStartTime
	 */
	public void closePeriod(Map<Integer, StatisticsGenerator> periodStatsMap, long periodStartTime);

}
