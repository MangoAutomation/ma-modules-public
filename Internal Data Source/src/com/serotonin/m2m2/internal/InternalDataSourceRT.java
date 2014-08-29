/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.monitor.IntegerMonitor;

/**
 * @author Matthew Lohbihler
 */
public class InternalDataSourceRT extends PollingDataSource {
    public static final int POLL_ABORTED_EVENT = 1;
	
    public InternalDataSourceRT(InternalDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
    }

    @Override
    public void doPoll(long time) {
        for (DataPointRT dataPoint : dataPoints) {
            InternalPointLocatorRT locator = dataPoint.getPointLocator();

            String monitorId = InternalPointLocatorVO.MONITOR_NAMES[locator.getPointLocatorVO().getAttributeId()];
            // They are all integer monitors so far, so this is fine.
            IntegerMonitor m = (IntegerMonitor) Common.MONITORED_VALUES.getValueMonitor(monitorId);
            if (m != null)
                dataPoint.updatePointValue(new PointValueTime((double) m.getValue(), time));
        }
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }
}
