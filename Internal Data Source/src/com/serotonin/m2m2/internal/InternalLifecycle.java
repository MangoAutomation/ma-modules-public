/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.measure.unit.NonSI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zafarkhaja.semver.Version;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.infiniteautomation.mango.spring.components.DiskUsageMonitoringService;
import com.infiniteautomation.mango.spring.components.ServerMonitoringService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.Rollups;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.EventHandlerDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.LifecycleDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.maint.UpgradeCheck;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.DataPointVO.IntervalLoggingTypes;
import com.serotonin.m2m2.vo.DataPointVO.LoggingTypes;
import com.serotonin.m2m2.vo.dataPoint.DataPointWithEventDetectors;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.provider.Providers;

/**
 *
 * @author Terry Packer
 *
 */
public class InternalLifecycle extends LifecycleDefinition {

    private static Logger LOG = LoggerFactory.getLogger(InternalLifecycle.class);

    @Override
    public void postInitialize(Version previousVersion, Version current) {
        try {
            IMangoLifecycle lifecycle = Providers.get(IMangoLifecycle.class);
            maybeInstallSystemMonitor(lifecycle.isSafeMode());
        }catch(Exception e) {
            LOG.error("Failed to create internal data points", e);
        }
    }

    //Module Monitor IDs
    public static final String WATCHLIST_COUNT_MONITOR_ID = "com.infiniteautomation.mango.spring.dao.WatchListDao.COUNT";
    public static final String SCHEDULED_EVENTS_COUNT_MONITOR_ID = "com.serotonin.m2m2.scheduledEvents.ScheduledEventDao.COUNT";
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
    public static final String AVAILABLE_UPDATES_COUNT_POINT_XID = "internal_mango_num_updates_available";
    public static final String ACTIVE_USER_SESSION_COUNT_POINT_XID = "internal_mango_num_active_user_sessions";
    public static final String SYSTEM_UPTIME_POINT_XID = "internal_mango_uptime_hrs";
    public static final String WATCHLIST_COUNT_POINT_XID = "internal_mango_num_watchlists";
    public static final String SCHEDULED_EVENTS_COUNT_POINT_XID = "internal_mango_num_scheduled_events";
    public static final String MAILING_LIST_COUNT_POINT_XID = "internal_mango_num_mailing_lists";
    public static final String EXCEL_REPORTS_COUNT_POINT_XID = "internal_mango_num_excel_reports";
    public static final String EXCEL_REPORT_TEMPLATES_COUNT_POINT_XID = "internal_mango_num_excel_report_templates";

    //System metrics
    public static final String SQL_DATABASE_SIZE_POINT_XID = "internal_mango_sql_db_size";
    public static final String SQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID = "internal_mango_sql_db_usable_space";
    public static final String NOSQL_DATABASE_SIZE_POINT_XID = "internal_mango_no_sql_db_size";
    public static final String NOSQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID = "internal_mango_no_sql_db_usable_space";
    public static final String MA_HOME_PARTITION_TOTAL_SPACE_XID = "internal_mango_disk_total_space";
    public static final String MA_HOME_PARTITION_USED_SPACE_XID = "internal_mango_disk_used_space";
    public static final String MA_HOME_PARTITION_USABLE_SPACE_XID = "internal_mango_disk_usable_space";
    public static final String JVM_USED_MEMORY_XID = "internal_jvm_used_memory";
    public static final String JVM_MAX_MEMORY_XID = "internal_jvm_max_memory";
    public static final String JVM_FREE_MEMORY_XID = "internal_jvm_free_memory";
    public static final String CPU_SYSTEM_LOAD_XID = "internal_cpu_system_load";
    public static final String CPU_PROCESS_LOAD_XID = "internal_cpu_process_load";

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
        monitors.put(WATCHLIST_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(WATCHLIST_COUNT_MONITOR_ID));
        monitors.put(SCHEDULED_EVENTS_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(SCHEDULED_EVENTS_COUNT_MONITOR_ID));
        monitors.put(EXCEL_REPORTS_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(EXCEL_REPORTS_COUNT_MONITOR_ID));
        monitors.put(EXCEL_REPORT_TEMPLATES_COUNT_POINT_XID, Common.MONITORED_VALUES.getMonitor(EXCEL_REPORT_TEMPLATES_COUNT_MONITOR_ID));

        //System metrics
        monitors.put(SQL_DATABASE_SIZE_POINT_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.SQL_DATABASE_SIZE));
        monitors.put(SQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.SQL_PARTITION_USABLE_SPACE));
        monitors.put(NOSQL_DATABASE_SIZE_POINT_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.NOSQL_DATABASE_SIZE));
        monitors.put(NOSQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.NOSQL_PARTITION_USABLE_SPACE));
        monitors.put(MA_HOME_PARTITION_TOTAL_SPACE_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.MA_HOME_PARTITION_TOTAL_SPACE));
        monitors.put(MA_HOME_PARTITION_USED_SPACE_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.MA_HOME_PARTITION_USED_SPACE));
        monitors.put(MA_HOME_PARTITION_USABLE_SPACE_XID, Common.MONITORED_VALUES.getMonitor(DiskUsageMonitoringService.MA_HOME_PARTITION_USABLE_SPACE));
        monitors.put(JVM_USED_MEMORY_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.USED_MEMORY_ID));
        monitors.put(JVM_MAX_MEMORY_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.MAX_MEMORY_ID));
        monitors.put(JVM_FREE_MEMORY_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.FREE_MEMORY_ID));
        monitors.put(CPU_SYSTEM_LOAD_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.OS_CPU_LOAD_SYSTEM_ID));
        monitors.put(CPU_PROCESS_LOAD_XID, Common.MONITORED_VALUES.getMonitor(ServerMonitoringService.OS_CPU_LOAD_PROCESS_ID));

        return monitors;
    }

    /**
     *
     */
    private void maybeInstallSystemMonitor(boolean safe) {
        DataSourceVO ds = DataSourceDao.getInstance().getByXid(SYSTEM_DATASOURCE_XID);
        if(ds == null){
            //Create Data Source
            DataSourceDefinition<InternalDataSourceVO> def = ModuleRegistry.getDataSourceDefinition(InternalDataSourceDefinition.DATA_SOURCE_TYPE);
            ds = def.baseCreateDataSourceVO();
            InternalDataSourceVO vo = (InternalDataSourceVO)ds;
            vo.setXid(SYSTEM_DATASOURCE_XID);
            vo.setName(SYSTEM_DATASOURCE_DEVICE_NAME);

            vo.setUpdatePeriods(10);
            vo.setUpdatePeriodType(TimePeriods.SECONDS);

            try {
                DataSourceDao.getInstance().insert(vo);

                // Setup the Points
                maybeCreatePoints(safe, ds);

                // Enable the data source
                InternalDataSourceVO existing = (InternalDataSourceVO) ds.copy();
                if (!safe) {
                    vo.setEnabled(true);
                    Common.getBean(DataSourceService.class).update(existing.getId(), vo);
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
                    dp = new DataPointVO();

                    InternalPointLocatorVO pl = new InternalPointLocatorVO();
                    pl.setMonitorId(monitor.getId());
                    dp.setPointLocator(pl);

                    dp.setXid(xid);
                    dp.setName(monitor.getName().translate(Common.getTranslations()));
                    dp.setDataSourceId(vo.getId());
                    dp.setDeviceName(vo.getName());
                    dp.defaultTextRenderer();
                    dp.setEnabled(true);
                    dp.setChartColour("");

                    if (pl.getDataType() == DataTypes.NUMERIC) {
                        dp.setRollup(Rollups.AVERAGE);
                        switch(xid) {
                            case SQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID:
                            case NOSQL_DATABASE_PARTITION_USABLE_SPACE_POINT_XID:
                            case MA_HOME_PARTITION_TOTAL_SPACE_XID:
                            case MA_HOME_PARTITION_USED_SPACE_XID:
                            case MA_HOME_PARTITION_USABLE_SPACE_XID:
                            case SQL_DATABASE_SIZE_POINT_XID:
                            case NOSQL_DATABASE_SIZE_POINT_XID:
                                dp.setUnit(Common.GIBI(NonSI.BYTE));
                                dp.setLoggingType(LoggingTypes.ON_CHANGE);
                                dp.setTextRenderer(new AnalogRenderer("0.0", " GiB", false));
                                break;
                            case CPU_SYSTEM_LOAD_XID:
                            case CPU_PROCESS_LOAD_XID:
                                dp.setLoggingType(LoggingTypes.ON_CHANGE);
                                dp.setTextRenderer(new AnalogRenderer("0", " %", false));
                                break;
                            case JVM_USED_MEMORY_XID:
                            case JVM_MAX_MEMORY_XID:
                            case JVM_FREE_MEMORY_XID:
                                dp.setUnit(Common.MEBI(NonSI.BYTE));
                                dp.setLoggingType(LoggingTypes.ON_CHANGE);
                                dp.setTextRenderer(new AnalogRenderer("0", " MiB", false));
                                break;
                            case SYSTEM_UPTIME_POINT_XID:
                                dp.setUnit(NonSI.HOUR);
                                //This value changes often, log interval instance
                                dp.setLoggingType(LoggingTypes.INTERVAL);
                                dp.setIntervalLoggingPeriodType(TimePeriods.MINUTES);
                                dp.setIntervalLoggingPeriod(5);
                                dp.setIntervalLoggingType(IntervalLoggingTypes.INSTANT);
                                dp.setTextRenderer(new AnalogRenderer("0.0", " hrs", false));
                                break;
                            default:
                                //If we are numeric then we want to log on change
                                dp.setLoggingType(LoggingTypes.ON_CHANGE);
                                dp.setTextRenderer(new AnalogRenderer("0", "", false));
                                break;
                        }
                    }

                    DataPointDao.getInstance().insert(dp);
                    if(!safe) {
                        Common.runtimeManager.startDataPoint(new DataPointWithEventDetectors(dp, new ArrayList<>()));
                    }
                }
            }
        }
    }
}
