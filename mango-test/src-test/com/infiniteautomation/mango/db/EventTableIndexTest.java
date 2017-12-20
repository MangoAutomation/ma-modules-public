/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.serotonin.log.LogStopWatch;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MockEventManager;
import com.serotonin.m2m2.MockMangoLifecycle;
import com.serotonin.m2m2.MockMangoProperties;
import com.serotonin.m2m2.db.AbstractDatabaseProxy;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.provider.Providers;
import com.serotonin.timer.SimulationTimer;

/**
 *
 * @author Terry Packer
 */
public class EventTableIndexTest {
    
    private DummyExtractor extractor =  new DummyExtractor();

    public static void main(String[] args) throws Exception {
        ConfigurationSource source = new ConfigurationSource(MangoTestBase.class.getClass().getResource("/test-log4j2.xml").openStream());
        Configurator.initialize(null, source);
        EventTableIndexTest test = new EventTableIndexTest();
        
        //Add indexes
//        test.addTypeRef1Index();
//        test.addTypeNameIndex();
//        test.addRtnTsIndex();
//        test.addRtnApplicableIndex();
        
//        //Init the web console
//        String webArgs[] = new String[4];
//        webArgs[0] = "-webPort";
//        webArgs[1] = "8081";
//        webArgs[2] = "-ifExists";
//        webArgs[3] = "-webAllowOthers";
//        try {
//            Server web = Server.createWebServer(webArgs);
//            web.start();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        
        
        test.testQueries();
    }
    
    public EventTableIndexTest() {
        MockMangoProperties properties = new MockMangoProperties();
        configureH2(properties);
        
        Providers.add(IMangoLifecycle.class, new MockMangoLifecycle(new ArrayList<>()));
        Common.MA_HOME = ".." + File.separator +  ".." + File.separator + "ma-core-public" + File.separator + "Core";
        Common.envProps = properties;
        
        Common.eventManager = new MockEventManager();
        Common.timer = new SimulationTimer();
        
        
        Common.databaseProxy = AbstractDatabaseProxy.createDatabaseProxy();
        Common.databaseProxy.initialize(null);
        AuditEventType.initialize();
    }
    
    public void configureH2(MockMangoProperties properties) {
        properties.setDefaultValue("db.type", "h2");
        properties.setDefaultValue("db.url", "jdbc:h2:./test-databases/copy/mah2");
        properties.setDefaultValue("db.username", "mango");
        properties.setDefaultValue("db.password", "mango");
    }
    
    public void testQueries() {
        String query = getQueryActive(0);
        LogStopWatch timer = new LogStopWatch();
        EventDao.instance.query(query, extractor);        
        timer.logInfo(query, 0);

        query = getSortActive(15, 0);
        timer = new LogStopWatch();
        EventDao.instance.query(query, extractor);        
        timer.logInfo(query, 0);

        query = countUnAcknowleged();
        timer = new LogStopWatch();
        EventDao.instance.query(query, extractor);        
        timer.logInfo(query, 0);        
    }
    
    public String getQueryActive(int limit) {
        return "SELECT COUNT(DISTINCT evt.id) FROM events AS evt "
                + " LEFT JOIN  users u  ON evt.ackUserId = u.id "
                + "LEFT JOIN  userEvents ue  ON evt.id=ue.eventId "
                + "WHERE ( typeName = 'DATA_POINT' AND typeRef1 = 1000  AND evt.ackTs IS null AND ue.userId=1 ) ";
    }
    public String getSortActive(int limit, int offset) {
        return "SELECT evt.id,evt.typeName,evt.subtypeName,evt.typeRef1,evt.typeRef2,evt.activeTs,"
                + "evt.rtnApplicable,evt.rtnTs,evt.rtnCause,evt.alarmLevel,evt.message,evt.ackTs,"
                + "evt.ackUserId,evt.alternateAckSource,u.username,"
                + "(select count(1) from userComments where commentType=1 and typeKey=evt.id) as cnt ,ue.silenced FROM events AS evt "
                + "LEFT JOIN  users u  ON evt.ackUserId = u.id "
                + "LEFT JOIN  userEvents ue  ON evt.id=ue.eventId "
                + "WHERE  (  typeName = 'DATA_POINT' AND typeRef1 = 1000  AND ue.userId=1 ) ORDER BY  activeTs  DESC LIMIT 15 OFFSET 0 ";
    }
    public String countUnAcknowleged() {
        return "SELECT COUNT(DISTINCT evt.id) FROM events AS evt  "
                + "LEFT JOIN  users u ON evt.ackUserId = u.id "
                + "LEFT JOIN  userEvents ue  ON evt.id=ue.eventId "
                + "WHERE (  typeName = 'DATA_POINT' AND typeRef1 = 1000  AND evt.rtnTs IS null  AND evt.rtnApplicable='Y'  AND ue.userId=1 )";
    }
   
    public void addTypeRef1Index() throws Exception {
        Common.databaseProxy.runScript(new String[] {"CREATE INDEX events_performance2 ON events (`typeRef1` ASC);"}, System.out);
    }
    public void addTypeNameIndex() throws Exception {
        Common.databaseProxy.runScript(new String[] {"CREATE INDEX events_performance3 ON events (`typeName` ASC);"}, System.out);
    }
    public void addRtnTsIndex() throws Exception {
        Common.databaseProxy.runScript(new String[] {"CREATE INDEX events_performance4 ON events (`rtnTs` ASC);"}, System.out);
    }
    public void addRtnApplicableIndex() throws Exception {
        Common.databaseProxy.runScript(new String[] {"CREATE INDEX events_performance5 ON events (`rtnApplicable` ASC);"}, System.out);
    }
    //What is this query?
    //SELECT COUNT(DISTINCT evt.id) FROM events  AS evt  LEFT JOIN  users u  ON evt.ackUserId = u.id LEFT JOIN  userEvents ue  ON evt.id=ue.eventId WHERE  (  typeName = 'DATA_POINT' AND typeRef1 = ?  AND ue.userId=?  ) 
    
    class DummyExtractor implements ResultSetExtractor<EventInstanceVO>{
        @Override
        public EventInstanceVO extractData(ResultSet arg0) throws SQLException, DataAccessException {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    
}
