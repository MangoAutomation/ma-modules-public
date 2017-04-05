/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.vo.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logout", description="Logout")
@RestController
@RequestMapping("/v2/logout")
public class LogoutV2RestController {

	/**
	 * POST Logout action
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Logout", notes = "Perform logout using POST")
	@RequestMapping(method = RequestMethod.POST, produces={"application/json"})
    public ResponseEntity<Void> logoutPost(
            @AuthenticationPrincipal User user,
    		HttpServletRequest request, HttpServletResponse response) {
	    if (user != null) {
	        throw new RuntimeException("Logout unsuccessful");
        }
	    return new ResponseEntity<>(HttpStatus.OK);
	}
}
