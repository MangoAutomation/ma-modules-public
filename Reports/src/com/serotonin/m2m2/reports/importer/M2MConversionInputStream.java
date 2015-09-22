/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Terry Packer
 *
 */
/**
 * Class to change the SerialUID from some value other than -1L to -1L
 * @author Terry Packer
 *
 */
class M2MConversionInputStream extends ObjectInputStream{
	private static final Log LOG = LogFactory.getLog(M2MConversionInputStream.class);
			
	//Map of legacy class names to new class names
	private final Map<String, String> classMappings;
	
	public M2MConversionInputStream(InputStream is, Map<String, String> classMappings) throws IOException{
		super(is);
		this.classMappings = classMappings;
	}
	
	@Override 
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException{
		ObjectStreamClass descriptor = super.readClassDescriptor();
		
		//Find our mapped classes and override the class name so we can import it
		String mappedClass = this.classMappings.get(descriptor.getName());
		
	    if(mappedClass != null){
	    	try {
				Field name = descriptor.getClass().getDeclaredField("name");
				name.setAccessible(true);
				name.set(descriptor, mappedClass);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
				throw new IOException(e);
			}
	    }
	    
	    
	    return descriptor;
	}
}
