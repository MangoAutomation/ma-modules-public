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

public class VirtualDataSourceRT extends PollingDataSource {
	
	public static final int POLL_ABORTED_EVENT = 1;
	
    public VirtualDataSourceRT(VirtualDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
    }

    @Override
    public void doPoll(long time) {
        for (DataPointRT dataPoint : dataPoints) {
            VirtualPointLocatorRT locator = dataPoint.getPointLocator();

            //DataValue oldValue = locator.getCurrentValue();

            // Change the point values according to their definitions.
            locator.change();

            //DataValue newValue = locator.getCurrentValue();

            // Update the data image with the new value if necessary.
            //TP EDIT, let the data point settings in the core choose the logging settings for us
            // rather than use only insert changes here
            //TP TODO: this actually causes issues in high polling data sources.  When setting the value from the UI
            // it will set the value once from there and another time from here.
            //if (!DataValue.isEqual(oldValue, newValue))
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
    public void addDataPoint(DataPointRT dataPoint) {
        if (dataPoint.getPointValue() != null) {
            VirtualPointLocatorRT locator = dataPoint.getPointLocator();
            locator.setCurrentValue(dataPoint.getPointValue().getValue());
        }

        super.addDataPoint(dataPoint);
    }
}
