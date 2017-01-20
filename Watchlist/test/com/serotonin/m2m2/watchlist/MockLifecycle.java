/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.ILifecycle;
import com.serotonin.m2m2.util.timeout.TimeoutTask;

/**
 * @author Terry Packer
 *
 */
public class MockLifecycle implements ILifecycle{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#terminate()
	 */
	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#addStartupTask(java.lang.Runnable)
	 */
	@Override
	public void addStartupTask(Runnable task) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#addShutdownTask(java.lang.Runnable)
	 */
	@Override
	public void addShutdownTask(Runnable task) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#getLifecycleState()
	 */
	@Override
	public int getLifecycleState() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#getStartupProgress()
	 */
	@Override
	public float getStartupProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#getShutdownProgress()
	 */
	@Override
	public float getShutdownProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#loadLic()
	 */
	@Override
	public void loadLic() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#scheduleShutdown(long, boolean)
	 */
	@Override
	public TimeoutTask scheduleShutdown(long timeout, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.ILifecycle#isRestarting()
	 */
	@Override
	public boolean isRestarting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer dataPointLimit() {
		// TODO Auto-generated method stub
		return null;
	}

}
