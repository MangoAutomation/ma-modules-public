package com.serotonin.m2m2.reports.upgrade;

import com.serotonin.m2m2.db.upgrade.DBUpgrade;

/**
 * Upgrade Schema 2 to 3
 * @author Phillip Dunlap
 *
 */
public class Upgrade2 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
    	runScript(new String[] { "alter table reportInstances add template varchar(40); alter table reportInstances add reportId int not null;" });
    }

    @Override
    protected String getNewSchemaVersion() {
        return "3";
    }
}
