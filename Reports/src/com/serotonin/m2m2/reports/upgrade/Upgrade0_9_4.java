package com.serotonin.m2m2.reports.upgrade;

import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade0_9_4 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
        runScript(new String[] { "alter table reportInstancePoints add plotType int default 1 not null;" });
    }

    @Override
    protected String getNewSchemaVersion() {
        return "1.3.0";
    }
}
