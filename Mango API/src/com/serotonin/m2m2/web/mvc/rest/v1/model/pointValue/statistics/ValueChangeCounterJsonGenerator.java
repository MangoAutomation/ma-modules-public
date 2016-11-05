/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class ValueChangeCounterJsonGenerator extends StatisticsJsonGenerator{

	private ValueChangeCounter statistics;
	
	/**
	 * @param jgen
	 * @param valueChangeCounter
	 */
	public ValueChangeCounterJsonGenerator(String host, int port, JsonGenerator jgen,
			DataPointVO vo, boolean useRendered, boolean unitConversion,
			ValueChangeCounter valueChangeCounter) {
		super(host, port, jgen, vo, useRendered, unitConversion, valueChangeCounter);
		this.statistics = valueChangeCounter;
	}
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsJsonGenerator#writeStatistics()
	 */
	@Override
	public void done(PointValueTime last) throws IOException {
		this.generator.done(last);
		
		//Do we have any data
		if(this.statistics.getCount() > 0){
			this.jgen.writeBooleanField("hasData", true);
			this.jgen.writeFieldName("first");
			this.writePointValueTime(this.statistics.getFirstValue(), this.statistics.getFirstTime(), null, vo);
			
			this.jgen.writeFieldName("last");
			this.writePointValueTime(this.statistics.getLastValue(), this.statistics.getLastTime(), null, vo);
			
			this.jgen.writeNumberField("count", this.statistics.getCount());
			this.jgen.writeNumberField("changes",  this.statistics.getChanges());
		}else{
			this.jgen.writeBooleanField("hasData", false);
			this.jgen.writeNullField("first");
			this.jgen.writeNullField("last");
			this.jgen.writeNullField("count");
			this.jgen.writeNullField("changes");
		}
	}
	

}
