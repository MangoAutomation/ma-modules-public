/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.view.stats.StartsAndRuntime;
import com.serotonin.m2m2.view.stats.StartsAndRuntimeList;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class StartsAndRuntimeListJsonGenerator extends StatisticsJsonGenerator{

	private StartsAndRuntimeList statistics;
	
	/**
	 * 
	 * @param jgen
	 * @param vo
	 * @param useRendered
	 * @param unitConversion
	 * @param generator
	 */
	public StartsAndRuntimeListJsonGenerator(String host, int port, JsonGenerator jgen, DataPointVO vo, boolean useRendered,
			boolean unitConversion, StartsAndRuntimeList generator) {
		super(host, port, jgen, vo, useRendered, unitConversion, generator);
		this.statistics = generator;
	}

	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsJsonGenerator#writeStatistics()
	 */
	@Override
	public void done() throws IOException {
		this.generator.done();
		
		//Do we have any data
		if(this.statistics.getCount() > 0){
			this.jgen.writeBooleanField("hasData", true);
			this.jgen.writeFieldName("first");
			this.writeNonNull(this.statistics.getFirstValue(), this.statistics.getFirstTime(), this.vo);
			
			this.jgen.writeFieldName("last");
			this.writeNonNull(this.statistics.getLastValue(), this.statistics.getLastTime(), this.vo);

			this.jgen.writeNumberField("count", this.statistics.getCount());
			
			this.jgen.writeFieldName("startsAndRuntimes");
			this.jgen.writeStartArray();
			for(StartsAndRuntime stat : this.statistics.getData()){
				this.jgen.writeStartObject();
				
				//Write the data value
				if(stat.getDataValue() != null){
					if(this.vo.getPointLocator().getDataTypeId() == DataTypes.BINARY)
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
		}else{
			this.jgen.writeBooleanField("hasData", false);
			this.jgen.writeNullField("first");
			this.jgen.writeNullField("last");
			this.jgen.writeNullField("startsAndRuntimes");
		}
	}
	
	
}
