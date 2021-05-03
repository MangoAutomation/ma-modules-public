/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;

public class VirtualDataSourceRT extends PollingDataSource<VirtualDataSourceVO> {
	
	public static final int POLL_ABORTED_EVENT = 1;
	private final long delay;
	
    public VirtualDataSourceRT(VirtualDataSourceVO vo) {
        super(vo);
        delay = vo.getDelay();
    }

    @Override
    public boolean shouldInitializeIntervalLogging(DataPointRT point) {
        // if we arent going to poll then initialize interval logging when data point is initialized
        return !vo.isPolling();
    }

    @Override
    public void addDataPointImpl(DataPointRT dataPoint) {
        addDataPointImpl(dataPoint, vo.isPolling());
    }

    @Override
    public void beginPolling() {
        if (vo.isPolling()) {
            super.beginPolling();
        }
    }

    @Override
    public void doPoll(long time) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
        for (DataPointRT dataPoint : dataPoints) {
            VirtualPointLocatorRT locator = dataPoint.getPointLocator();

            // Change the point values according to their definitions.
            locator.change();
            dataPoint.updatePointValue(new PointValueTime(locator.getCurrentValue(), time));
        }
    }

    @Override
    public void forcePointRead(DataPointRT dataPoint) {
        VirtualPointLocatorRT locator = dataPoint.getPointLocator();
        locator.change();
        dataPoint.updatePointValue(new PointValueTime(locator.getCurrentValue(), System.currentTimeMillis()));
    }

    @Override
    public void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        VirtualPointLocatorRT l = dataPoint.getPointLocator();
        l.setCurrentValue(valueTime.getValue());
        dataPoint.setPointValue(valueTime, source);
    }

    @Override
    public void dataPointAdded(DataPointRT dataPoint) {
        if (dataPoint.getPointValue() != null) {
            VirtualPointLocatorRT locator = dataPoint.getPointLocator();
            locator.setCurrentValue(dataPoint.getPointValue().getValue());
        }
    }
}
