/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.serotonin.m2m2.db.dao;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.infiniteautomation.mango.rest.latest.SqlQueryResult;
import com.serotonin.db.DaoUtils;
import com.serotonin.m2m2.db.DatabaseType;
import com.serotonin.util.SerializationHelper;

@Component
public class SqlConsole extends DaoUtils {

    @Autowired
    public SqlConsole(DataSource dataSource, PlatformTransactionManager transactionManager, DatabaseType databaseType, DSLContext context) {
        super(dataSource, transactionManager, databaseType, context);
    }

    public SqlQueryResult query(String sqlString, String serializedDataMsg) {
        return this.ejt.execute((StatementCallback<SqlQueryResult>) stmt -> {
            ResultSet rs = stmt.executeQuery(sqlString);

            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            List<String> headers = new ArrayList<>(columns);
            for (int i = 0; i < columns; i++)
                headers.add(meta.getColumnLabel(i + 1));

            List<List<Object>> data = new LinkedList<>();
            List<Object> row;
            while (rs.next()) {
                row = new ArrayList<>(columns);
                data.add(row);
                for (int i = 0; i < columns; i++) {
                    if (meta.getColumnType(i + 1) == Types.CLOB)
                        row.add(rs.getString(i + 1));
                    else if (meta.getColumnType(i + 1) == Types.LONGVARBINARY
                            || meta.getColumnType(i + 1) == Types.BLOB) {
                        Blob blob = rs.getBlob(i + 1);
                        Object o;
                        if (blob == null)
                            o = null;
                        else
                            o = SerializationHelper.readObjectInContext(blob.getBinaryStream());
                        row.add(serializedDataMsg + "(" + o + ")");
                    } else
                        row.add(rs.getObject(i + 1));
                }
            }

            SqlQueryResult result = new SqlQueryResult();
            result.setHeaders(headers);
            result.setData(data);
            return result;
        });
    }
}
