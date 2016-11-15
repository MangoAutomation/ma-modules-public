/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.stats.IValueTime;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * 
 * Class that uses a Statistics Generator to write all values as JSON
 * 
 * @author Terry Packer
 *
 */
public abstract class StatisticsJsonGenerator extends StatisticsJsonWriter {

	protected StatisticsGenerator generator;
	
	/**
	 * @param periodStart
	 * @param periodEnd
	 * @param startValue
	 */
	public StatisticsJsonGenerator(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered, boolean unitConversion, StatisticsGenerator generator) {
		super(host, port, jgen, vo, useRendered, unitConversion);
		this.generator = generator;
	}

	
    /**
     * Used to add values to a period
     * 
     * @param vt
     *            the value to add
     */
    public void addValueTime(IValueTime vt){
    	this.generator.addValueTime(vt);
    }

    /**
     * Used to end a period
     */
    public void done(PointValueTime last) throws IOException{
    	this.generator.done(last);
    }
}
