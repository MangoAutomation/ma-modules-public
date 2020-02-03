/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import com.fasterxml.jackson.core.Version;
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
    public SimpleModule getJacksonModule() {
        return new RestApiJacksonModule();
    }

    @Override
    public ObjectMapperSource getSourceMapperType() {
        return ObjectMapperSource.REST;
    }

    public class RestApiJacksonModule extends SimpleModule {
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
