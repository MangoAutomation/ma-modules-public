/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.mapping;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.model.permissions.MangoPermissionModel;

/**
 * Class used to convert @RequestParam Strings into MangoPermissionModels
 * @author Terry Packer
 */
public class MangoPermissionModelConverter implements Converter<String, MangoPermissionModel> {

    private final ObjectMapper mapper;
    private final MangoPermissionModelDeserializer deserializer;

    public MangoPermissionModelConverter(ObjectMapper mapper, MangoPermissionModelDeserializer deserializer) {
        this.mapper = mapper;
        this.deserializer = deserializer;
    }

    @Override
    public MangoPermissionModel convert(String value) {
        if(value == null) {
            return new MangoPermissionModel();
        }else {
            //Convert using our deserializer which supports all formats
            try {
                JsonNode tree = this.mapper.valueToTree(value);
                return this.deserializer.nodeToModel(tree, mapper);
            } catch (Exception e) {
                throw new BadRequestException(e);
            }
        }
    }
}
