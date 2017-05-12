/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.ReadOnlySettingDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Access to read only settings
 * 
 * @author Terry Packer
 */
@Api(value="Read Only Settings", description="Mango system information that is read only.")
@RestController
@RequestMapping("/v2/read-only-settings")
public class ReadOnlySettingRestController {

	@ApiOperation(value = "Get all available read only settings", notes = "")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
	})
	@RequestMapping( method = {RequestMethod.GET}, produces = {"application/json"} )
	public ResponseEntity<List<ReadOnlySettingDefinition<?>>> getAll(@AuthenticationPrincipal User user) {
		RestProcessResult<List<ReadOnlySettingDefinition<?>>> result = new RestProcessResult<>(HttpStatus.OK);
		
		return result.createResponseEntity(ModuleRegistry.getReadOnlySettingDefinitions());
	}

	@ApiOperation(value = "Get one read only setting by key", notes = "")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "Not Found", response=ResponseEntity.class),
	})
	@RequestMapping( method = {RequestMethod.GET}, value="/{key}", produces = {"application/json"} )
	public ResponseEntity<ReadOnlySettingDefinition<?>> getOne(@AuthenticationPrincipal User user,
			@ApiParam(value = "Valid Read Only Setting Key", required = true, allowMultiple = false)
			@PathVariable String key) {
		RestProcessResult<ReadOnlySettingDefinition<?>> result = new RestProcessResult<>(HttpStatus.OK);
		
		List<ReadOnlySettingDefinition<?>> settings = ModuleRegistry.getReadOnlySettingDefinitions();
		
		for(ReadOnlySettingDefinition<?> setting : settings){
			if(StringUtils.equals(setting.getKey(), key))
				return result.createResponseEntity(setting);
		}
		
		throw new NotFoundRestException();
	}
	
}
