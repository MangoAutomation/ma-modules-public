/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.rest.v2.model.MaintenanceEventTypeModel;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class MaintenanceEventsSpringRestConfiguration {

    @Autowired
    public MaintenanceEventsSpringRestConfiguration(
            @Autowired
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(MaintenanceEventTypeModel.class, MaintenanceEventType.TYPE_NAME));
    }
}
