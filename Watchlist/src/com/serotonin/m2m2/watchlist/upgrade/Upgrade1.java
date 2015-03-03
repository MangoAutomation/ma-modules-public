package com.serotonin.m2m2.watchlist.upgrade;

import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade1 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
        List<Map<String, Object>> rows = ejt.queryForList("SELECT id, parentId, name FROM pointHierarchy");
        for (Map<String, Object> row : rows)
            ejt.update("INSERT INTO dataPointHierarchy (id, parentId, name) VALUES (?,?,?)", row.get("id"),
                    row.get("parentId"), row.get("name"));

        // Copy hierarchy values to the dataPoints table.
        rows = ejt.queryForList("SELECT dataPointId, folderId FROM pointFolders");
        for (Map<String, Object> row : rows)
            ejt.update("UPDATE dataPoints SET pointFolderId=? WHERE id=?", row.get("folderId"), row.get("dataPointId"));

        ejt.execute("DROP TABLE pointFolders");
        ejt.execute("DROP TABLE pointHierarchy");
    }

    @Override
    protected String getNewSchemaVersion() {
        return "2";
    }
}
