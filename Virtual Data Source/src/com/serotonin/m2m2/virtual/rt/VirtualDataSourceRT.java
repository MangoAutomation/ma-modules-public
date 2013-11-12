/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;

public class VirtualDataSourceRT extends PollingDataSource {
    public VirtualDataSourceRT(VirtualDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
    }

    @Override
    public void doPoll(long time) {
        for (DataPointRT dataPoint : dataPoints) {
            VirtualPointLocatorRT locator = dataPoint.getPointLocator();

            DataValue oldValue = locator.getCurrentValue();

            // Change the point values according to their definitions.
            locator.change();

            DataValue newValue = locator.getCurrentValue();

            // Update the data image with the new value if necessary.
            //TP EDIT, let the data point settings in the core choose this for us
            //if (!DataValue.isEqual(oldValue, newValue))
                dataPoint.updatePointValue(new PointValueTime(locator.getCurrentValue(), time));
        }
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
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
