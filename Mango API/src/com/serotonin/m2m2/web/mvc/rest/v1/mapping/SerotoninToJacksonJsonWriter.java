/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

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
		
		/* (non-Javadoc)
		 * @see java.io.Writer#write(char[], int, int)
		 */
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			jgen.writeRaw(cbuf, off, len);
		}

		/* (non-Javadoc)
		 * @see java.io.Writer#flush()
		 */
		@Override
		public void flush() throws IOException {
			jgen.flush();
		}

		/* (non-Javadoc)
		 * @see java.io.Writer#close()
		 */
		@Override
		public void close() throws IOException {
			jgen.close();
		}
}
