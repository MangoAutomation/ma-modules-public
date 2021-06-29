/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.model.workitem.BackgroundProcessingQueueCounts;
import com.infiniteautomation.mango.rest.latest.model.workitem.BackgroundProcessingRejectedTaskStats;
import com.infiniteautomation.mango.rest.latest.model.workitem.BackgroundProcessingRunningStats;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.WorkItemInfo;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Mango Work Items")
@RestController
@RequestMapping("/work-items")
public class WorkItemRestController {

    private final PermissionService permissionService;

    @Autowired
    public WorkItemRestController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @ApiOperation(value = "Get all work items", notes = "Returns a list of all work items, optionally filterable on classname")
    @RequestMapping(method = RequestMethod.GET)
    public List<WorkItemInfo> getAll(
            @RequestParam(value = "classname", required = false, defaultValue="") String classname,
            @AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);

        List<WorkItemInfo> modelList = new ArrayList<>();
        modelList.addAll(Common.backgroundProcessing.getHighPriorityServiceItems());
        modelList.addAll(Common.backgroundProcessing.getMediumPriorityServiceQueueItems());
        modelList.addAll(Common.backgroundProcessing.getLowPriorityServiceQueueItems());
        if(StringUtils.isNotEmpty(classname)) {
            List<WorkItemInfo> filteredList = new ArrayList<>();
            for(WorkItemInfo model : modelList){
                if(model.getClassname().equalsIgnoreCase(classname)){
                    filteredList.add(model);
                }
            }
            return filteredList;
        }else {
            return modelList;
        }
    }

    @ApiOperation(value = "Get list of work items by classname", notes = "Returns the Work Item specified by the given classname and priority")
    @RequestMapping(method = RequestMethod.GET, value = "/by-priority/{priority}")
    public List<WorkItemInfo> getWorkItemsByPriority(
            @ApiParam(value = "priority", required = true, allowMultiple = false)
            @PathVariable String priority,
            @RequestParam(value = "classname", required = false, defaultValue="") String classname,
            @AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);

        List<WorkItemInfo> list;
        if(priority.equalsIgnoreCase("HIGH")){
            list = Common.backgroundProcessing.getHighPriorityServiceItems();
        }else if(priority.equalsIgnoreCase("MEDIUM")){
            list = Common.backgroundProcessing.getMediumPriorityServiceQueueItems();
        }else if(priority.equalsIgnoreCase("LOW")){
            list = Common.backgroundProcessing.getLowPriorityServiceQueueItems();
        }else{
            ProcessResult result = new ProcessResult();
            result.addContextualMessage("priority", "validate.invalidValue");
            throw new ValidationException(result);
        }

        //Filter if we need to
        if(StringUtils.isNotEmpty(classname)){
            List<WorkItemInfo> modelList = new ArrayList<>();
            for(WorkItemInfo model : list){
                if(model.getClassname().equalsIgnoreCase(classname)){
                    modelList.add(model);
                }
            }
            return modelList;
        }else{
            return list;
        }
    }


    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Queued Work Item Counts", notes = "Returns Work Item names to instance count for High, Medium and Low thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/queue-counts")
    public ResponseEntity<BackgroundProcessingQueueCounts> getQueueCounts(HttpServletRequest request) throws IOException {

        BackgroundProcessingQueueCounts model = new BackgroundProcessingQueueCounts();
        model.setHighPriorityServiceQueueClassCounts(Common.backgroundProcessing.getHighPriorityServiceQueueClassCounts());
        model.setMediumPriorityServiceQueueClassCounts(Common.backgroundProcessing.getMediumPriorityServiceQueueClassCounts());
        model.setLowPriorityServiceQueueClassCounts(Common.backgroundProcessing.getLowPriorityServiceQueueClassCounts());

        return ResponseEntity.ok(model);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Running Work Item Statistics", notes = "Returns information on all tasks running in the High and Medium thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/running-stats")
    public ResponseEntity<BackgroundProcessingRunningStats> getRunningStats(HttpServletRequest request) throws IOException {

        BackgroundProcessingRunningStats model = new BackgroundProcessingRunningStats();
        model.setHighPriorityOrderedQueueStats(Common.backgroundProcessing.getHighPriorityOrderedQueueStats());
        model.setMediumPriorityOrderedQueueStats(Common.backgroundProcessing.getMediumPriorityOrderedQueueStats());

        return ResponseEntity.ok(model);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Rejected Task Statistics", notes = "Returns information on all tasks rejected from the High and Medium thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/rejected-stats")
    public ResponseEntity<BackgroundProcessingRejectedTaskStats> getRejectedStats(HttpServletRequest request) throws IOException {

        BackgroundProcessingRejectedTaskStats model = new BackgroundProcessingRejectedTaskStats();
        model.setHighPriorityRejectedTaskStats(Common.backgroundProcessing.getHighPriorityRejectionHandler().getRejectedTaskStats());
        model.setMediumPriorityRejectedTaskStats(Common.backgroundProcessing.getMediumPriorityRejectionHandler().getRejectedTaskStats());

        return ResponseEntity.ok(model);
    }

}
