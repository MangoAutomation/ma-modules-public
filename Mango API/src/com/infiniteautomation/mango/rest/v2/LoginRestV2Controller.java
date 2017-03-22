/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Login/Switch User Actions
 * 
 * This controller 
 * 
 * @author Terry Packer
 */
@Api(value = "Login", description = "Login")
@RestController
@RequestMapping("/v2/login")
public class LoginRestV2Controller {

	//private static final Log LOG = LogFactory.getLog(LoginRestController.class);
	public static final String LOGIN_DEFAULT_URI_HEADER = "user-home-uri";

	/**
	 * The actual authentication for the login occurs in the core, by the time this
	 * end point is actually reached the user is either already authenticated or not
	 * The Spring Security authentication success handler forwards the request here
	 * @throws IOException 
	 */
	@ApiOperation(value = "Login", notes = "Used to login using POST and JSON credentials")
	@RequestMapping(method = RequestMethod.POST,  produces={"application/json"})
	public ResponseEntity<UserModel> loginPost(
            @AuthenticationPrincipal User user,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
	    
	    AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	    // TODO throw this here or maybe just throw it in login failure handler
	    // and catch it and turn it into a proper response elsewhere
	    // if we can do this perhaps add the @Secured annotation to this method
	    // or maybe use object mapper to write the response in the failure handler
	    
	    if (ex != null) {
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	        response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
	        return null;
	    }
	    
	    if (user == null) {
	        return new ResponseEntity<>(HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
	    }
	}
	
	/**
	 * The actual authentication for the switch user occurs in the core by the SwitchUserFilter,
	 *  by the time this end point is actually reached the user is either already authenticated or not
	 * The Spring Security authentication success handler forwards the request here
	 * @throws IOException 
	 */
	@ApiOperation(value = "Switch User", notes = "Used to switch User using GET")
	@RequestMapping(method = RequestMethod.GET,  value="/su", produces={"application/json"})
	public ResponseEntity<UserModel> switchUser(
			@ApiParam(value = "Username to switch to", required = true, allowMultiple = false)
    		@RequestParam(required=true) String username,
            @AuthenticationPrincipal(expression="user") User user,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
	    
	    AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	    // TODO throw this here or maybe just throw it in login failure handler
	    // and catch it and turn it into a proper response elsewhere
	    // if we can do this perhaps add the @Secured annotation to this method
	    // or maybe use object mapper to write the response in the failure handler
	    
	    if (ex != null) {
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	        response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
	        return null;
	    }
	    
	    if (user == null) {
	        return new ResponseEntity<>(HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
	    }
	}
	
	/**
	 * The actual authentication for the exit user occurs in the core by the SwitchUserFilter,
	 *  by the time this end point is actually reached the user is either already authenticated or not
	 * The Spring Security authentication success handler forwards the request here
	 * @throws IOException 
	 */
	@ApiOperation(value = "Exit Switch User", notes = "Used to switch User using GET")
	@RequestMapping(method = RequestMethod.GET,  value="/exit-su", produces={"application/json"})
	public ResponseEntity<UserModel> exitSwitchUser(
            @AuthenticationPrincipal User user,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
	    
	    AuthenticationException ex = (AuthenticationException) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	    // TODO throw this here or maybe just throw it in login failure handler
	    // and catch it and turn it into a proper response elsewhere
	    // if we can do this perhaps add the @Secured annotation to this method
	    // or maybe use object mapper to write the response in the failure handler
	    
	    if (ex != null) {
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	        response.sendError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
	        return null;
	    }
	    
	    if (user == null) {
	        return new ResponseEntity<>(HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>(new UserModel(user), HttpStatus.OK);
	    }
	}
	
}
