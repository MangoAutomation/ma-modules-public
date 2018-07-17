/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

/**
 * 
 * Upgrade to remove dataSourceId from the maintenenceEvents table and replace
 * with 2 mapping tables.  A mapping of dataSourceIds and a mapping of dataPointIds
 * to allow linking a list of data sources or points.  Validation will ensure at least 
 * one point or data source is setup.
 *
 * @author Terry Packer
 */
public class Upgrade2 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlCreateTables);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2CreateTables);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlCreateTables);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgresCreateTables);
        runScript(scripts);
        
        //Copy over the dataSourceId info to new table before we delete it
        this.ejt.query("SELECT id,dataSourceId FROM maintenanceEvents", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int id = rs.getInt(1);
                int dataSourceId = rs.getInt(2);
                ejt.update("INSERT INTO maintenanceEventDataSources (maintenanceEventId, dataSourceId) VALUES (?,?)", new Object[] {id, dataSourceId});
            }
            
        });
        
        //Drop the dataSourceId
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlDropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2DropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlDropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgresDropDataSourceId);
        runScript(scripts);

    }

    @Override
    protected String getNewSchemaVersion() {
        return "3";
    }

    private final String[] mysqlCreateTables = new String[] {
            "CREATE TABLE maintenanceEventPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL) engine=InnoDB;",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",
            
            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL) engine=InnoDB;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;"

    };
    private final String[] h2CreateTables = new String[] {
            "CREATE TABLE maintenanceEventPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",

            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;"

    };
    private final String[] mssqlCreateTables = new String[] {
            "CREATE TABLE maintenanceEventPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",            

            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;"

    };
    private final String[] postgresCreateTables = new String[] {
            "CREATE TABLE maintenanceEventPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventPoints add constraint maintenanceEventPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",
            
            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;"

    };
    
    private final String[] mysqlDropDataSourceId = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN dataSourceId;",
            "ALTER TABLE maintenanceEvents DROP FOREIGN KEY maintenanceEventsFk1;"
    };
    
    private final String[] h2DropDataSourceId = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN dataSourceId;",
            "ALTER TABLE maintenanceEvents DROP CONSTRAINT maintenanceEventsFk1;"
    };

    private final String[] mssqlDropDataSourceId = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN dataSourceId;",
            "ALTER TABLE maintenanceEvents DROP CONSTRAINT maintenanceEventsFk1;"
    };

    private final String[] postgresDropDataSourceId = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN dataSourceId;",
            "ALTER TABLE maintenanceEvents DROP FOREIGN KEY maintenanceEventsFk1;"
    };
    
}
