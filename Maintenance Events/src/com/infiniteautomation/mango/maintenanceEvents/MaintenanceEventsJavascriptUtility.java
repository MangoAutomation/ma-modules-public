/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.maintenanceEvents;

import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.infiniteautomation.mango.util.script.ScriptUtility;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.permission.PermissionException;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventsJavascriptUtility extends ScriptUtility {

    public static final String CONTEXT_KEY = "MaintenanceEventsUtility";

    protected final MaintenanceEventsService meService;

    @Autowired
    public MaintenanceEventsJavascriptUtility(MangoJavaScriptService service, PermissionService permissionService, MaintenanceEventsService meService) {
        super(service, permissionService);
        this.meService = meService;
    }

    @Override
    public String getContextKey() {
        return CONTEXT_KEY;
    }

    public MaintenanceEventVO get(String xid) throws NotFoundException, PermissionException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.get(xid);
        });

    }

    public boolean toggle(String xid) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.toggle(xid);
        });
    }

    public boolean isEventActive(String xid) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.isEventActive(xid);
        });
    }

    public boolean setState(String xid, boolean active) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.setState(xid, active);
        });
    }

    public MaintenanceEventVO insert(MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return this.permissionService.runAs(permissions, () -> {
            return this.meService.insert(vo);
        });
    }

    public MaintenanceEventVO update(MaintenanceEventVO existing, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.update(existing, vo);
        });
    }

    public MaintenanceEventVO update(String existingXid, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.update(existingXid, vo);
        });
    }

    public MaintenanceEventVO delete(String xid) throws NotFoundException, PermissionException {
        return this.permissionService.runAs(permissions, () -> {
            return meService.delete(xid);
        });
    }

}
