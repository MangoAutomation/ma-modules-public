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
import com.serotonin.m2m2.internal.InternalDataSourceDefinition;
import com.serotonin.m2m2.internal.InternalDataSourceModel;
import com.serotonin.m2m2.internal.InternalPointLocatorModel;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class InternalDataSourceSpringRestConfiguration {

    @Autowired
    InternalDataSourceSpringRestConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(InternalDataSourceModel.class, InternalDataSourceDefinition.DATA_SOURCE_TYPE));
        mapper.registerSubtypes(new NamedType(InternalPointLocatorModel.class, InternalPointLocatorModel.TYPE_NAME));
    }
}
