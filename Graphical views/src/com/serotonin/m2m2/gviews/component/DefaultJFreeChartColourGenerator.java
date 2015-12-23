/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.gviews.component;

import java.awt.Color;

import org.jfree.chart.plot.DefaultDrawingSupplier;

/**
 * Produce same color pattern as JFreeChart
 * 
 * @author Terry Packer
 *
 */
public class DefaultJFreeChartColourGenerator implements ChartColourGenerator{

	private DefaultDrawingSupplier supplier;
	
	public DefaultJFreeChartColourGenerator(){
		this.supplier = new DefaultDrawingSupplier();
	}
	
	public String getNextHexColour(){
		Color c = (Color)this.supplier.getNextPaint();
		return "0x" + Integer.toHexString(c.getRGB()).substring(2);
	}
	
}
