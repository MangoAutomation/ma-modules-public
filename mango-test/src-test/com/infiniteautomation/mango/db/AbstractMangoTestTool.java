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
import java.util.List;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.infiniteautomation.mangoApi.websocket.DataPointWebSocketDefinition;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MangoTestModule;
import com.serotonin.m2m2.MockEventManager;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockMangoProperties;
import com.serotonin.m2m2.MockRuntimeManager;
import com.serotonin.m2m2.db.AbstractDatabaseProxy;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
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
import com.serotonin.m2m2.web.mvc.spring.MangoRestSpringConfiguration;
import com.serotonin.provider.Providers;
import com.serotonin.timer.SimulationTimer;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractMangoTestTool {

    private List<User> users;
    
    public AbstractMangoTestTool() {
        this.users = new ArrayList<>();
    }
    
    public void initialize(DatabaseType type) throws Exception{
        ConfigurationSource source = new ConfigurationSource(MangoTestBase.class.getClass().getResource("/test-log4j2.xml").openStream());
        Configurator.initialize(null, source);

        //Add in the Dao Notification Web Socket Handler
        MangoTestModule module = new MangoTestModule("DataPointTagger");
        module.addDefinition(new DataPointWebSocketDefinition());
        ModuleRegistry.addModule(module);
        
        MockMangoProperties properties = new MockMangoProperties();
        switch(type) {
            case MYSQL:
                configureMySQL(properties);
                break;
            case H2:
                configureH2(properties, true);
                break;
            default:
                throw new ShouldNeverHappenException("unsupported database");
        }
        
        Providers.add(IMangoLifecycle.class, new MockMangoLifecycle(new ArrayList<>()));
        Common.MA_HOME = ".." + File.separator +  ".." + File.separator + "ma-core-public" + File.separator + "Core";
        Common.envProps = properties;
        
        Common.eventManager = new MockEventManager();
        Common.timer = new SimulationTimer();
        Common.runtimeManager = new MockRuntimeManager();

        //Setup Object Mapper
        MangoRestSpringConfiguration.initializeObjectMapper();
        
        Common.databaseProxy = AbstractDatabaseProxy.createDatabaseProxy();
        Common.databaseProxy.initialize(null);
        AuditEventType.initialize();
        
        //Add Admin User
        User admin = UserDao.instance.getUser("admin");
        if(admin == null) {
            admin = new User();
            admin.setName("admin");
            admin.setEmail("admin@admin.com");
            admin.setUsername("admin");
            admin.setPassword(Common.encrypt("admin"));
            admin.setPermissions("superadmin");
            UserDao.instance.saveUser(admin);
        }
        users.add(admin);
    }
    
    public void configureH2(MockMangoProperties properties, boolean clean) throws IOException{
        if(clean) {
            //Delete existing database
            File dbDir = new File("test-databases");
            if(dbDir.exists())
                delete(dbDir);
        }
        properties.setDefaultValue("db.type", "h2");
        properties.setDefaultValue("db.url", "jdbc:h2:./test-databases/mah2;LOG=0;CACHE_SIZE=655360;LOCK_MODE=0;UNDO_LOG=0");
        properties.setDefaultValue("db.username", "mango");
        properties.setDefaultValue("db.password", "mango");
    }
    
    public void configureMySQL(MockMangoProperties properties) {
        properties.setDefaultValue("db.type", "mysql");
        properties.setDefaultValue("db.url", "jdbc:mysql://localhost:3306/mango");
        properties.setDefaultValue("db.username", "mango");
        properties.setDefaultValue("db.password", "mango");
    }
    
    public void generateUsers(int count) {
        for(long i=0; i<count; i++) {
            System.out.println("Generating User " + i + " of " + count);
            User user = new User();
            user.setName("User " + i);
            user.setEmail("admin@admin.com");
            user.setUsername("user" + i);
            user.setPassword(Common.encrypt("user" + i));
            user.setPermissions("user");
            
            UserDao.instance.saveUser(user);
            users.add(user);
        }
    }
    /**
     * Generate some events
     * @param eventCount
     */
    public void generateSystemEvents(int eventCount) {

        EventType system = new SystemEventType("Testing", -1);
        long timestamp = System.currentTimeMillis() - eventCount;

        for(long i=0; i<eventCount; i++) {
            if(i %1000 == 0)
                System.out.println("Generating system event " + i + " of " + eventCount);
            EventInstance event = new EventInstance(system, timestamp, true, AlarmLevels.CRITICAL, new TranslatableMessage("common.default", "Testing " + timestamp), null);
            EventDao.instance.saveEvent(event);
            timestamp++;
        }
    }
    
    public DataSourceVO<?> generateVirtualDataSource(int count, boolean dataSourceEnabled) {
        VirtualDataSourceVO ds = new VirtualDataSourceVO();
        DataSourceDefinition def = new VirtualDataSourceDefinition();
        ds = (VirtualDataSourceVO) def.baseCreateDataSourceVO();
        ds.setId(Common.NEW_ID);
        ds.setXid(DataSourceDao.instance.generateUniqueXid());
        ds.setName("Test Virtual " + count);
        ds.setEnabled(dataSourceEnabled);
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
        List<Integer> userIds = new ArrayList<>();
        for(User u : users) {
            userIds.add(u.getId());
        }
        
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
                    //Need to save a user event for this
                    EventDao.instance.insertUserEvents(event.getId(), userIds, true);
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
