/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * TODO Flesh out after we get the events endpoint rest api defined
 * 
 * @author Terry Packer
 *
 */
@Api(value="User Notes", description="Operations on User Notes")
@RestController()
@RequestMapping("/v1/userNotes")
public class UserCommentRestController extends MangoRestController{
	
	private static Log LOG = LogFactory.getLog(UserCommentRestController.class);
	
	public UserCommentRestController(){ }

	
	@ApiOperation(
			value = "Get all user notes",
			notes = ""
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 403, message = "User does not have access")
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<List<UserCommentModel>> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")int limit) {

        
        RestProcessResult<List<UserCommentModel>> result = new RestProcessResult<List<UserCommentModel>>(HttpStatus.OK);
        return result.createResponseEntity();
	}

}
