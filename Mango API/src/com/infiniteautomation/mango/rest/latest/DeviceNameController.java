/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.db.DataPointTableDefinition;
import com.infiniteautomation.mango.spring.db.DataSourceTableDefinition;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Jared Wiltshire
 */
@Api(value="Device Names")
@RestController
@RequestMapping("/device-names")
public class DeviceNameController {

    private final DataPointService service;
    private final DataPointTableDefinition dataPointTable;
    private final DataSourceTableDefinition dataSourceTable;

    @Autowired
    public DeviceNameController(DataPointService service,
            DataPointTableDefinition dataPointTable,
            DataSourceTableDefinition dataSourceTable) {
        this.service = service;
        this.dataPointTable = dataPointTable;
        this.dataSourceTable = dataSourceTable;
    }

    @ApiOperation(
            value = "List device names",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET)
    public Set<String> deviceNames(@RequestParam(value="contains", required=false) String contains,
            @AuthenticationPrincipal PermissionHolder user) {
        Field<String> deviceName = dataPointTable.getAlias("deviceName");
        Condition conditions;
        if(StringUtils.isEmpty(contains)) {
            conditions = DSL.trueCondition();
        }else {
            conditions = deviceName.likeRegex(".*" + contains + ".*");
        }

        Set<String> deviceNames = new HashSet<>();
        service.queryDeviceNames(conditions, true, null, null, (name, index) -> deviceNames.add(name));

        return deviceNames;
    }

    @ApiOperation(
            value = "List device names by data source ID",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET, value = "/by-data-source-id/{id}")
    public Set<String> deviceNamesByDataSourceId(
            @PathVariable int id,
            @RequestParam(value="contains", required=false) String contains) {
        Field<String> deviceName = dataPointTable.getAlias("deviceName");
        Field<Integer> dataSourceId = dataPointTable.getAlias("dataSourceId");
        Condition conditions;
        if(StringUtils.isEmpty(contains)) {
            conditions = DSL.trueCondition();
        }else {
            conditions = DSL.and(deviceName.likeRegex(".*" + contains + ".*"), dataSourceId.eq(id));
        }

        Set<String> deviceNames = new HashSet<>();
        service.queryDeviceNames(conditions, true, null, null, (name, index) -> deviceNames.add(name));

        return deviceNames;
    }

    @ApiOperation(
            value = "List device names by data source XID",
            response = String.class,
            responseContainer = "Set")
    @RequestMapping(method = RequestMethod.GET, value = "/by-data-source-xid/{xid}")
    public Set<String> deviceNamesByDataSourceXid(
            @PathVariable String xid,
            @RequestParam(value="contains", required=false) String contains) {
        Field<String> deviceName = dataPointTable.getAlias("deviceName");
        Field<String> dataSourceXid = dataSourceTable.getAlias("xid");
        Condition conditions;
        if(StringUtils.isEmpty(contains)) {
            conditions = DSL.trueCondition();
        }else {
            conditions = DSL.and(deviceName.likeRegex(".*" + contains + ".*"), dataSourceXid.eq(xid));
        }

        Set<String> deviceNames = new HashSet<>();
        service.queryDeviceNames(conditions, true, null, null, (name, index) -> deviceNames.add(name));

        return deviceNames;
    }
}
