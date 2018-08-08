/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.spring.maintenanceEvents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.dao.UserDao;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.exception.NotFoundException;

/**
 * TODO Make superclass
 *
 * @author Terry Packer
 */
@Component("maintenanceEventsJavascriptUtility")
public class MaintenanceEventsJavascriptUtility {
    
    @Autowired
    private MaintenanceEventsService service;
    
    
    public boolean toggle(String xid, int userId) {
        User user = UserDao.instance.get(userId);
        if(user == null)
            throw new NotFoundException();
        return service.toggle(xid, user);
    }

    //TODO Generate via reflection
    public String help() {
        return "";
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return help();
    }
}
