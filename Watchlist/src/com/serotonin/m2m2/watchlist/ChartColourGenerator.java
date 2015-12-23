/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

/**
 * 
 * Simple Interface to generate colours
 * 
 * @author Terry Packer
 *
 */
public interface ChartColourGenerator {
	
	/**
	 * Return the Hex code for the colour as 0x------
	 * @return
	 */
	public String getNextHexColour();

}
