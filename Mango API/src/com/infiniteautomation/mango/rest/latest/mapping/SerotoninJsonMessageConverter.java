/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.type.JsonTypeReader;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.MediaTypes;

/**
 * Convert Outgoing Objects to Serotonin JSON
 *
 * Convert Incoming Serotonin JSON to Mango VOs (experimental)
 *
 * TODO Mango 4.0 the read internal method will not work properly
 * @author Terry Packer
 */
public class SerotoninJsonMessageConverter extends AbstractHttpMessageConverter<Object>{

    public SerotoninJsonMessageConverter(){
        super(MediaTypes.SEROTONIN_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        InputStreamReader isReader = new InputStreamReader(inputMessage.getBody());
        JsonTypeReader typeReader = new JsonTypeReader(isReader);
        try {
            JsonValue value = typeReader.read();
            if(clazz.equals(JsonValue.class))
                return value;
            else
                return value.toNative();
        }catch(JsonException e){
            throw new IOException(e);
        }
    }

    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        OutputStreamWriter osWriter = new OutputStreamWriter(outputMessage.getBody());
        JsonWriter writer = new JsonWriter(Common.JSON_CONTEXT, osWriter);
        writer.setPrettyOutput(true);
        writer.setPrettyIndent(2);
        try {
            writer.writeObject(t);
            writer.flush();
        }
        catch (JsonException e) {
            throw new IOException(e);
        }
    }

}
