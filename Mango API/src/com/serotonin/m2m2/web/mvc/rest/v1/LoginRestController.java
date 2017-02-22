/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

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
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * Rest Login endpoint

 * @author Terry Packer
 * 
 */
@Api(value = "Login", description = "Login")
@RestController
@RequestMapping("/v1/login")
public class LoginRestController extends MangoRestController {

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

//	/**
//	 * TODO dont use plaintext password, use OAuth2 or HMAC etc
//	 * 
//     * GET login action to switch user if we are an admin
//     * @param username
//     * @return
//     */
//	@ApiOperation(value = "Switch User", notes = "Must be have Administrator priviledges to switch users")
//    @RequestMapping(method = RequestMethod.GET, value = "/su/{username}",  produces={"application/json"})
//    public ResponseEntity<UserModel> su(
//    		@ApiParam(value="Username of user to switch to")
//            @PathVariable String username,
//            HttpServletRequest request, HttpServletResponse response) {
//    	RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
//    	// check if user is already logged in, if logout == false just return the current user
//    	User user = this.checkUser(request, result);
//    	
//        if (result.isOk()) {
//        	if(user.isAdmin()){
//	        	User newUser = DaoRegistry.userDao.getUser(username);
//	        	
//	        	if(newUser == null){
//	        		result.addRestMessage(this.getDoesNotExistMessage());
//	        		return result.createResponseEntity();
//	        	}
//	        	String password = newUser.getPassword();
//	        	return performLogin(username, password, request, response, true, true);
//        	}else{
//    			result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("common.default", "User Not Admin"));
//        	}
//        }
//        
//        return result.createResponseEntity();
//        
//    }
//	
//	/**
//	 * Shared work for the login process.
//	 * 
//	 * The end result for a logged in user is to have the header user-home-url set 
//	 * as well as the homeUrl for the user model.
//	 * 
//	 * @param username
//	 * @param password
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	private ResponseEntity<UserModel> performLogin(String username, String password, HttpServletRequest request, HttpServletResponse response) {
//		
//		DataBinder binder = new DataBinder(User.class);
//		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
//		
//		// Hack for now to get a BindException object so we can use the Auth
//		// Defs to login.
//		BindException errors = new BindException(binder.getBindingResult());
//
//		try{
//			User user = Common.loginManager.performLogin(username, password, request, response, null, errors, logout, passwordEncrypted);
//			String uri = DefaultPagesDefinition.getDefaultUri(request,
//					response, user);
//			UserModel model = new UserModel(user);
//			if(StringUtils.isEmpty(model.getHomeUrl()))
//				model.setHomeUrl(uri);
//			result.addHeader(LOGIN_DEFAULT_URI_HEADER, uri);
//			//Assign new SessionId to thwart Session Hijacking attempts for pre-login sniffed requests
//        	request.changeSessionId();
//			return result.createResponseEntity(model);
//		}catch(TranslatableException e){
//			result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, e.getTranslatableMessage());
//			return result.createResponseEntity();
//		}
//	}
	
}
