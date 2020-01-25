/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Simple class to output Serotonin JSON through Jackson
 *
 * @author Terry Packer
 */
public class SerotoninToJacksonJsonWriter extends Writer{

    private JsonGenerator jgen;

    public SerotoninToJacksonJsonWriter(JsonGenerator jgen){
        this.jgen = jgen;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        jgen.writeRaw(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        jgen.flush();
    }

    @Override
    public void close() throws IOException {
        jgen.close();
    }
}
