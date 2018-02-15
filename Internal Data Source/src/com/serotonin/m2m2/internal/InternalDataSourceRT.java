/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.monitor.AtomicIntegerMonitor;
import com.infiniteautomation.mango.monitor.DoubleMonitor;
import com.infiniteautomation.mango.monitor.IntegerMonitor;
import com.infiniteautomation.mango.monitor.LongMonitor;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;


/**
 * @author Matthew Lohbihler
 */
public class InternalDataSourceRT extends PollingDataSource<InternalDataSourceVO> {
    public static final int POLL_ABORTED_EVENT = 1;
    private final boolean createsPoints;
    private final Pattern createPointsPattern;
    private final Map<String, Boolean> monitorMap;
	
    public InternalDataSourceRT(InternalDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
        createsPoints = !StringUtils.isEmpty(vo.getCreatePointsPattern());
        if(createsPoints)
            try {
                createPointsPattern = Pattern.compile(vo.getCreatePointsPattern());
                monitorMap = new HashMap<String, Boolean>();
                for(DataPointVO dpvo : DataPointDao.instance.getDataPoints(vo.getId(), null)) {
                    InternalPointLocatorVO plvo = dpvo.getPointLocator();
                    monitorMap.put(plvo.getMonitorId(), true);
                }
            } catch(PatternSyntaxException e) {
                throw new ShouldNeverHappenException("Regex was validated but failed to compile:" + vo.getCreatePointsPattern());
            }
        else {
            createPointsPattern = null;
            monitorMap = null;
        }
    }


    @Override
    public void beginPolling() {
    	//Ensure have all our points loaded
    	this.updateChangedPoints(Common.timer.currentTimeMillis());
    	//Refresh our Monitored Values
    	for (DataPointRT dataPoint : dataPoints) {
            InternalPointLocatorRT locator = dataPoint.getPointLocator();
            ValueMonitor<?> m = Common.MONITORED_VALUES.getValueMonitor(locator.getPointLocatorVO().getMonitorId());
            if(m != null)
            	m.reset();
    	}
    	super.beginPolling();
    }
    
    @Override
    public void forcePointRead(DataPointRT dataPoint) {
    	InternalPointLocatorRT locator = dataPoint.getPointLocator();
        ValueMonitor<?> m = Common.MONITORED_VALUES.getValueMonitor(locator.getPointLocatorVO().getMonitorId());
        if (m != null){
        	if(m instanceof IntegerMonitor)
        		dataPoint.updatePointValue(new PointValueTime((double) ((IntegerMonitor)m).getValue(), Common.timer.currentTimeMillis()));
        	else if(m instanceof LongMonitor)
        		dataPoint.updatePointValue(new PointValueTime((double) ((LongMonitor)m).getValue(), Common.timer.currentTimeMillis()));
        	else if(m instanceof DoubleMonitor)
        		dataPoint.updatePointValue(new PointValueTime((double) ((DoubleMonitor)m).getValue(), Common.timer.currentTimeMillis()));
        	else if(m instanceof AtomicIntegerMonitor)
        		dataPoint.updatePointValue(new PointValueTime((double) ((AtomicIntegerMonitor)m).getValue(), Common.timer.currentTimeMillis()));
        }
    }
    
    @Override
    public void doPoll(long time) {
        if(createsPoints) {
            for(ValueMonitor<?> m : Common.MONITORED_VALUES.getMonitors()) {
                if(createPointsPattern.matcher(m.getId()).matches() && !monitorMap.containsKey(m.getId()))
                    createMonitorPoint(m);
            }
        }
        
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
            	else if(m instanceof AtomicIntegerMonitor)
            		dataPoint.updatePointValue(new PointValueTime((double) ((AtomicIntegerMonitor)m).getValue(), time));
            }
        }
    }
    
    private void createMonitorPoint(ValueMonitor<?> monitor) {
        DataPointVO dpvo = new DataPointVO();
        InternalPointLocatorVO plvo = new InternalPointLocatorVO();
        plvo.setMonitorId(monitor.getId());
        dpvo.setPointLocator(plvo);
        dpvo.setDataSourceId(vo.getId());
        dpvo.setDataSourceTypeName(vo.getDefinition().getDataSourceTypeName());
        dpvo.setEventDetectors(new ArrayList<AbstractPointEventDetectorVO<?>>(0));
        dpvo.setEnabled(true);
        dpvo.setDeviceName(vo.getName());
        //Logging types are usually going to be ON_CHANGE, INTERVAL (MAXIMUM), INTERVAL (INSTANT) AND INTERVAL (MINIMUM)
        if(monitor.getId().startsWith("com.serotonin.m2m2.rt.dataSource.PollingDataSource")) {
            //Defaults for polling data source metrics
            String namePart;
            if(monitor.getId().contains("SUCCESS")) {
                dpvo.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
                namePart = " - Consecutive Run Polls";
            } else {
                dpvo.setLoggingType(DataPointVO.LoggingTypes.INTERVAL);
                dpvo.setIntervalLoggingPeriod(5);
                dpvo.setIntervalLoggingPeriodType(TimePeriods.MINUTES);
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.MAXIMUM);
                if(monitor.getId().contains("QUOTIENT"))
                    namePart = " - Poll Abort Quotient";
                else //must be duration
                    namePart = " - Poll Duration";
            }
            
            //Set the device name base on the XID in the monitor ID....
            String dsXid = monitor.getId().substring(monitor.getId().indexOf('_')+1, monitor.getId().lastIndexOf('_'));
            defaultNewPointToDataSource(dpvo, dsXid);
            dpvo.setName(dpvo.getName() + namePart);
        } else if(monitor.getId().startsWith("com.serotonin.m2m2.persistent")) {
            //Defaults for persistent metrics
            int dsXidIndex = 30; //com.serotonin.m2m2.persistent.
            String namePart;
            dpvo.setLoggingType(DataPointVO.LoggingTypes.INTERVAL);
            dpvo.setIntervalLoggingPeriod(5);
            dpvo.setIntervalLoggingPeriodType(TimePeriods.MINUTES);
            if(monitor.getId().contains("TOTAL_CONNECTION_TIME_MONITOR")) {
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.MINIMUM);
                namePart = " - Connection Time";
                dsXidIndex+=30;
            } else if(monitor.getId().contains("CONNECTED_POINTS_MONITOR")) {
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.MINIMUM);
                namePart = " - Connected Points";
                dsXidIndex+=25;
            } else if(monitor.getId().contains("TOTAL_CONNECTIONS_MONITOR")) {
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.MAXIMUM);
                namePart = " - Total Connections";
                dsXidIndex+=26;
            } else if(monitor.getId().contains("TOTAL_TIMEOUTS_MONITOR")) {
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.MAXIMUM);
                namePart = " - Timeouts";
                dsXidIndex+=23;
            } else if(monitor.getId().contains("RECIEVING_RATE_MONITOR")) {
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.AVERAGE);
                namePart = " - Recieving Rate";
                dsXidIndex+=23;
            } else {//Nothing should get here currently...
                dpvo.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.INSTANT);
                namePart = monitor.getId();
            }
            
            //Set the device name base on the XID in the monitor ID....
            if(dsXidIndex > 30) {
                String dsXid = monitor.getId().substring(dsXidIndex);
                defaultNewPointToDataSource(dpvo, dsXid);
                dpvo.setName(dpvo.getName() + namePart);
            } else {
                dpvo.setName(namePart);
            }
            
            
        } else {
            //Default others, including InternalPointLocatorRT.MONITOR_NAMES to ON_CHANGE
            dpvo.setLoggingType(DataPointVO.LoggingTypes.ON_CHANGE);
            dpvo.setName(plvo.getConfigurationDescription().translate(Common.getTranslations()));
        }
        
        dpvo.defaultTextRenderer();
        dpvo.setXid(Common.generateXid("DP_Internal_"));
        monitorMap.put(monitor.getId(), true);
        Common.runtimeManager.saveDataPoint(dpvo); //Won't appear until next poll, but that's fine.
    }
    
    private void defaultNewPointToDataSource(DataPointVO dpvo, String dsXid) {
        DataSourceVO<?> dsvo = DataSourceDao.instance.getDataSource(dsXid);
        if(dsvo == null)
            throw new ShouldNeverHappenException("Error creating point, unknown data source: "+dsXid);
        dpvo.setName(dsvo.getName());
    }

    @Override
    public void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }
}
