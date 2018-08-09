package com.serotonin.m2m2.reports.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.dao.SchemaDefinition;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.module.DatabaseSchemaDefinition;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;

/**
 * Upgrade Schema 2 to 3
 * @author Phillip Dunlap
 *
 */
public class Upgrade2 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
    	runScript(new String[] { "alter table reportInstances add template varchar(40);",
    		    "alter table reportInstances add reportId int;",
    			"update reportInstances set reportId=-1;",
    			"alter table reportInstances add mapping blob;",
    			"alter table reportInstancePoints add xid varchar(50);",
    			"update reportInstancePoints set xid='legacyReport';"});
    	
    	//Get all users
        ejt.query("SELECT id FROM " + SchemaDefinition.USERS_TABLE, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet userRs) throws SQLException {
                ejt.query("SELECT id,name FROM reportInstances WHERE userId=" + userRs.getInt(1), new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet instanceRs) throws SQLException {
                        //Get all reports with this name and update the instance with the report id
                        ejt.query("SELECT id FROM " + ReportDao.TABLE_NAME + " WHERE name='" + instanceRs.getString(2), new RowCallbackHandler() {
                            @Override
                            public void processRow(ResultSet reportRs) throws SQLException {
                                //Update the instances reportId with the reportId
                                ejt.update("UPDATE reportInstances SET reportId=? WHERE id=?", new Object[] {reportRs.getInt(1), instanceRs.getInt(1)} );
                            }
                        });
                    }
                });
            }
        });
    	
    	//Alter the column back to have no default
        Map<String, String[]> scripts = new HashMap<String, String[]>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), new String[] { 
        	"alter table reportInstances alter column reportId NOT NULL; ",
        	"alter table reportInstancePoints alter column xid NOT NULL;"});
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), new String[] { 
        	"ALTER TABLE reportInstances CHANGE COLUMN `reportId` `reportId` int NOT NULL;",
        	"ALTER TABLE reportInstancePoints CHANGE COLUMN `xid` `xid` VARCHAR(50) NOT NULL;"});
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), new String[] { 
        	"alter table reportInstances alter column reportId int NOT NULL;",
    		"alter table reportInstancePoints alter column xid varchar(50) NOT NULL;" });
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), new String[] { 
        	"alter table reportInstances alter column reportId int NOT NULL;",
    		"alter table reportInstancePoints alter column xid varchar(50) NOT NULL;" });
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "3";
    }
}
