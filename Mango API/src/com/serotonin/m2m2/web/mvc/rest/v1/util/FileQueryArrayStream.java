/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.RQLToLimitVisitor;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

import net.jazdw.rql.parser.ASTNode;

/**
 * 
 * @author Terry Packer
 */
public class FileQueryArrayStream implements QueryArrayStream<String>{

	private File file;
	private ASTNode query;

	public FileQueryArrayStream(File file, ASTNode query){
		this.file = file;
		this.query = query;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
	 */
	@Override
	public void streamData(JsonGenerator jgen) throws IOException {
		if(file.exists()){
			int count = 0;
			Integer limit = null;
			if(this.query != null)
				limit = query.accept(new RQLToLimitVisitor());
			try(BufferedReader br = new BufferedReader(new FileReader(file))){
				
				for(String line; (line = br.readLine()) != null; ) {
					jgen.writeObject(line);
					//TODO Match line here
					//Matcher m = pattern.matcher(line);
					//if(m.find()){
						//TODO Write out item in Array? if so there will be no way to sort/order
						//TODO What about groups?
						count++;
					//}
					
					//Check Limit
					if(limit != null && count >= limit)
						return;
			    }
			}catch(Exception e){
				throw new IOException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<String> writer) throws IOException {
		//Not supported yet
	}

}
