/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class MaintenanceEventModelMapping implements RestModelMapping<MaintenanceEventVO, MaintenanceEventModel>{

    @Override
    public MaintenanceEventModel map(Object o, User user, RestModelMapper mapper) {
        return new MaintenanceEventModel((MaintenanceEventVO)o);
    }

    @Override
    public Class<MaintenanceEventModel> toClass() {
        return MaintenanceEventModel.class;
    }

    @Override
    public Class<MaintenanceEventVO> fromClass() {
        return MaintenanceEventVO.class;
    }
}
