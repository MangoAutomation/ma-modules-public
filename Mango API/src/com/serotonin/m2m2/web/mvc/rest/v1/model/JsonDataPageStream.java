/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * 
 * Used to allow the Jackson Mapper to wrap this 
 * entity with a count and results
 * 
 * @author Terry Packer
 *
 */
public interface JsonDataPageStream extends JsonArrayStream{

	/**
	 * Stream the Query Count Value
	 * @param jgen
	 * @throws IOException
	 */
	public void streamCount(JsonGenerator jgen) throws IOException;
}
