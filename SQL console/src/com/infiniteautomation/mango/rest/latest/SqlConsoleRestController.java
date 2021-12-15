/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.dao.SqlConsole;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.MediaTypes;

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
@PreAuthorize("isAdmin()")
public class SqlConsoleRestController {

    private final SqlConsole sqlConsole;
    private final DatabaseProxy databaseProxy;

    @Autowired
    public SqlConsoleRestController(SqlConsole sqlConsole, DatabaseProxy databaseProxy) {
        this.sqlConsole = sqlConsole;
        this.databaseProxy = databaseProxy;
    }

    @ApiOperation(
            value = "List Tables",
            notes = "List all tables in the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/list-tables")
    public ResponseEntity<SqlQueryResult> listTables(
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(sqlConsole.query(databaseProxy.getTableListQuery(), getSerializedDataMessage(user)));
    }

    @ApiOperation(
            value = "Query",
            notes = "Submit a query to the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<SqlQueryResult> query(
            @RequestParam(value="query", required=true) String query,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(sqlConsole.query(query, getSerializedDataMessage(user)));
    }

    @ApiOperation(
            value = "Query",
            notes = "Submit a query to the Mango database, Admin Only",
            response=SqlQueryResult.class
            )
    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.CSV_VALUE)
    public List<Map<String, Object>> queryCsv(
            @RequestParam(value="query", required=true) String query,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        SqlQueryResult result = sqlConsole.query(query, getSerializedDataMessage(user));
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
    @RequestMapping(method = RequestMethod.POST, consumes = {"application/sql"})
    public ResponseEntity<Integer> update(
            @RequestBody String update,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        return ResponseEntity.ok(databaseProxy.getJdbcTemplate().update(update));
    }

    /**
     * Get a translated message about serialized data
     */
    private String getSerializedDataMessage(User user) {
        return user.getTranslations().translate("sql.serializedData");
    }
}
