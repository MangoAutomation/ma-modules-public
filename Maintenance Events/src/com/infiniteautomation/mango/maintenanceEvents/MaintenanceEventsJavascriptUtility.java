/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.maintenanceEvents;

import org.springframework.beans.factory.annotation.Autowired;

import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
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
    public MaintenanceEventsJavascriptUtility(MangoJavaScriptService service, MaintenanceEventsService meService) {
        super(service);
        this.meService = meService;
    }

    @Override
    public String getContextKey() {
        return CONTEXT_KEY;
    }

    public MaintenanceEventVO get(String xid) throws NotFoundException, PermissionException {
        return meService.getFull(xid, permissions);
    }
    
    public boolean toggle(String xid) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return meService.toggle(xid, permissions);
    }
    
    public boolean isEventActive(String xid) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return meService.isEventActive(xid, permissions);
    }
    
    public boolean setState(String xid, boolean active) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return meService.setState(xid, permissions, active);
    }
    
    public MaintenanceEventVO insert(MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return this.meService.insert(vo, permissions);
    }
    
    public MaintenanceEventVO update(MaintenanceEventVO existing, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return meService.update(existing, vo, permissions);
    }
    
    public MaintenanceEventVO update(String existingXid, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return meService.update(existingXid, vo, permissions);
    }
    
    public MaintenanceEventVO delete(String xid) throws NotFoundException, PermissionException {
        return meService.delete(xid, permissions);
    }
    
}
