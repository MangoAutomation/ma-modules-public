/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.db.spring.ConnectionCallbackVoid;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.MediaTypes;
import com.serotonin.util.SerializationHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * TODO Convert some binary data to JSON?
 *
 * @author Terry Packer
 */
@Api(value="SQL Console Rest Controller")
@RestController()
@RequestMapping("/sql-console")
public class SqlConsoleRestController {

    @ApiOperation(
            value = "List Tables",
            notes = "List all tables in the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET, value="/list-tables")
    public ResponseEntity<SqlQueryResult> listTables(
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(query(Common.databaseProxy.getTableListQuery(), getSerializedDataMessage(user)));
    }

    @ApiOperation(
            value = "Query",
            notes = "Submit a query to the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<SqlQueryResult> query(
            @RequestParam(value="query", required=true) String query,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(query(query, getSerializedDataMessage(user)));
    }

    @ApiOperation(
            value = "Query",
            notes = "Submit a query to the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.CSV_VALUE)
    public List<Map<String, Object>> queryCsv(
            @RequestParam(value="query", required=true) String query,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        SqlQueryResult result = query(query, getSerializedDataMessage(user));
        List<String> headers = result.getHeaders();
        List<List<Object>> rows = result.getData();

        return rows.stream().map(row -> {
            Map<String, Object> resultRow = new LinkedHashMap<>(row.size());
            for (int i = 0; i < row.size(); i++) {
                Object value = row.get(i);
                String header = headers.get(i);
                resultRow.put(header, value);
            }
            return resultRow;
        }).collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Update",
            notes = "Submit an update to the Mango database, Admin Only, return number of rows affected",
            response=Integer.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST, consumes = {"application/sql"})
    public ResponseEntity<Integer> update(
            @RequestBody String update,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ExtendedJdbcTemplate ejt = new ExtendedJdbcTemplate();
        ejt.setDataSource(Common.databaseProxy.getDataSource());
        return ResponseEntity.ok(ejt.update(update));
    }

    /**
     * Get a translated message about serialized data
     * @param user
     * @return
     */
    private String getSerializedDataMessage(User user) {
        return user.getTranslations().translate("sql.serializedData");
    }

    /**
     *
     * @param sqlString
     * @param serializedDataMsg
     * @param model
     */
    private SqlQueryResult query(final String sqlString, final String serializedDataMsg) {

        SqlQueryResult result = new SqlQueryResult();
        Common.databaseProxy.doInConnection(new ConnectionCallbackVoid() {
            @Override
            public void doInConnection(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlString);

                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();
                List<String> headers = new ArrayList<String>(columns);
                for (int i = 0; i < columns; i++)
                    headers.add(meta.getColumnLabel(i + 1));

                List<List<Object>> data = new LinkedList<List<Object>>();
                List<Object> row;
                while (rs.next()) {
                    row = new ArrayList<Object>(columns);
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
                        }
                        else
                            row.add(rs.getObject(i + 1));
                    }
                }

                result.setHeaders(headers);
                result.setData(data);
            }
        });
        return result;
    }
}
