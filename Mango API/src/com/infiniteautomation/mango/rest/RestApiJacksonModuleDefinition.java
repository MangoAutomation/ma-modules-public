/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import java.util.Collections;
import java.util.EnumSet;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.module.JacksonModuleDefinition;

import net.jazdw.rql.parser.ASTNode;

/**
 * Common api jackson module
 * @author Terry Packer
 */
public class RestApiJacksonModuleDefinition extends JacksonModuleDefinition {

    @Override
    public Iterable<? extends Module> getJacksonModules() {
        return Collections.singleton(new RestApiJacksonModule());
    }

    @Override
    public EnumSet<ObjectMapperSource> getSourceMapperTypes() {
        return EnumSet.of(ObjectMapperSource.REST);
    }

    public static class RestApiJacksonModule extends SimpleModule {
        private static final long serialVersionUID = 1L;

        public RestApiJacksonModule() {
            super("MangoApi", new Version(4, 0, 0, "", "com.infiniteautomation", "mango"));

            this.addSerializer(ASTNode.class, new ASTNodeSerializer());
            this.addSerializer(JsonValue.class, new SerotoninJsonValueSerializer());

            this.addDeserializer(JsonValue.class, new SerotoninJsonValueDeserializer());
            this.addDeserializer(ASTNode.class, new ASTNodeDeserializer());
        }
    }

}
