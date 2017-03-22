/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.Map;

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
public class SessionExceptionRestV2Controller extends AbstractMangoRestV2Controller{

	//Session Keys for all stored exceptions
	private final String [] exceptionKeys = {Common.SESSION_USER_EXCEPTION,  WebAttributes.AUTHENTICATION_EXCEPTION, WebAttributes.ACCESS_DENIED_403};
	
	@ApiOperation(value = "Get Last Exception for your session", notes = "")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No Exception exists", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.GET}, value = {"/latest"}, produces = {"application/json"} )
	public ResponseEntity<Map<String,Exception>> getLatest(HttpServletRequest request) {
		RestProcessResult<Map<String,Exception>> result = new RestProcessResult<>(HttpStatus.OK);
		
		//Get latest Session Exception
		HttpSession session = request.getSession(false);
		if(session == null)
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, "No Session");
		
		Map<String,Exception> exceptionMap = new HashMap<String, Exception>();
		for(String key : exceptionKeys){
			exceptionMap.put(key, (Exception)session.getAttribute(key));			
		}
		
		return result.createResponseEntity(exceptionMap);
	}
	
	@ApiOperation(value = "Clear Last Exception for your session", notes = "")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No Exception exists", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.PUT}, value = {"/latest"}, produces = {"application/json"} )
	public ResponseEntity<Map<String,Exception>> clearLatest(HttpServletRequest request) {
		RestProcessResult<Map<String,Exception>> result = new RestProcessResult<>(HttpStatus.OK);
		
		//Get latest Session Exception
		HttpSession session = request.getSession(false);
		if(session == null)
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, "No Session");
		
		Map<String,Exception> exceptionMap = new HashMap<String, Exception>();
		for(String key : exceptionKeys){
			exceptionMap.put(key, (Exception)session.getAttribute(key));
			session.removeAttribute(key);
		}
		
		return result.createResponseEntity(exceptionMap);
	}
}
