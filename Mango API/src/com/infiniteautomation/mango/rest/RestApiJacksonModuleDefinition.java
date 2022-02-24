/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import java.util.Collections;
import java.util.EnumSet;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.module.JacksonModuleDefinition;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

import net.jazdw.rql.parser.ASTNode;

/**
 * Common api jackson module
 * @author Terry Packer
 */
public class RestApiJacksonModuleDefinition extends JacksonModuleDefinition {

    @Override
    public Iterable<? extends Module> getJacksonModules() {
        return Collections.singleton(new RestApiJacksonModule(getModule().getName(), createJacksonVersion()));
    }

    @Override
    public EnumSet<ObjectMapperSource> getSourceMapperTypes() {
        return EnumSet.of(ObjectMapperSource.REST);
    }

    public static class RestApiJacksonModule extends SimpleModule {
        private static final long serialVersionUID = 1L;

        public RestApiJacksonModule(String name, Version version) {
            super(name, version);

            this.addSerializer(ASTNode.class, new ASTNodeSerializer());
            this.addSerializer(JsonValue.class, new SerotoninJsonValueSerializer());

            this.addDeserializer(JsonValue.class, new SerotoninJsonValueDeserializer());
            this.addDeserializer(ASTNode.class, new ASTNodeDeserializer());

            this.addSerializer(DataValue.class, new DataValueSerializer());
        }
    }

}
