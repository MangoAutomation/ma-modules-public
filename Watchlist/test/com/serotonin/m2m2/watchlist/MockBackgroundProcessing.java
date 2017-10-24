/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.rt.maint.BackgroundProcessingImpl;
import com.serotonin.m2m2.rt.maint.work.WorkItem;

/**
 * Stub for testing
 * 
 * @author Terry Packer
 *
 */
public class MockBackgroundProcessing extends BackgroundProcessingImpl{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.maint.BackgroundProcessing#addWorkItem(com.serotonin.m2m2.rt.maint.work.WorkItem)
	 */
	@Override
	public void addWorkItem(final WorkItem item) {
		
		new Thread(item.getDescription()){
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				item.execute();
			}
		}.start();//Stub to simply run it
	}
	
}
