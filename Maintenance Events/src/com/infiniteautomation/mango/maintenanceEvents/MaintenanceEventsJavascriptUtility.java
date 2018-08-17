/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.maintenanceEvents;

import org.springframework.beans.factory.annotation.Autowired;

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
public class MaintenanceEventsJavascriptUtility extends ScriptUtility{
    
    @Autowired
    private MaintenanceEventsService service;

    public MaintenanceEventVO get(String xid) throws NotFoundException, PermissionException {
        return service.getFullByXid(xid, permissions);
    }
    
    public boolean toggle(String xid) throws NotFoundException, PermissionException, TranslatableIllegalStateException {
        return service.toggle(xid, permissions);
    }
    
    public MaintenanceEventVO insert(MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return this.service.insert(vo, permissions);
    }
    
    public MaintenanceEventVO update(MaintenanceEventVO existing, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return service.update(existing, vo, permissions);
    }
    
    public MaintenanceEventVO update(String existingXid, MaintenanceEventVO vo) throws NotFoundException, PermissionException, ValidationException {
        return service.update(existingXid, vo, permissions);
    }
    
    public MaintenanceEventVO delete(String xid) throws NotFoundException, PermissionException {
        return service.delete(xid, permissions);
    }
    
}
