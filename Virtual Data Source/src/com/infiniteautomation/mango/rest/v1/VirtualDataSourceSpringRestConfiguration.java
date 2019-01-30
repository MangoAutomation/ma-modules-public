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
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceModel;
import com.serotonin.m2m2.virtual.vo.model.VirtualPointLocatorModel;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class VirtualDataSourceSpringRestConfiguration {

    @Autowired
    VirtualDataSourceSpringRestConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(VirtualDataSourceModel.class, VirtualDataSourceDefinition.TYPE_NAME));
        mapper.registerSubtypes(new NamedType(VirtualPointLocatorModel.class, VirtualPointLocatorModel.TYPE_NAME));
    }
}