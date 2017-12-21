/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MockMangoProperties;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Open up a database and put tags on all the datapoints
 *
 * @author Terry Packer
 */
public class DataPointTagger extends AbstractMangoTestTool {

    
    public static void main(String[] args) throws Exception {
        ConfigurationSource source = new ConfigurationSource(MangoTestBase.class.getClass().getResource("/test-log4j2.xml").openStream());
        Configurator.initialize(null, source);
    
        DataPointTagger tagger = new DataPointTagger();
        tagger.initialize(DatabaseType.MYSQL);
        tagger.addTags(100);
        
        Common.databaseProxy.terminate(false);
    }
    
    public DataPointTagger() {
        super();
    }
    
    @Override
    public void configureH2(MockMangoProperties properties, boolean clean) {
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
