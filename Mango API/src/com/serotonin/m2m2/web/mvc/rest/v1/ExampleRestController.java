/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * @author Terry Packer
 */
@Api(value="Example Controller", description="Useful for testing various responses")
@RestController
@RequestMapping("/v1/example")
public class ExampleRestController {

	@ApiOperation(value = "Example Permission Exception Response", notes = "")
	@ApiResponses({
		@ApiResponse(code = 401, message = "Unauthorized user access", response=ResponseEntity.class),
	})
	@RequestMapping( method = {RequestMethod.GET}, value = {"/permissions-exception"}, produces = {"application/json"} )
	public ResponseEntity<Object> alwaysFails(@AuthenticationPrincipal User user) {
		throw new PermissionException("I always fail.", user);
	}
	
}
