/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
public class ASTNodeSerializer extends StdSerializer<ASTNode> {
    private static final long serialVersionUID = 1L;

    protected ASTNodeSerializer() {
        super(ASTNode.class);
    }

    @Override
    public void serialize(ASTNode value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        provider.defaultSerializeField("args", value.getArguments(), gen);
        gen.writeEndObject();
    }

}
