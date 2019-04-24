/**
 * Copyright (C) 2018 Infinite Automation Systems, Inc. All rights reserved
 * 
 */
package com.serotonin.m2m2.reports.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.text.ConvertingRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.util.SerializationHelper;

/**
 * Move units out into their own columns, this will lose any unit information currently in the text renderer blob of this table.
 *
 * @author Terry Packer
 */
public class Upgrade4 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgres);
        runScript(scripts);
        
        //Now fill in the units for all report points
        this.ejt.query("SELECT rp.id, dp.data FROM reportInstancePoints AS rp JOIN dataPoints dp ON dp.xid=rp.xid", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int id = rs.getInt(1);
                DataPointVO dp = (DataPointVO) SerializationHelper.readObjectInContext(rs.getBinaryStream(2));
                dp.ensureUnitsCorrect();
                
                if(dp.getTextRenderer() instanceof ConvertingRenderer) {
                    String unit = UnitUtil.formatUcum(dp.getUnit());
                    String renderedUnit = UnitUtil.formatUcum(dp.getRenderedUnit());
                    ejt.update("UPDATE reportInstancePoints SET unit=?,renderedUnit=? WHERE id=?", new Object[] {unit, renderedUnit, id});
                }
            }
            
        });
    }

    @Override
    protected String getNewSchemaVersion() {
        return "5";
    }

    private final String[] mysql = new String[] {
            "ALTER TABLE reportInstancePoints ADD COLUMN unit VARCHAR(255);",
            "ALTER TABLE reportInstancePoints ADD COLUMN renderedUnit VARCHAR(255);"
    };
    private final String[] h2 = new String[] {
            "ALTER TABLE reportInstancePoints ADD COLUMN unit VARCHAR(255);",
            "ALTER TABLE reportInstancePoints ADD COLUMN renderedUnit VARCHAR(255);"
    };
    private final String[] mssql = new String[] {
            "ALTER TABLE reportInstancePoints ADD COLUMN unit NVARCHAR(255);",
            "ALTER TABLE reportInstancePoints ADD COLUMN renderedUnit NVARCHAR(255);"
    };
    private final String[] postgres = new String[] {
            "ALTER TABLE reportInstancePoints ADD COLUMN unit VARCHAR(255);",
            "ALTER TABLE reportInstancePoints ADD COLUMN renderedUnit VARCHAR(255);"
    };
}