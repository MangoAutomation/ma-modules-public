/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfig;
import com.infiniteautomation.mango.rest.latest.exception.ExceptionMixin;
import com.infiniteautomation.mango.rest.latest.mapping.JSONStreamedArraySerializer;
import com.infiniteautomation.mango.rest.latest.mapping.JScienceModule;
import com.infiniteautomation.mango.rest.latest.mapping.MangoPermissionModelDeserializer;
import com.infiniteautomation.mango.rest.latest.mapping.MangoPermissionModelSerializer;
import com.infiniteautomation.mango.rest.latest.mapping.TranslatableMessageSerializer;
import com.infiniteautomation.mango.rest.latest.mapping.VirtualSerialPortConfigDeserializer;
import com.infiniteautomation.mango.rest.latest.model.JSONStreamedArray;
import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.JacksonModuleDefinition;
import com.infiniteautomation.mango.spring.annotations.RestMapper;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

import net.jazdw.rql.parser.ASTNode;

/**
 * Common api jackson module
 * @author Terry Packer
 */
@RestMapper
public class RestApiJacksonModuleDefinition extends JacksonModuleDefinition {

    @Autowired
    PermissionService permissionService;

    @Override
    public Iterable<? extends Module> getJacksonModules() {
        return List.of(
                new RestApiJacksonModule(getModule().getName(), createJacksonVersion(), permissionService),
                new JScienceModule(),
                new JavaTimeModule(),
                new Jdk8Module()
        );
    }

    public static class RestApiJacksonModule extends SimpleModule {
        private static final long serialVersionUID = 1L;

        private final PermissionService permissionService;

        public RestApiJacksonModule(String name, Version version, PermissionService permissionService) {
            super(name, version);
            this.permissionService = permissionService;
            setMixInAnnotation(Exception.class, ExceptionMixin.class);
        }

        @Override
        public void setupModule(SetupContext context) {
            addSerializer(ASTNode.class, new ASTNodeSerializer());
            addDeserializer(ASTNode.class, new ASTNodeDeserializer());
            addSerializer(JsonValue.class, new SerotoninJsonValueSerializer());
            addDeserializer(JsonValue.class, new SerotoninJsonValueDeserializer());
            addSerializer(DataValue.class, new DataValueSerializer());
            addSerializer(JSONStreamedArray.class, new JSONStreamedArraySerializer());
            addSerializer(TranslatableMessage.class, new TranslatableMessageSerializer());
            addDeserializer(VirtualSerialPortConfig.class, new VirtualSerialPortConfigDeserializer());
            addSerializer(MangoPermissionModel.class, new MangoPermissionModelSerializer());
            addDeserializer(MangoPermissionModel.class, new MangoPermissionModelDeserializer(permissionService));
            super.setupModule(context);
        }
    }

}
