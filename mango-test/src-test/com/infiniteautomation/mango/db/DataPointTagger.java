/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.infiniteautomation.mangoApi.websocket.DataPointWebSocketDefinition;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MangoTestModule;
import com.serotonin.m2m2.MockEventManager;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockMangoProperties;
import com.serotonin.m2m2.MockRuntimeManager;
import com.serotonin.m2m2.db.AbstractDatabaseProxy;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.spring.MangoRestSpringConfiguration;
import com.serotonin.provider.Providers;
import com.serotonin.timer.SimulationTimer;

/**
 * Open up a database and put tags on all the datapoints
 *
 * @author Terry Packer
 */
public class DataPointTagger {

    
    public static void main(String[] args) throws Exception {
        ConfigurationSource source = new ConfigurationSource(MangoTestBase.class.getClass().getResource("/test-log4j2.xml").openStream());
        Configurator.initialize(null, source);
    
        DataPointTagger tagger = new DataPointTagger();
        tagger.addTags(100);
        
        Common.databaseProxy.terminate(false);
    }
    
    public DataPointTagger() {
        
        //Add in the Dao Notification Web Socket Handler
        MangoTestModule module = new MangoTestModule("DataPointTagger");
        module.addDefinition(new DataPointWebSocketDefinition());
        ModuleRegistry.addModule(module);
        
        MockMangoProperties properties = new MockMangoProperties();
        configureH2(properties);
        
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
    }
    
    public void configureH2(MockMangoProperties properties) {
        properties.setDefaultValue("db.type", "h2");
        properties.setDefaultValue("db.url", "jdbc:h2:./test-databases/copy/mah2");
        properties.setDefaultValue("db.username", "mango");
        properties.setDefaultValue("db.password", "mango");
        properties.setDefaultValue("db.h2.shutdownCompact", "true");
    }
    
    public void addTags(int count) {
        Map<String,String> tags = new HashMap<>();
        for(int i=0; i<count; i++) {
            tags.put("tag" + i, "value" + i);
        }
        DataPointDao.instance.getAll(new MappedRowCallback<DataPointVO>() {

            @Override
            public void row(DataPointVO dataPoint, int index) {
                if(index % 100 == 0)
                    System.out.println("Processing Data Point " + index);
                dataPoint.setTags(tags);
                DataPointTagsDao.instance.saveDataPointTags(dataPoint);
            }
            
        });
    }
    
}
