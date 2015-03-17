package com.serotonin.m2m2.reports.upgrade;

import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade0_9_2 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
        runScript(new String[] { //
        "alter table reportInstancePoints add column consolidatedChart char(1);", //
                "update reportInstancePoints set consolidatedChart='Y';", //
        });
    }

    @Override
    protected String getNewSchemaVersion() {
        return "0.9.3";
    }
}
