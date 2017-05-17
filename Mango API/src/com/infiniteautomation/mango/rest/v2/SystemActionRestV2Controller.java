/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.rest.v2.util.SystemActionTemporaryResource;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemActionDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Class to provide server information
 * 
 * @author Terry Packer
 */
@Api(value="System Actions", description="Ask Mango to perform a pre-defined action.  Admin Only.")
@RestController
@RequestMapping("/v2/actions")
public class SystemActionRestV2Controller extends AbstractMangoRestV2Controller{

	private MangoRestTemporaryResourceContainer<SystemActionTemporaryResource> resources;
	
	public SystemActionRestV2Controller(){
		this.resources = new MangoRestTemporaryResourceContainer<>("SYSACTION_");
	}
	
	@ApiOperation(
			value = "List Available Actions",
			notes = "",
			response=List.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<List<String>> list() {
		RestProcessResult<List<String>> result = new RestProcessResult<>(HttpStatus.OK);
		Map<String, SystemActionDefinition> defs = ModuleRegistry.getSystemActionDefinitions();
		List<String> model = new ArrayList<String>();
		for(SystemActionDefinition def : defs.values())
			model.add(def.getKey());
		return result.createResponseEntity(model);
	}

	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Perform an Action", notes="Kicks off action and returns temporary URL for status")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "Not Found", response=ResponseEntity.class),
	})
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json"}, produces={"application/json"}, value = "/{action}")
    public ResponseEntity<Void> sendTestEmail(
    		@ApiParam(value = "Valid System Action", required = true, allowMultiple = false)
			@PathVariable String action,
			@ApiParam(value = "Input for task", required = false, allowMultiple = false)
    		JsonNode input,
    		UriComponentsBuilder builder) {
		try{
			RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
			
			//Kick off action
			SystemActionDefinition def = ModuleRegistry.getSystemActionDefinition(action);
			if(def == null)
				throw new NotFoundRestException();
			
			String resourceId = resources.generateResourceId();
			SystemActionTemporaryResource resource = new SystemActionTemporaryResource(resourceId, def.getTask(input));
			
			//Resource can live for up to 10 minutes (TODO Configurable?)
			resources.put(resourceId, resource, System.currentTimeMillis() + 600000);
	    	URI location = builder.path("/v2/data-file/import/{resourceId}").buildAndExpand(resourceId).toUri();
	    	result.addHeader("Location", location.toString());
	    	
			return result.createResponseEntity();
		}catch(Exception e){
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Get Action Progress", notes = "Polls temporary resource for results.")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No resource exists with given id", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.GET}, value = {"/import/{resourceId}"}, produces = {"application/json"} )
	public ResponseEntity<SystemActionTemporaryResource> getProgress(HttpServletRequest request, 
			@ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId,
			@AuthenticationPrincipal User user) {
		RestProcessResult<SystemActionTemporaryResource> result = new RestProcessResult<>(HttpStatus.OK);
		
		SystemActionTemporaryResource resource = resources.get(resourceId);
		
		return result.createResponseEntity(resource);
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Cancel Action", notes = "No Guarantees that the cancel will work, this is task dependent.")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No resource exists with given id", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.PUT}, value = {"/import/{resourceId}"}, produces = {"application/json"} )
	public ResponseEntity<SystemActionTemporaryResource> cancel(HttpServletRequest request, 
			@ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId,
			@AuthenticationPrincipal User user) {
		RestProcessResult<SystemActionTemporaryResource> result = new RestProcessResult<>(HttpStatus.OK);
		
		SystemActionTemporaryResource resource = resources.get(resourceId);
		
		resource.cancel();
		
		return result.createResponseEntity(resource);
	}
	
}
