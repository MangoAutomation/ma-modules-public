/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.infiniteautomation.mango.spring.components.ServerMonitoringService;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.EventHandlerDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.LifecycleDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.maint.UpgradeCheck;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.DataPointVO.LoggingTypes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.template.DataPointPropertiesTemplateVO;

/**
 *
 * @author Terry Packer
 *
 */
public class InternalLifecycle extends LifecycleDefinition {

    private static Log LOG = LogFactory.getLog(InternalLifecycle.class);

    @Override
    public void postInitialize(boolean install, boolean upgrade) {
        File safeFile = new File(Common.MA_HOME, "SAFE");
        final boolean safe = (safeFile.exists() && safeFile.isFile());
        maybeInstallSystemMonitor(safe);
    }

    //Module Monitor IDs
    public static final String POINT_LINK_COUNT_MONITOR_ID = "com.serotonin.m2m2.pointLinks.PointLinkDao.COUNT";
    public static final String WATCHLIST_COUNT_MONITOR_ID = "com.serotonin.m2m2.watchlist.WatchListDao.COUNT";
    public static final String SCHEDULED_EVENTS_COUNT_MONITOR_ID = "com.serotonin.m2m2.scheduledEvents.ScheduledEventDao.COUNT";
    public static final String REPORTS_COUNT_MONITOR_ID = "com.serotonin.m2m2.reports.ReportDao.COUNT";
    public static final String REPORT_INSTANCES_COUNT_MONITOR_ID = "com.serotonin.m2m2.reports.ReportInstanceDao.COUNT";
    public static final String EXCEL_REPORTS_COUNT_MONITOR_ID = "com.infiniteautomation.mango.excelreports.dao.ExcelReportDao.COUNT";
    public static final String EXCEL_REPORT_TEMPLATES_COUNT_MONITOR_ID = "com.infiniteautomation.mango.excelreports.dao.ExcelReportTemplateDao.COUNT";

    //Datasource Defaults
    public static final String SYSTEM_DATASOURCE_XID = "internal_mango_monitoring_ds";
    public static final String SYSTEM_DATASOURCE_DEVICE_NAME = "Mango Internal";

    //Default Point XIDs
    public static final String DATASOURCE_COUNT_POINT_XID = "internal_mango_num_data_sources";
    public static final String DATAPOINT_COUNT_POINT_XID = "internal_mango_num_data_points";
    public static final String USERS_COUNT_POINT_XID = "internal_mango_num_users";
    public static final String PUBLISHER_COUNT_POINT_XID = "internal_mango_num_publishers";
    public static final String EVENT_DETECTOR_COUNT_POINT_XID = "internal_mango_num_event_detectors";
    public static final String EVENT_HANDLER_COUNT_POINT_XID = "internal_mango_num_event_handlers";
    public static final String POINT_LINK_COUNT_POINT_XID = "internal_mango_num_point_links";
    public static final String AVAILABLE_UPDATES_COUNT_POINT_XID = "internal_mango_num_updates_available";
    public static final String ACTIVE_USER_SESSION_COUNT_POINT_XID = "internal_mango_num_active_user_sessions";
    public static final String SYSTEM_UPTIME_POINT_XID = "internal_mango_uptime_hrs";
    public static final String WATCHLIST_COUNT_POINT_XID = "internal_mango_num_watchlists";
    public static final String SCHEDULED_EVENTS_COUNT_POINT_XID = "internal_mango_num_scheduled_events";
    public static final String MAILING_LIST_COUNT_POINT_XID = "internal_mango_num_mailing_lists";
    public static final String REPORTS_COUNT_POINT_XID = "internal_mango_num_reports";
    public static final String REPORT_INSTANCES_COUNT_POINT_XID = "internal_mango_num_report_instances";
    public static final String EXCEL_REPORTS_COUNT_POINT_XID = "internal_mango_num_excel_reports";
    public static final String EXCEL_REPORT_TEMPLATES_COUNT_POINT_XID = "internal_mango_num_excel_report_templates";

    /**
     *
     * @return Map of Home Page Monitor XIDs to Home Page Monitors
     */
    public Map<String, ValueMonitor<?>> getAllHomePageMonitors(){
        Map<String, ValueMonitor<?>> monitors = new HashMap<String, ValueMonitor<?>>();

        //Data Source Count
        monitors.put(DATASOURCE_COUNT_POINT_XID, DataSourceDao.getInstance().getCountMonitor());

        //Data Point Count
        monitors.put(DATAPOINT_COUNT_POINT_XID, DataPointDao.getInstance().getCountMonitor());

        //Users Count
        monitors.put(USERS_COUNT_POINT_XID, UserDao.getInstance().getCountMonitor());

        //Publisher Count
        monitors.put(PUBLISHER_COUNT_POINT_XID, PublisherDao.getInstance().getCountMonitor());

        //Event Detector Count
        monitors.put(EVENT_DETECTOR_COUNT_POINT_XID, EventDetectorDao.getInstance().getCountMonitor());

        //Event Handler Count
        monitors.put(EVENT_HANDLER_COUNT_POINT_XID, EventHandlerDao.getInstance().getCountMonitor());

        //Mailing Lists Count
        monitors.put(MAILING_LIST_COUNT_POINT_XID, MailingListDao.getInstance().getCountMonitor());

        //Module Updates Available
        monitors.put(AVAILABLE_UPDATES_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(UpgradeCheck.UPGRADES_AVAILABLE_MONITOR_ID));

        //Active User Sessions
        monitors.put(ACTIVE_USER_SESSION_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.USER_SESSION_MONITOR_ID));

        //System Uptime
        monitors.put(SYSTEM_UPTIME_POINT_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.SYSTEM_UPTIME_MONITOR_ID));

        //Get from Modules
        monitors.put(POINT_LINK_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(POINT_LINK_COUNT_MONITOR_ID));
        monitors.put(WATCHLIST_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(WATCHLIST_COUNT_MONITOR_ID));
        monitors.put(SCHEDULED_EVENTS_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(SCHEDULED_EVENTS_COUNT_MONITOR_ID));
        monitors.put(REPORTS_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(REPORTS_COUNT_MONITOR_ID));
        monitors.put(REPORT_INSTANCES_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(REPORT_INSTANCES_COUNT_MONITOR_ID));
        monitors.put(EXCEL_REPORTS_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(EXCEL_REPORTS_COUNT_MONITOR_ID));
        monitors.put(EXCEL_REPORT_TEMPLATES_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(EXCEL_REPORT_TEMPLATES_COUNT_MONITOR_ID));

        return monitors;
    }

    /**
     *
     */
    private void maybeInstallSystemMonitor(boolean safe) {
        DataSourceVO ds = DataSourceDao.getInstance().getByXid(SYSTEM_DATASOURCE_XID);
        if(ds == null){
            //Create Data Source
            DataSourceDefinition def = ModuleRegistry.getDataSourceDefinition(InternalDataSourceDefinition.DATA_SOURCE_TYPE);
            ds = def.baseCreateDataSourceVO();
            InternalDataSourceVO vo = (InternalDataSourceVO)ds;
            vo.setXid(SYSTEM_DATASOURCE_XID);
            vo.setName(SYSTEM_DATASOURCE_DEVICE_NAME);

            vo.setUpdatePeriods(10);
            vo.setUpdatePeriodType(TimePeriods.SECONDS);

            try {
                DataSourceDao.getInstance().saveDataSource(vo);

                // Setup the Points
                maybeCreatePoints(safe, ds);

                // Enable the data source
                if (!safe) {
                    vo.setEnabled(true);
                    Common.runtimeManager.saveDataSource(vo);
                }
            }catch(ValidationException e) {
                for(ProcessMessage message : e.getValidationResult().getMessages()){
                    LOG.error(message.toString(Common.getTranslations()));
                }
            }
        }else{
            //Ensure all points are added
            maybeCreatePoints(safe, ds);
        }
    }

    /**
     *
     */
    private void maybeCreatePoints(boolean safe, DataSourceVO vo) {
        Map<String, ValueMonitor<?>> monitors = getAllHomePageMonitors();
        Iterator<String> it = monitors.keySet().iterator();
        while(it.hasNext()){
            String xid = it.next();
            ValueMonitor<?> monitor = monitors.get(xid);
            if(monitor != null){
                DataPointVO dp = DataPointDao.getInstance().getByXid(xid);
                if(dp == null){
                    InternalPointLocatorVO pl = new InternalPointLocatorVO();
                    pl.setMonitorId(monitor.getId());

                    dp = new DataPointVO();
                    dp.setXid(xid);
                    dp.setName(monitor.getName().translate(Common.getTranslations()));
                    dp.setDataSourceId(vo.getId());
                    dp.setDeviceName(vo.getName());
                    dp.setEventDetectors(new ArrayList<AbstractPointEventDetectorVO<?>>(0));
                    dp.defaultTextRenderer();
                    dp.setEnabled(true);
                    dp.setChartColour("");

                    dp.setPointLocator(pl);

                    //Use default template
                    DataPointPropertiesTemplateVO template = TemplateDao.getInstance().getDefaultDataPointTemplate(pl.getDataTypeId());
                    if(template != null){
                        template.updateDataPointVO(dp);
                        dp.setTemplateId(template.getId());
                    }

                    //If we are numeric then we want to log on change
                    switch(pl.getDataTypeId()){
                        case DataTypes.NUMERIC:
                            if(SYSTEM_UPTIME_POINT_XID.equals(xid)) { //This changes every time, so just do an interval instant
                                dp.setLoggingType(LoggingTypes.INTERVAL);
                                dp.setIntervalLoggingPeriodType(Common.TimePeriods.MINUTES);
                                dp.setIntervalLoggingPeriod(5);
                                dp.setIntervalLoggingType(DataPointVO.IntervalLoggingTypes.INSTANT);
                            } else {
                                //Setup to Log on Change
                                dp.setLoggingType(LoggingTypes.ON_CHANGE);
                            }

                            if(dp.getTextRenderer() instanceof AnalogRenderer && !dp.getXid().equals(SYSTEM_UPTIME_POINT_XID)) {
                                // This are count points, no need for decimals.
                                ((AnalogRenderer)dp.getTextRenderer()).setFormat("0");
                            }

                            //No template in use here
                            dp.setTemplateId(null);
                            break;
                    }

                    if(safe)
                        DataPointDao.getInstance().saveDataPoint(dp);
                    else
                        Common.runtimeManager.saveDataPoint(dp);
                }
            }
        }
    }
}
