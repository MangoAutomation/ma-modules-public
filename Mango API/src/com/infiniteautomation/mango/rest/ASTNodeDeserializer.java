/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
public class ASTNodeDeserializer extends StdDeserializer<ASTNode> {
    private static final long serialVersionUID = 1L;

    protected ASTNodeDeserializer() {
        super(ASTNode.class);
    }

    @Override
    public ASTNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            // consume start token
            p.nextToken();
        }

        // empty object
        if (p.currentToken() == JsonToken.END_OBJECT) {
            return new ASTNode(null);
        }

        if (p.currentToken() != JsonToken.FIELD_NAME) {
            return (ASTNode) ctxt.handleUnexpectedToken(ASTNode.class, p);
        }

        String name = null;
        List<Object> args = null;

        while (p.currentToken() == JsonToken.FIELD_NAME) {
            JsonToken currentToken = p.nextToken(); // consume field name
            String fieldName = p.currentName();

            if ("name".equals(fieldName) && currentToken == JsonToken.VALUE_STRING) {
                name = p.getText();
                p.nextToken();
            } else if (("arguments".equals(fieldName) || "args".equals(fieldName)) && currentToken == JsonToken.START_ARRAY) {
                args = deserializeArray(p, ctxt);
            } else if (currentToken.isStructStart()) {
                // skip over children of arrays or objects
                p.skipChildren();
                // consume end token
                p.nextToken();
            } else {
                // skip over unknown value fields
                p.nextToken();
            }
        }

        // consume end token
        if (p.currentToken() == JsonToken.END_OBJECT) {
            p.nextToken();
        }

        return new ASTNode(name, args == null ? Collections.emptyList() : args);
    }

    public List<Object> deserializeArray(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken t = p.nextToken(); // consume start array token
        List<Object> result = new ArrayList<>();

        while (p.currentToken() != JsonToken.END_ARRAY) {
            if (t == JsonToken.START_OBJECT) {
                // treat all objects inside the arguments as ASTNodes
                result.add(this.deserialize(p, ctxt));
            } else if (t == JsonToken.START_ARRAY) {
                result.add(this.deserializeArray(p, ctxt));
            } else {
                result.add(ctxt.readValue(p, Object.class));
                p.nextToken();
            }
        }

        p.nextToken(); // consume end array token

        return result;
    }

}
