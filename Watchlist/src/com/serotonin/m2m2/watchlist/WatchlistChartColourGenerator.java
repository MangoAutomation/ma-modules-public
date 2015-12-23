/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.awt.Color;

/**
 * 
 * 
 * @author Terry Packer
 *
 */
public class WatchlistChartColourGenerator {

	private Color[] colors;
	private int currentColor;
	
	public WatchlistChartColourGenerator(int distinctColours, float minComponent, float maxComponent){
		this.colors = ColorUtils.generateVisuallyDistinctColors(distinctColours, minComponent, maxComponent);
		this.currentColor = 0;
	}
	
	public String getNextHexColour(){
		Color c = this.colors[this.currentColor];
		this.currentColor++;
		return "0x" + Integer.toHexString(c.getRGB()).substring(2);
	}
	
}
