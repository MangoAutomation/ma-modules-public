/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.definitions.permissions.SystemMetricsReadPermissionDefinition;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Allows access to all System Metrics that are Monitored.
 *
 * There are many core metrics and modules may also install thier own metrics.
 *
 *
 * @author Terry Packer
 */
@Api(value="System Metrics, access to the current value for any System Metric")
@RestController
@RequestMapping("/system-metrics")
public class SystemMetricsRestController {

    //Permissions Definition for Internal Metrics
    private final PermissionService service;
    private final SystemMetricsReadPermissionDefinition definition;
    @Autowired
    public SystemMetricsRestController(PermissionService service) {
        this.service = service;
        this.definition = (SystemMetricsReadPermissionDefinition) ModuleRegistry.getPermissionDefinition(SystemMetricsReadPermissionDefinition.PERMISSION);
    }

    @ApiOperation(
            value = "Get the current value for all System Metrics",
            notes = "TBD Add RQL Support to this endpoint"
            )
    @RequestMapping(method = RequestMethod.GET)
    public List<ValueMonitor<?>> query(@AuthenticationPrincipal User user) {
        MangoPermission permission = definition.getPermission();
        service.ensurePermission(user, permission);
        return Common.MONITORED_VALUES.getMonitors();
    }

    @ApiOperation(
            value = "Get the current value for one System Metric by its ID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value="/{id}")
    public ValueMonitor<?> get(
            @ApiParam(value = "Valid Monitor id", required = true, allowMultiple = false)
            @PathVariable String id,
            @AuthenticationPrincipal User user) {

        MangoPermission permission = definition.getPermission();
        service.ensurePermission(user, permission);

        List<ValueMonitor<?>> values = Common.MONITORED_VALUES.getMonitors();
        for(ValueMonitor<?> v : values){
            if(v.getId().equals(id)){
                return v;
            }
        }
        throw new NotFoundException();
    }
}
