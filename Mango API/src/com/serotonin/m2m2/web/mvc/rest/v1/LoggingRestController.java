/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.QueryAttribute;
import com.infiniteautomation.mango.db.query.QueryModel;
import com.infiniteautomation.mango.db.query.TableModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.logging.LogQueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.util.FileQueryArrayStream;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

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
			notes = "Returns a list of recent logs, ie. /by-filename/ma.log?limit(10)\n" + 
					"<br>Query Examples: \n" + 
					"by-filename/ma.log/?level=gt=DEBUG\n" + 
					"by-filename/ma.log/?classname=com.serotonin.m2m2m.Common\n" + 
					"by-filename/ma.log/?methodName=setPointValue\n" + 
					"NOTE: non ma.log files only support limit restrictions in the query")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="/by-filename/{filename}")
    public ResponseEntity<QueryArrayStream<?>> query(
    		@PathVariable String filename, 
    		HttpServletRequest request) {
		RestProcessResult<QueryArrayStream<?>> result = new RestProcessResult<QueryArrayStream<?>>(HttpStatus.OK);
		
		this.checkUser(request, result);
    	if(result.isOk()){
    		try{
	    		ASTNode query = this.parseRQLtoAST(request);
	    		File file = new File(Common.getLogsDir(), filename);
	    		if(file.exists()){
	    			if(filename.startsWith("ma.")){
	    				LogQueryArrayStream stream = new LogQueryArrayStream(filename, query);
	    				return result.createResponseEntity(stream);
	    			}else{
	    				//Simply return the lines from the file without RQL
	    				FileQueryArrayStream stream = new FileQueryArrayStream(file, query);
	    				return result.createResponseEntity(stream);
	    			}
	    		}else{
	    			result.addRestMessage(getDoesNotExistMessage());
	    		}
    		}catch(UnsupportedEncodingException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Get Explaination For Query",
			notes = "What is Query-able on this model"
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/explain-query")
    public ResponseEntity<TableModel> getTableModel(HttpServletRequest request) {
        
        RestProcessResult<TableModel> result = new RestProcessResult<TableModel>(HttpStatus.OK);
        
        this.checkUser(request, result);
        if(result.isOk()){
        	TableModel model = new TableModel();
        	List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
        	attributes.add(new QueryAttribute("level", null, Types.VARCHAR));
        	attributes.add(new QueryAttribute("classname", null, Types.VARCHAR));
        	attributes.add(new QueryAttribute("method", null, Types.VARCHAR));
        	attributes.add(new QueryAttribute("time", null, Types.INTEGER));
        	attributes.add(new QueryAttribute("message", null, Types.VARCHAR));
        	
        	model.setAttributes(attributes);
 	        result.addRestMessage(getSuccessMessage());
	        return result.createResponseEntity();
        }
        
        return result.createResponseEntity();
    }
}
