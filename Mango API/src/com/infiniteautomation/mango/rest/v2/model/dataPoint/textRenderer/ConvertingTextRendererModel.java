/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint.textRenderer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public abstract class ConvertingTextRendererModel<T> extends BaseTextRendererModel<T>{

	@JsonProperty
	private boolean useUnitAsSuffix;
	
	public ConvertingTextRendererModel(){ }
	
	/**
	 * @param useUnitAsSuffix
	 * @param unit
	 * @param renderedUnit
	 */
	public ConvertingTextRendererModel(boolean useUnitAsSuffix, String unit,
			String renderedUnit) {
		super();
		this.useUnitAsSuffix = useUnitAsSuffix;
	}
	public boolean isUseUnitAsSuffix() {
		return useUnitAsSuffix;
	}
	public void setUseUnitAsSuffix(boolean useUnitAsSuffix) {
		this.useUnitAsSuffix = useUnitAsSuffix;
	}
}
