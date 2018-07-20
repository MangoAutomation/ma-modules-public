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
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), dropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), dropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), dropDataSourceId);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), dropDataSourceId);
        runScript(scripts);

        //Add the timeout columns
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), addTimeoutColumns);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), addTimeoutColumns);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), addTimeoutColumns);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), addTimeoutColumns);
        runScript(scripts);
        
    }

    @Override
    protected String getNewSchemaVersion() {
        return "3";
    }

    private final String[] mysqlCreateTables = new String[] {
            "CREATE TABLE maintenanceEventDataPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL) engine=InnoDB;",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",
            
            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL) engine=InnoDB;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;",
            
            "ALTER TABLE maintenanceEvents ADD COLUMN togglePermission VARCHAR(255);"

    };
    private final String[] h2CreateTables = new String[] {
            "CREATE TABLE maintenanceEventDataPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",

            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;",
            
            "ALTER TABLE maintenanceEvents ADD COLUMN togglePermission VARCHAR(255);"

    };
    private final String[] mssqlCreateTables = new String[] {
            "CREATE TABLE maintenanceEventDataPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",            

            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;",
            
            "ALTER TABLE maintenanceEvents ADD COLUMN togglePermission NVARCHAR(255);"

    };
    private final String[] postgresCreateTables = new String[] {
            "CREATE TABLE maintenanceEventDataPoints (maintenanceEventId int NOT NULL, dataPointId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;",
            
            "CREATE TABLE maintenanceEventDataSources (maintenanceEventId int NOT NULL, dataSourceId int NOT NULL);",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;",
            "ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;",
            
            "ALTER TABLE maintenanceEvents ADD COLUMN togglePermission VARCHAR(255);"
    };
    
    private final String[] dropDataSourceId = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN dataSourceId;",
    };
    private final String[] addTimeoutColumns = new String[] {
            "ALTER TABLE maintenanceEvents ADD COLUMN timeoutPeriods INT;",
            "ALTER TABLE maintenanceEvents ADD COLUMN timeoutPeriodType INT;"
    };
    
}
