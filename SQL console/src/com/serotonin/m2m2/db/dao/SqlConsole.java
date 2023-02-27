/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.serotonin.m2m2.db.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.SqlQueryResult;
import com.serotonin.db.spring.ExtendedJdbcTemplate;

@Component
public class SqlConsole {

    private final ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    public SqlConsole(ExtendedJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlQueryResult query(String sqlString, String serializedDataMsg) {
        return jdbcTemplate.execute((StatementCallback<SqlQueryResult>) stmt -> {
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
                        SerialBlob blob = new SerialBlob( rs.getBlob(i + 1) );
                        row.add(serializedDataMsg + "(" + blob + ")");
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
