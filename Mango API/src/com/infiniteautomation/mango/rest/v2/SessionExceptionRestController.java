/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Endpoints for Collecting Recent Exception Information
 * 
 * @author Terry Packer
 */
@Api(value = "Session Exception Information", description = "Endpoints to help with collection of server side errors")
@RestController
@RequestMapping("/v2/exception")
public class SessionExceptionRestController extends AbstractMangoRestV2Controller{

	
	@ApiOperation(value = "Get Last Exception for your session", notes = "")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No Exception exists", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.GET}, value = {"/latest"}, produces = {"application/json"} )
	public ResponseEntity<Exception> getLatest(HttpServletRequest request) {
		RestProcessResult<Exception> result = new RestProcessResult<>(HttpStatus.OK);
		
		//Get latest Session Exception
		HttpSession session = request.getSession(false);
		if(session == null)
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, "No Session");
		
		Exception e = (Exception)session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		if(e == null){
			//Check for other general mango exceptions
			e = (Exception)session.getAttribute(Common.SESSION_USER_EXCEPTION);
		}
		
		return result.createResponseEntity(e);
	}
	
	@ApiOperation(value = "Clear Last Exception for your session", notes = "")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No Exception exists", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.PUT}, value = {"/latest"}, produces = {"application/json"} )
	public ResponseEntity<Exception> clearLatest(HttpServletRequest request) {
		RestProcessResult<Exception> result = new RestProcessResult<>(HttpStatus.OK);
		
		//Get latest Session Exception
		HttpSession session = request.getSession(false);
		if(session == null)
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, "No Session");
		
		Exception e = (Exception)session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		if(e != null)
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		else
			session.removeAttribute(Common.SESSION_USER_EXCEPTION);	
		
		
		return result.createResponseEntity(e);
	}
}
