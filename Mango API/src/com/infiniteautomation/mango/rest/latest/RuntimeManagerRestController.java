/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RTException;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Runtime Manager", description="Operations on Data Source Runtime Manager")
@RestController(value="RuntimeManagerRestController")
@RequestMapping("/runtime-manager")
public class RuntimeManagerRestController {

    private final DataPointService service;

    @Autowired
    public RuntimeManagerRestController(DataPointService service) {
        this.service = service;
    }

    @ApiOperation(
            value = "Force Refresh a data point",
            notes = "Not all data sources implement this feature",
            response=Void.class
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/force-refresh/{xid}")
    public void forceRefreshDataPoint(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user){
        DataPointVO vo = service.get(xid);
        service.ensureReadPermission(user, vo);
        DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getId());
        if(rt == null){
            throw new TranslatableIllegalStateException(new TranslatableMessage("rest.error.pointNotEnabled", xid));
        }
        Common.runtimeManager.forcePointRead(vo.getId());
    }

    @ApiOperation(
            value = "Relinquish the value of a data point",
            notes = "Only BACnet data points allow this",
            response=Void.class
            )
    @RequestMapping(method = RequestMethod.POST, value = "/relinquish/{xid}")
    public void relinquish(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            HttpServletRequest request){

        DataPointVO dataPoint = service.get(xid);
        service.ensureReadPermission(user, dataPoint);

        DataPointRT rt = Common.runtimeManager.getDataPoint(dataPoint.getId());
        if(rt == null) {
            throw new TranslatableIllegalStateException(new TranslatableMessage("rest.error.pointNotEnabled", xid));
        }

        //Get the Data Source and Relinquish the point
        DataSourceRT<?> dsRt = null;
        try {
            dsRt = Common.runtimeManager.getRunningDataSource(rt.getDataSourceId());
        } catch (RTException e) {
            throw new TranslatableIllegalStateException(new TranslatableMessage("rest.error.dataSourceNotEnabled", xid));
        }

        dsRt.relinquish(rt);
    }

    @ApiOperation(
            value = "Reset the cache on a running data point",
            notes = "Must have edit access to the data point",
            response=Void.class
    )
    @RequestMapping(method = RequestMethod.POST, value = "/reset-cache/{xid}")
    public void resetCache(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            HttpServletRequest request){

        DataPointVO vo = service.get(xid);
        service.ensureEditPermission(user, vo);
        DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getId());
        if(rt == null){
            throw new TranslatableIllegalStateException(new TranslatableMessage("rest.error.pointNotEnabled", xid));
        }
        rt.resetValues();
    }
}
