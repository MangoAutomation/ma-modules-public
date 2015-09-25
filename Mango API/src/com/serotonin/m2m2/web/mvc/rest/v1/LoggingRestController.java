/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.QueryModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.logging.LogQueryArrayStream;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logging", description="Logging")
@RestController
@RequestMapping("/v1/logging")
public class LoggingRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(LoggingRestController.class);
	
	@ApiOperation(value = "List Log Files", notes = "Returns a list of logfile names")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/files")
    public ResponseEntity<List<String>> list(
    		@RequestParam(value = "limit", required = false, defaultValue="100") int limit,
    		HttpServletRequest request) {
		RestProcessResult<List<String>> result = new RestProcessResult<List<String>>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){
    		QueryModel query = new QueryModel();
    		query.setLimit(limit);
			List<String> modelList = new ArrayList<String>();
			File logsDir = Common.getLogsDir();
			for(String filename : logsDir.list()){
				if(!filename.startsWith("."))
				modelList.add(filename);
			}
			return result.createResponseEntity(modelList);
    	}
    	
    	return result.createResponseEntity();
    }
	
	@ApiOperation(value = "Query logs", 
			notes = "Returns a list of recent logs, when adding a file extension end the URL with a slash. i.e. /by-filename/ma.log/?limit(10)\n" + 
					"<br>Query Examples: \n" + 
					"by-filename/ma.log/?level=gt=DEBUG\n" + 
					"by-filename/ma.log/?classname=com.serotonin.m2m2m.Common\n" + 
					"by-filename/ma.log/?methodName=setPointValue\n")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="/by-filename/{filename}")
    public ResponseEntity<LogQueryArrayStream> getAll(
    		@PathVariable String filename, 
    		HttpServletRequest request) {
		RestProcessResult<LogQueryArrayStream> result = new RestProcessResult<LogQueryArrayStream>(HttpStatus.OK);

		
		
		this.checkUser(request, result);
    	if(result.isOk()){
    		try{
	    		ASTNode query = this.parseRQLtoAST(request);
				LogQueryArrayStream stream = new LogQueryArrayStream(filename, query);
				return result.createResponseEntity(stream);
    		}catch(UnsupportedEncodingException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
    }
	
	
}
