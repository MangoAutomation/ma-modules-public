/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.datasource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.v2.model.time.TimePeriodType;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Terry Packer
 *
 */
@JsonPropertyOrder({"override"})
public class PurgeSettings {

    public PurgeSettings(DataSourceVO<?> vo) {
        this(vo.isPurgeOverride(), vo.getPurgePeriod(), vo.getPurgeType());
    }
    
	/**
	 * 
	 * @param override - Override system defaults
	 * @param periods - number of periods
	 * @param type - Period Type integer
	 */
	public PurgeSettings(boolean override, int periods, int type){
		this.override = override;
		this.frequency = new TimePeriod(periods ,TimePeriodType.convertTo(type));
	}
	
	public PurgeSettings(){
		
	}
	
	@JsonProperty("override")
	private boolean override;
	
	@JsonProperty("frequency")
	private TimePeriod frequency;

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public TimePeriod getFrequency() {
		return frequency;
	}

	public void setFrequency(TimePeriod frequency) {
		this.frequency = frequency;
	}
	
	public void fromVO(DataSourceVO<?> vo) {
        this.override = vo.isPurgeOverride();
        this.frequency = new TimePeriod(vo.getPurgePeriod() ,TimePeriodType.convertTo(vo.getPurgeType()));
	}
	
	public void toVO(DataSourceVO<?> vo) {
	    vo.setPurgeOverride(override);
	    if(frequency != null) {
	        vo.setPurgeType(TimePeriodType.convertFrom(frequency.getType()));
	        vo.setPurgePeriod(frequency.getPeriods());
	    }
	}
	
}
