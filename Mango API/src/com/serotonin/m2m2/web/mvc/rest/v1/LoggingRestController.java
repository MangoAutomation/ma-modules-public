/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.QueryAttribute;
import com.infiniteautomation.mango.db.query.TableModel;
import com.infiniteautomation.mango.rest.v2.FileStoreRestV2Controller;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.filestore.FileModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.logging.LogQueryArrayStream;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logging")
@RestController
@RequestMapping("/logging")
public class LoggingRestController extends MangoRestController{

	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "List Log Files", notes = "Returns a list of logfile metadata")
	@RequestMapping(method = RequestMethod.GET, value = "/files")
    public ResponseEntity<List<FileModel>> list (
    		@RequestParam(value = "limit", required = false) Integer limit,
    		HttpServletRequest request) throws IOException{

		List<FileModel> modelList = new ArrayList<FileModel>();
		File logsDir = Common.getLogsDir();
		int count = 0;
		for(File file : logsDir.listFiles()){
	        if((limit != null)&&(count >= limit.intValue()))
	            break;
			if(!file.getName().startsWith(".")){
			    FileModel model = FileStoreRestV2Controller.fileToModel(file, logsDir, request.getServletContext());
			    model.setMimeType("text/plain");
			    modelList.add(model);
			    count++;
			}
		}
		return ResponseEntity.ok(modelList);
    }
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Query ma.log logs", 
			notes = "Returns a list of recent logs, ie. /by-filename/ma.log?limit(10)\n" + 
					"<br>Query Examples: \n" + 
					"by-filename/ma.log/?level=gt=DEBUG\n" + 
					"by-filename/ma.log/?classname=com.serotonin.m2m2m.Common\n" + 
					"by-filename/ma.log/?methodName=setPointValue\n" + 
					"NOTE: Querying non ma.log files is not supported, nor is ordering")
	@RequestMapping(method = RequestMethod.GET, value="/by-filename/{filename}")
    public ResponseEntity<QueryArrayStream<?>> query(
    		@PathVariable String filename, 
    		HttpServletRequest request) {
		RestProcessResult<QueryArrayStream<?>> result = new RestProcessResult<QueryArrayStream<?>>(HttpStatus.OK);
    		ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
    		File file = new File(Common.getLogsDir(), filename);
    		if(file.exists()){
    		    //Pattern pattern = new 
    			if(filename.matches(LogQueryArrayStream.LOGFILE_REGEX)){
    				LogQueryArrayStream stream = new LogQueryArrayStream(filename, query);
    				return result.createResponseEntity(stream);
    			}else{
    			    throw new AccessDeniedException("Non ma.log files are not accessible on this endpoint.");
    			}
    		}else{
    			throw new NotFoundRestException();
    		}
    }
	
	@PreAuthorize("isAdmin()")
    @ApiOperation(value = "View log", notes = "Optionally download file as attachment")
    @RequestMapping(method = RequestMethod.GET, produces={"text/plain"}, value = "/view/{filename}")
    public ResponseEntity<FileSystemResource> download(
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean download,
            @AuthenticationPrincipal User user,
            @PathVariable String filename, HttpServletRequest request) {
        File file = new File(Common.getLogsDir(), filename);
        if (file.exists()) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");
            return new ResponseEntity<>(new FileSystemResource(file), responseHeaders, HttpStatus.OK);
        }else {
            throw new NotFoundRestException();
        }
    }
	
	
	@ApiOperation(
			value = "Get Explaination For Query",
			notes = "What is Query-able on this model"
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.GET, value = "/explain-query")
    public ResponseEntity<TableModel> getTableModel(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
    	TableModel model = new TableModel();
    	List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
    	attributes.add(new QueryAttribute("level", null, Types.VARCHAR));
    	attributes.add(new QueryAttribute("classname", null, Types.VARCHAR));
    	attributes.add(new QueryAttribute("method", null, Types.VARCHAR));
    	attributes.add(new QueryAttribute("time", null, Types.INTEGER));
    	attributes.add(new QueryAttribute("message", null, Types.VARCHAR));
	    model.setAttributes(attributes);
	    return ResponseEntity.ok(model);
    }
}
