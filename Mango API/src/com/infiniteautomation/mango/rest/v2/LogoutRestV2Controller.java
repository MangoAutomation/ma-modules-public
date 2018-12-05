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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logout", description="Logout")
@RestController
@RequestMapping("/logout")
public class LogoutRestV2Controller {

	/**
	 * POST Logout action
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Logout", notes = "Perform logout using POST")
	@RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> logoutPost(
            @AuthenticationPrincipal User user,
    		HttpServletRequest request, HttpServletResponse response) {
	    if (user != null) {
	        throw new RuntimeException("Logout unsuccessful");
        }
	    return new ResponseEntity<>(HttpStatus.OK);
	}
}
