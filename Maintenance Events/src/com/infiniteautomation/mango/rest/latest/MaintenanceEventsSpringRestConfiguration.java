/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.latest.model.MaintenanceEventTypeModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;
import com.infiniteautomation.mango.spring.annotations.RestMapper;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class MaintenanceEventsSpringRestConfiguration {

    @Autowired
    public MaintenanceEventsSpringRestConfiguration(
            @Autowired
            @RestMapper
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(MaintenanceEventTypeModel.class, MaintenanceEventType.TYPE_NAME));
    }
}
