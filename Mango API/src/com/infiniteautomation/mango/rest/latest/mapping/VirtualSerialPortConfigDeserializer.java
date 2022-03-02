/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.infiniteautomation.mango.io.serial.virtual.SerialServerSocketBridgeConfig;
import com.infiniteautomation.mango.io.serial.virtual.SerialSocketBridgeConfig;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfig;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * 
 * @author Terry Packer
 */
public class VirtualSerialPortConfigDeserializer extends StdDeserializer<VirtualSerialPortConfig>{

    private static final long serialVersionUID = 1L;
    
    public VirtualSerialPortConfigDeserializer() {
        super(VirtualSerialPortConfig.class);
    }

    @Override
    public VirtualSerialPortConfig deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();  
        JsonNode tree = jp.readValueAsTree();
        String typeName = tree.get("portType").asText();
        Class<?> clazz;
        switch(typeName){
        case "SERIAL_SOCKET_BRIDGE":
            clazz = SerialSocketBridgeConfig.class;
            break;
        case "SERIAL_SERVER_SOCKET_BRIDGE":
            clazz = SerialServerSocketBridgeConfig.class;
            break;
        default:
            throw new ServerErrorException(new TranslatableMessage("rest.missingModelMapping", VirtualSerialPortConfig.class.getSimpleName(), typeName));
        }
        
        VirtualSerialPortConfig model = (VirtualSerialPortConfig)mapper.treeToValue(tree, clazz);
        return model;
    }

}
