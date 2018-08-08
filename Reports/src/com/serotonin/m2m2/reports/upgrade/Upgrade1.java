package com.serotonin.m2m2.reports.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.infiniteautomation.mango.spring.dao.ReportDao;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.reports.vo.ReportVO;

/**
 * Upgrade Schema 1 to 2
 * @author Terry Packer
 *
 */
public class Upgrade1 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
    	runScript(new String[] { "alter table reports add xid varchar(50);" });
    	
    	//Now generate XIDs for all entries in the table
    	ejt.query("SELECT id FROM " + ReportDao.TABLE_NAME, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ejt.update("UPDATE " + ReportDao.TABLE_NAME + " SET xid=? WHERE id=?", new Object[] {Common.generateXid(ReportVO.XID_PREFIX), rs.getInt(1)} );
            }
        });
    	
    	//Alter the column back to have no default
        Map<String, String[]> scripts = new HashMap<String, String[]>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), new String[] { "alter table reports alter column xid NOT NULL;" });
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), new String[] { "ALTER TABLE reports CHANGE COLUMN `xid` `xid` VARCHAR(50) NOT NULL;" });
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), new String[] { "alter table reports alter column xid varchar(50) NOT NULL;" });
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), new String[] { "alter table reports alter column xid varchar(50) NOT NULL;" });
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "2";
    }
}
