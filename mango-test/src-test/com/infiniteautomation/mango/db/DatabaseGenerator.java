/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.MockEventManager;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockMangoProperties;
import com.serotonin.m2m2.db.AbstractDatabaseProxy;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.definitions.event.detectors.AnalogChangeEventDetectorDefinition;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.EventType.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.view.chart.ImageChartRenderer;
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AnalogChangeDetectorVO;
import com.serotonin.provider.Providers;
import com.serotonin.timer.SimulationTimer;

/**
 *
 * @author Terry Packer
 */
public class DatabaseGenerator {
    
    public static void main(String[] args) {
        
        DatabaseGenerator gen = new DatabaseGenerator();

        gen.generateSystemEvents(1000000);

        for(int i=0; i<50; i++) {
            DataSourceVO<?> ds = gen.generateVirtualDataSource();
            gen.generateVirtualPoints(ds, 100, 100);
        }

        Common.databaseProxy.terminate(false);
    }

    public DatabaseGenerator() {
        
        //Delete existing database
        try {
            File dbDir = new File("test-databases");
            if(dbDir.exists())
                delete(dbDir);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        MockMangoProperties properties = new MockMangoProperties();
        properties.setDefaultValue("db.type", "h2");
        properties.setDefaultValue("db.url", "jdbc:h2:./test-databases/mah2");
        properties.setDefaultValue("db.username", "mango");
        properties.setDefaultValue("db.password", "mango");
        
        Providers.add(IMangoLifecycle.class, new MockMangoLifecycle(new ArrayList<>()));
        Common.MA_HOME = ".." + File.separator +  ".." + File.separator + "ma-core-public" + File.separator + "Core";
        Common.envProps = properties;
        
        Common.eventManager = new MockEventManager();
        Common.timer = new SimulationTimer();
        
        
        Common.databaseProxy = AbstractDatabaseProxy.createDatabaseProxy();
        Common.databaseProxy.initialize(null);
        AuditEventType.initialize();
        
        //Add Admin User
        User admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@admin.com");
        admin.setUsername("admin");
        admin.setPassword(Common.encrypt("admin"));
        admin.setPermissions("superadmin");
        
        UserDao.instance.saveUser(admin);
        
    }
    
    /**
     * Generate some events
     * @param eventCount
     */
    public void generateSystemEvents(int eventCount) {

        EventType system = new SystemEventType("Testing", -1);
        long timestamp = System.currentTimeMillis() - eventCount;

        for(long i=0; i<eventCount; i++) {
            EventInstance event = new EventInstance(system, timestamp, true, AlarmLevels.CRITICAL, new TranslatableMessage("common.default", "Testing " + timestamp), null);
            EventDao.instance.saveEvent(event);
            timestamp++;
        }
    }
    
    public DataSourceVO<?> generateVirtualDataSource() {
        VirtualDataSourceVO ds = new VirtualDataSourceVO();
        DataSourceDefinition def = new VirtualDataSourceDefinition();
        ds = (VirtualDataSourceVO) def.baseCreateDataSourceVO();
        ds.setId(Common.NEW_ID);
        ds.setXid(DataSourceDao.instance.generateUniqueXid());
        ds.setName("Test Virtual");
        ds.setEnabled(true);
        ds.setUpdatePeriods(5);
        ds.setUpdatePeriodType(TimePeriods.SECONDS);
        ds.setPolling(true);
        
        ProcessResult response = new ProcessResult();
        ds.validate(response);
        if(response.getHasMessages()) {
            throw new RuntimeException("Invalid Virtual DS Configuration");
        }
        
        DataSourceDao.instance.save(ds);
        return ds;
    }
    
    public void generateVirtualPoints(DataSourceVO<?> ds, int count, int eventCount) {
        
        for(int i=0; i<count; i++) {
            ProcessResult response = new ProcessResult();
            VirtualPointLocatorVO pointLocator = new VirtualPointLocatorVO();
            
            //Create a Random Points
            pointLocator.setDataTypeId(DataTypes.NUMERIC);
            pointLocator.setChangeTypeId(ChangeTypeVO.Types.RANDOM_ANALOG);
            pointLocator.getRandomAnalogChange().setMin(0);
            pointLocator.getRandomAnalogChange().setMax(100);
            pointLocator.getRandomAnalogChange().setStartValue("1");
            pointLocator.setSettable(true);
            
            
            DataPointVO dp = new DataPointVO();
            dp.setXid(DataPointDao.instance.generateUniqueXid());
            dp.setName(ds.getId() + " Virtual Random " + i);
            dp.setDataSourceId(ds.getId());
            dp.setDataSourceTypeName(ds.getDefinition().getDataSourceTypeName());
            dp.setDeviceName(ds.getName());
            dp.setEventDetectors(new ArrayList<AbstractPointEventDetectorVO<?>>(0));
            dp.defaultTextRenderer();
            //Setup the Chart Renderer
            ImageChartRenderer chartRenderer = new ImageChartRenderer(TimePeriods.DAYS, 5);
            dp.setChartRenderer(chartRenderer);
            
            dp.setPointLocator(pointLocator);
            dp.setEnabled(true);
            dp.setSettable(true);
            dp.setDefaultCacheSize(0);          
            
            dp.validate(response);

            if(response.getHasMessages())
                throw new RuntimeException("Invalid Virtual PL Configuration");
            
            
            AnalogChangeDetectorVO ed = null;
            if(eventCount > 0) {
                ed = new AnalogChangeDetectorVO();
                ed.setDefinition(new AnalogChangeEventDetectorDefinition());
                ed.setLimit(0.01d);
                ed.setCheckIncrease(true);
                ed.setDuration(10);
                ed.setDurationType(TimePeriods.SECONDS);
                ed.setXid(EventDetectorDao.instance.generateUniqueXid());
                ed.setName("");
                ed.setSourceId(dp.getId());
                ed.njbSetDataPoint(dp);
                dp.getEventDetectors().add(ed);
            }
            
            DataPointDao.instance.saveDataPoint(dp);
            
            if(eventCount > 0) {
                //Need to add an event detector
                EventType type = new DataPointEventType(ds.getId(), dp.getId(), ed.getId(), DuplicateHandling.ALLOW);
                long timestamp = System.currentTimeMillis() - eventCount;
    
                for(long j=0; j<eventCount; j++) {
                    EventInstance event = new EventInstance(type, timestamp, true, AlarmLevels.CRITICAL, new TranslatableMessage("common.default", "Changing " + timestamp), null);
                    EventDao.instance.saveEvent(event);
                    timestamp++;
                }
            }

        }
    }
    
    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!Files.deleteIfExists(f.toPath()))
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
    
}
