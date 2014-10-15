/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.stats.StartsAndRuntime;
import com.serotonin.m2m2.view.stats.StartsAndRuntimeList;

/**
 * @author Terry Packer
 *
 */
public class StartsAndRuntimeListJsonGenerator extends StatisticsJsonGenerator{

	private StartsAndRuntimeList statistics;
	private int dataTypeId;
	
	/**
	 * @param jgen
	 * @param generator
	 */
	public StartsAndRuntimeListJsonGenerator(JsonGenerator jgen, int dataTypeId,
			StartsAndRuntimeList generator) {
		super(jgen, generator);
		this.statistics = generator;
		this.dataTypeId = dataTypeId;
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

		this.jgen.writeFieldName("startsAndRuntimes");
		this.jgen.writeStartArray();
		for(StartsAndRuntime stat : this.statistics.getData()){
			this.jgen.writeStartObject();
			
			//Write the data value
			if(stat.getDataValue() != null){
				if(this.dataTypeId == DataTypes.BINARY)
					this.jgen.writeBooleanField("value", stat.getDataValue().getBooleanValue());
				else
					this.jgen.writeNumberField("value", stat.getDataValue().getIntegerValue());
			}else{
				this.jgen.writeNullField("value");
			}
			
			this.jgen.writeNumberField("runtime", stat.getRuntime());
			this.jgen.writeNumberField("proportion", stat.getProportion());
			this.jgen.writeNumberField("starts", stat.getStarts());
			
			
			this.jgen.writeEndObject();
		}
		this.jgen.writeEndArray();
		
		//Do we have any data
		if(this.statistics.getData().size() > 0){
			this.jgen.writeBooleanField("hasData", true);
		}else{
			this.jgen.writeBooleanField("hasData", false);
		}
	}
	
	
}
