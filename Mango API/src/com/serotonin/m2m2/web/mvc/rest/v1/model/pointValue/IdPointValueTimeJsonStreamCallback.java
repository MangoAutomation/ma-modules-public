/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.IdTimeJsonStreamCallback;

/**
 * Create JSON Output for multiple data point's point values of the form:
 * 
 * [
 *  {
 *   "timestamp": 1470696259000,
 *   "xid_one": 23.44,
 *   "xid_two": 11.33,
 *   "xid_three": 57.9
 * },
 * {
 *   "timestamp": 1470696259000,
 *   "xid_one": 23.44,
 *   "xid_three": 57.9
 * },
 * {
 *   "timestamp": 1470696259000,
 *   "xid_two": 11.33,
 *  "xid_three": 57.9
 * }
 *]
 * 
 * 
 * @author Terry Packer
 *
 */
public class IdPointValueTimeJsonStreamCallback extends IdTimeJsonStreamCallback<IdPointValueTime>{
	
    /**
     * 
     * @param host
     * @param port
     * @param jgen
     * @param voMap
     * @param useRendered
     * @param unitConversion
     * @param limit
     * @param dateTimeFormat  - format for String dates or null for timestamp numbers
     * @param timezone
     */
	public IdPointValueTimeJsonStreamCallback(String host, int port, JsonGenerator jgen, Map<Integer,DataPointVO> voMap, boolean useRendered,  boolean unitConversion, Integer limit, String dateTimeFormat, String timezone) {
		super(new XidPointValueTimeJsonWriter(host, port, jgen, useRendered, unitConversion, dateTimeFormat, timezone), jgen, voMap, limit, dateTimeFormat, timezone);
	}
	
}
