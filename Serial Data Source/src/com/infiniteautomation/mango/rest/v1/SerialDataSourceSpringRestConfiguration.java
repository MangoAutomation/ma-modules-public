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
import com.infiniteautomation.serial.SerialDataSourceDefinition;
import com.infiniteautomation.serial.vo.SerialDataSourceModel;
import com.infiniteautomation.serial.vo.SerialPointLocatorModel;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class SerialDataSourceSpringRestConfiguration {

    @Autowired
    SerialDataSourceSpringRestConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(SerialDataSourceModel.class, SerialDataSourceDefinition.DATA_SOURCE_TYPE));
        mapper.registerSubtypes(new NamedType(SerialPointLocatorModel.class, SerialPointLocatorModel.TYPE_NAME));
    }
}
