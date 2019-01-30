/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceModel;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorModel;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;

/**
 * @author Terry Packer
 *
 */
@Configuration
public class AsciiFileDataSourceSpringRestConfiguration {

    @Autowired
    AsciiFileDataSourceSpringRestConfiguration(
            @Qualifier(MangoRuntimeContextConfiguration.REST_OBJECT_MAPPER_NAME)
            ObjectMapper mapper) {
        mapper.registerSubtypes(new NamedType(AsciiFileDataSourceModel.class, AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE));
        mapper.registerSubtypes(new NamedType(AsciiFilePointLocatorModel.class, AsciiFilePointLocatorModel.TYPE_NAME));
    }
}
