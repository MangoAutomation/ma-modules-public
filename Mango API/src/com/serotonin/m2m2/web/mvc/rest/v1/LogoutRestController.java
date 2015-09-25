/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Logout", description="Logout")
@RestController
@RequestMapping("/v1/logout")
public class LogoutRestController extends MangoRestController{
    /**
     * GET Logout action
     * @param request
     * @param response
     * @return
     */
	@ApiOperation(value = "Logout", notes = "Logout current user")
    @RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<UserModel> logout(HttpServletRequest request,
            HttpServletResponse response) {
        return performLogout(request, response);
    }

	/**
	 * PUT Logout action
	 * @param username
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Logout", notes = "Deprecated, use GET method")
    @Deprecated
	@RequestMapping(method = RequestMethod.PUT, value = "/{username}", produces={"application/json"})
    public ResponseEntity<UserModel> logoutPut(
    		@PathVariable String username,
    		HttpServletRequest request,
    		HttpServletResponse response
    		) {
		return performLogout(request, response);
	}
	
	/**
	 * POST Logout action
	 * @param username
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "Logout", notes = "Deprecated, use GET method")
    @Deprecated
	@RequestMapping(method = RequestMethod.POST, value = "/{username}", produces={"application/json"})
    public ResponseEntity<UserModel> logoutPost(
    		@PathVariable String username,
    		HttpServletRequest request,
    		HttpServletResponse response
    		) {
		return performLogout(request, response);
	}

	/**
	 * Shared logout work
	 * 
	 * @param username
	 * @param request
	 * @param response
	 * @return
	 */
	private ResponseEntity<UserModel> performLogout(HttpServletRequest request,
	        HttpServletResponse response) {
		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
		
		// Check if the user is logged in.
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            Common.loginManager.performLogout(request, response);
            //Return an OK
            UserModel model = new UserModel(user);
            return result.createResponseEntity(model);
        
        }
      	return result.createResponseEntity();
	}
	
	
}
