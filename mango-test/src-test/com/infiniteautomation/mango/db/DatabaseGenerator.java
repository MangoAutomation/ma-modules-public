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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
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

/**
 *
 * @author Terry Packer
 */
public class DatabaseGenerator extends AbstractMangoTestTool {
    
    static final int systemEventCount = 1000000;
    
    //User settings
    static final int userCount = 10;
    
    //Data source settings
    static final int dataSourceCount = 100;
    static final boolean dataSourceEnabled = false;
    
    //Data Point settings
    static final int dataPointPerSourceCount = 500;
    static final int dataPointEventPerPointCount = 100;
    static final int dataPointTagsPerPoint = 100;
    
    protected Map<String,String> tags;
   
    
    public static void main(String[] args) throws Exception {
        DatabaseGenerator gen = new DatabaseGenerator();
        gen.initialize(DatabaseType.MYSQL);

        gen.generateUsers(userCount);
        gen.generateSystemEvents(systemEventCount);

        for(int i=0; i<dataSourceCount; i++) {
            long time = System.currentTimeMillis();
            DataSourceVO<?> ds = gen.generateVirtualDataSource(i);
            gen.generateVirtualPoints(ds, dataPointPerSourceCount, dataPointEventPerPointCount);
            System.out.println("Generating data source " + i + " of " + dataSourceCount + " took " + (System.currentTimeMillis() - time) + "ms");
        }

        Common.databaseProxy.terminate(false);
    }

    public DatabaseGenerator() {
        super();
        this.tags = new HashMap<>();
        for(int i=0; i<dataPointTagsPerPoint; i++) {
            this.tags.put("tag" + i, "value" + i);
        }
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
    
    public DataSourceVO<?> generateVirtualDataSource(int count) {
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
            
            //Add Tags
            saveTags(dp);
            
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
    
    public void saveTags(DataPointVO dataPoint) {
        dataPoint.setTags(tags);
        DataPointTagsDao.instance.saveDataPointTags(dataPoint);
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
