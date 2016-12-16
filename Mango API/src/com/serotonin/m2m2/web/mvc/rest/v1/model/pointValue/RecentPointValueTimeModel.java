/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.serotonin.m2m2.rt.dataImage.PointValueTime;

/**
 * Point Value Time with additional information about if the value has 
 *  been logged or is a cached value.  
 * 
 * Useful for returning values from the cache for interval logged points that may have cached values
 *  that are not stored on disk.
 * 
 * @author Terry Packer
 */
public class RecentPointValueTimeModel extends PointValueTimeModel implements Comparable<RecentPointValueTimeModel>{
	
	boolean cached;

	public RecentPointValueTimeModel(){
		
	}
	
	public RecentPointValueTimeModel(PointValueTime pvt, boolean cached){
		super(pvt);
		this.cached = cached;
	}
	
	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 37 * (int)(this.data.getTime() ^ (this.data.getTime() >>> 32));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecentPointValueTimeModel){
			return ((RecentPointValueTimeModel)obj).data.getTime() == this.data.getTime();
		}else
			return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RecentPointValueTimeModel o) {
		return this.data.compareTo(o.data);
	}
}
