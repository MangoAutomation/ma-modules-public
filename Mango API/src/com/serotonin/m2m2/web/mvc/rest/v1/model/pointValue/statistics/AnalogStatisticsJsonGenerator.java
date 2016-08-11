/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.vo.DataPointVO;

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
			DataPointVO vo, boolean useRendered, boolean unitConversion, AnalogStatistics generator) {
		super(jgen, vo, useRendered, unitConversion, generator);
		this.statistics = generator;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsJsonGenerator#writeStatistics()
	 */
	@Override
	public void done(PointValueTime last) throws IOException {
		this.generator.done(last);

		if(this.statistics.getCount() > 0){
			this.jgen.writeBooleanField("hasData", true);
			this.jgen.writeFieldName("first");
			this.writeNonNullDouble(this.statistics.getFirstValue(), this.statistics.getFirstTime(), this.vo);
			
			this.jgen.writeFieldName("last");
			this.writeNonNullDouble(this.statistics.getLastValue(), this.statistics.getLastTime(), this.vo);
			
			this.jgen.writeFieldName("minimum");
			this.writeNonNullDouble(this.statistics.getMinimumValue(), this.statistics.getMinimumTime(), this.vo);
			
			this.jgen.writeFieldName("maximum");
			this.writeNonNullDouble(this.statistics.getMaximumValue(), this.statistics.getMaximumTime(), this.vo);
			
			this.jgen.writeFieldName("average");
			this.writeNonNullDouble(this.statistics.getAverage(), this.statistics.getPeriodEndTime(), this.vo);
			
			this.jgen.writeFieldName("integral");
			this.writeNonNullIntegral(this.statistics.getIntegral(), this.statistics.getPeriodEndTime(), this.vo);
			
			this.jgen.writeFieldName("sum");
			this.writeNonNullDouble(this.statistics.getSum(), this.statistics.getPeriodEndTime(), this.vo);

			this.jgen.writeNumberField("count", this.statistics.getCount());
		}else{
			this.jgen.writeBooleanField("hasData", false);
			this.jgen.writeNullField("first");
			this.jgen.writeNullField("last");
			this.jgen.writeNullField("minimum");
			this.jgen.writeNullField("maximum");
			this.jgen.writeNullField("average");
			this.jgen.writeNullField("integral");
			this.jgen.writeNullField("sum");
			this.jgen.writeNullField("count");
		}
	}

}
