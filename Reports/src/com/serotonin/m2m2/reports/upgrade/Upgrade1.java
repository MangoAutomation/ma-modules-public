package com.serotonin.m2m2.reports.upgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.mango.spring.dao.ReportDao;
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
    	ReportDao dao = ReportDao.instance;
    	List<ReportVO> reports = dao.getReports();
    	for(ReportVO report : reports){
    		report.setXid(dao.generateUniqueXid());
    		dao.saveReport(report);
    	}
    	
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
