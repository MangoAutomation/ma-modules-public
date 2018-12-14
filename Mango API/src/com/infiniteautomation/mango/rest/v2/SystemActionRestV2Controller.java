/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mango.rest.v2.util.SystemActionTemporaryResource;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemActionDefinition;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller for triggering system actions and retrieving their status
 * 
 * @author Terry Packer
 */
@Api(value="System Actions", description="Ask Mango to perform a pre-defined action.  Admin Only.")
@PreAuthorize("isAdmin()")
@RestController
@RequestMapping("/actions")
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
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<String>> list() {

		Map<String, SystemActionDefinition> defs = ModuleRegistry.getSystemActionDefinitions();
		List<String> model = new ArrayList<String>();
		for(SystemActionDefinition def : defs.values())
			model.add(def.getKey());

        return new ResponseEntity<>(model, HttpStatus.OK);
	}

	@ApiOperation(value = "Perform an Action", notes="Kicks off action and returns temporary URL for status")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "Not Found", response=ResponseEntity.class),
	})
	@RequestMapping(method = RequestMethod.PUT, value = "/trigger/{action}")
    public ResponseEntity<SystemActionTemporaryResource> performAction(
    		@ApiParam(value = "Valid System Action", required = true, allowMultiple = false)
			@PathVariable String action,
			@ApiParam(value = "Input for task", required = false, allowMultiple = false)
    		@RequestBody(required=false)
    		JsonNode input,
    		@AuthenticationPrincipal User user,
    		UriComponentsBuilder builder) {
	    //Kick off action
        SystemActionDefinition def = ModuleRegistry.getSystemActionDefinition(action);
        if(def == null)
            throw new NotFoundRestException();
        
        String resourceId = resources.generateResourceId();
        SystemActionTemporaryResource resource = new SystemActionTemporaryResource(resourceId, def.getTask(user, input), resources, new Date(System.currentTimeMillis() + 600000));
        
        //Resource can live for up to 10 minutes (TODO Configurable?)
        resources.put(resourceId, resource);
        URI location = builder.path("/actions/status/{resourceId}").buildAndExpand(resourceId).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Get Action Progress", notes = "Polls temporary resource for results.")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No resource exists with given id", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.GET}, value = {"/status/{resourceId}"})
	public ResponseEntity<SystemActionTemporaryResource> getStatus(HttpServletRequest request, 
			@ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId,
			@AuthenticationPrincipal User user) {
	    
		SystemActionTemporaryResource resource = resources.get(resourceId);
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@ApiOperation(value = "Cancel Action", notes = "No Guarantees that the cancel will work, this is task dependent.")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "No resource exists with given id", response=ResponseEntity.class),
		@ApiResponse(code = 500, message = "Error processing request", response=ResponseEntity.class)
	})
	@RequestMapping( method = {RequestMethod.PUT}, value = {"/cancel/{resourceId}"})
	public ResponseEntity<SystemActionTemporaryResource> cancel(HttpServletRequest request, 
			@ApiParam(value="Resource id", required=true, allowMultiple=false) @PathVariable String resourceId,
			@AuthenticationPrincipal User user) {

		SystemActionTemporaryResource resource = resources.get(resourceId);
		resource.cancel();
        return new ResponseEntity<>(resource, HttpStatus.OK);
	}

}
