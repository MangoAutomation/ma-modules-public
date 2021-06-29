/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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
public class MockBackgroundProcessing extends BackgroundProcessingImpl {

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
