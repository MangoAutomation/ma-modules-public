/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.stats.AnalogStatistics;

/**
 * @author Terry Packer
 *
 */
public class AnalogStatisticsJsonGenerator extends StatisticsJsonGenerator{

	private AnalogStatistics statistics;
	
	/**
	 * @param jgen
	 * @param generator
	 */
	public AnalogStatisticsJsonGenerator(JsonGenerator jgen,
			AnalogStatistics generator) {
		super(jgen, generator);
		this.statistics = generator;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsJsonGenerator#writeStatistics()
	 */
	@Override
	public void done(PointValueTime last) throws IOException {
		this.generator.done(last);

		this.jgen.writeFieldName("first");
		this.writePointValueTime(this.statistics.getFirstValue(), this.statistics.getFirstTime(), null);
		
		this.jgen.writeFieldName("last");
		this.writePointValueTime(this.statistics.getLastValue(), this.statistics.getLastTime(), null);
		
		this.jgen.writeFieldName("minimum");
		this.writePointValueTime(this.statistics.getMinimumValue(), this.statistics.getMinimumTime(), null);
		
		this.jgen.writeFieldName("maximum");
		this.writePointValueTime(this.statistics.getMaximumValue(), this.statistics.getMaximumTime(), null);
		
		this.jgen.writeNumberField("average", this.statistics.getAverage());
		this.jgen.writeNumberField("integral", this.statistics.getIntegral());
		this.jgen.writeNumberField("sum", this.statistics.getSum());
		this.jgen.writeNumberField("count", this.statistics.getCount());
		
		if(this.statistics.getCount() > 0){
			this.jgen.writeBooleanField("hasData", true);
		}else{
			this.jgen.writeBooleanField("hasData", false);
		}
	}

}
