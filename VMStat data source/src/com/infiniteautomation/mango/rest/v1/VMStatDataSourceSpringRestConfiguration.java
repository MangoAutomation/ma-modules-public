/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.serotonin.m2m2.vmstat.VMStatDataSourceDefinition;
import com.serotonin.m2m2.vmstat.VMStatDataSourceModel;
import com.serotonin.m2m2.vmstat.VMStatPointLocatorModel;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class VMStatDataSourceSpringRestConfiguration {

    @Autowired
    VMStatDataSourceSpringRestConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(VMStatDataSourceModel.class, VMStatDataSourceDefinition.DATA_SOURCE_TYPE));
        mapper.registerSubtypes(new NamedType(VMStatPointLocatorModel.class, VMStatPointLocatorModel.TYPE_NAME));
    }
}
