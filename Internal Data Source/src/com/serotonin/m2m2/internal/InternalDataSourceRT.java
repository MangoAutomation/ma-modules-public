/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import com.infiniteautomation.mango.monitor.DoubleMonitor;
import com.infiniteautomation.mango.monitor.IntegerMonitor;
import com.infiniteautomation.mango.monitor.LongMonitor;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;


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
            ValueMonitor<?> m = Common.MONITORED_VALUES.getValueMonitor(locator.getPointLocatorVO().getMonitorId());
            if (m != null){
            	if(m instanceof IntegerMonitor)
            		dataPoint.updatePointValue(new PointValueTime((double) ((IntegerMonitor)m).getValue(), time));
            	else if(m instanceof LongMonitor)
            		dataPoint.updatePointValue(new PointValueTime((double) ((LongMonitor)m).getValue(), time));
            	else if(m instanceof DoubleMonitor)
            		dataPoint.updatePointValue(new PointValueTime((double) ((DoubleMonitor)m).getValue(), time));
            }
        }
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }
}
