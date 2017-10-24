/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionDetails;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Users", description="Users")
@RestController
@RequestMapping("/v1/users")
public class UserRestController extends MangoVoRestController<User, UserModel, UserDao>{
	
	private static Log LOG = LogFactory.getLog(UserRestController.class);
	
	public UserRestController(){
		super(UserDao.instance);
	}

	@ApiOperation(value = "Get all users", notes = "Returns a list of all users")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value="list")
    public ResponseEntity<List<UserModel>> getAll(HttpServletRequest request) {
		RestProcessResult<List<UserModel>> result = new RestProcessResult<List<UserModel>>(HttpStatus.OK);
    	
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		if(Permissions.hasAdmin(user)){
    	    	List<UserModel> userModelList = new ArrayList<UserModel>();
    	    	List<User> users = UserDao.instance.getUsers();
    	    	for(User u : users){
    	    		userModelList.add(new UserModel(u));
    	    	}
    			return result.createResponseEntity(userModelList);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access all users");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
    }
	
	@ApiOperation(value = "Get current user", notes = "Returns the logged in user")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/current")
	public ResponseEntity<UserModel> getCurrentUser(HttpServletRequest request) {
	    RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
	    User user = this.checkUser(request, result);
	    
	    if (result.isOk()) {
            UserModel model = new UserModel(user);
            return result.createResponseEntity(model);
	    }

	    return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get user by name", notes = "Returns the user specified by the given username")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{username}")
    public ResponseEntity<UserModel> getUser(
    		@ApiParam(value = "Valid username", required = true, allowMultiple = false)
    		@PathVariable String username, HttpServletRequest request) {
		
		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(username);
    		if(Permissions.hasAdmin(user)){
    			if (u == null) {
    				result.addRestMessage(getDoesNotExistMessage());
    	    		return result.createResponseEntity();
    	        }
    			UserModel model = new UserModel(u);
    			return result.createResponseEntity(model);
    		}else{
    			if(u.getId() != user.getId()){
	    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
	    			result.addRestMessage(this.getUnauthorizedMessage());
	    			return result.createResponseEntity();
    			}else{
    				//Allow users to access themselves
    				return result.createResponseEntity(new UserModel(u));
    			}
    		}
    	}
    	
    	return result.createResponseEntity();
	}

	@ApiOperation(value = "Get new user", notes = "Returns a new user with default values")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/new/user")
    public ResponseEntity<UserModel> getNewUser(HttpServletRequest request) {
		
		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		return result.createResponseEntity(new UserModel(new User()));
    	}
    	return result.createResponseEntity();
	}
	
	
	@ApiOperation(value = "Updates a user")
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"}, value = "/{username}")
    public ResponseEntity<UserModel> updateUser(
    		@PathVariable String username,
    		@RequestBody(required=true) UserModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(username);
    		
    		if(Permissions.hasAdmin(user)){
    			if (u == null) {
    				result.addRestMessage(getDoesNotExistMessage());
    	    		return result.createResponseEntity();
    	        }

                // Cannot make yourself disabled or not admin
                if (user.getId() == u.getId()) {
                	boolean failed = false;
                    if (!model.isAdmin()){
                    	model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminInvalid")));
                    	failed = true;
                    }
                    if (model.getDisabled()){
                    	model.addValidationMessage(new ProcessMessage("disabled", new TranslatableMessage("users.validate.adminDisable")));
                    	failed = true;
                    }
                    if(failed){
                    	result.addRestMessage(getValidationFailedError());
                    	return result.createResponseEntity(model);
                    }
                }

                //Cannot Rename a User to an existing Username
                if(!model.getUsername().equals(username)){
                	User existingUser = UserDao.instance.getUser(model.getUsername());
                	if(existingUser != null){
                		model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.usernameInUse")));
                		result.addRestMessage(getValidationFailedError());
                    	return result.createResponseEntity(model);
                	}
                }
    			
    			//Set the ID for the user for validation
    			model.getData().setId(u.getId());
    	        if(!model.validate()){
    	        	result.addRestMessage(this.getValidationFailedError());
    	        }else{
    	        	User newUser = model.getData();
        			newUser.setId(u.getId());
        			if (!StringUtils.isBlank(model.getData().getPassword()))
        				newUser.setPassword(Common.encrypt(model.getData().getPassword()));
        			else
        				newUser.setPassword(u.getPassword());
        			UserDao.instance.saveUser(newUser);
    	        	this.maybeUpdateSessionUser(user, newUser, request);
    	        }
    			return result.createResponseEntity(model);
    		}else{
    			if(u.getId() != user.getId()){
	    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to update user : " + u.getUsername());
	    			result.addRestMessage(this.getUnauthorizedMessage());
	    			return result.createResponseEntity();
    			}else{
    				//Allow users to update themselves
    				User newUser = model.getData();
        			newUser.setId(u.getId());
    				if (!StringUtils.isBlank(model.getData().getPassword()))
        				newUser.setPassword(Common.encrypt(model.getData().getPassword()));
        			else
        				newUser.setPassword(u.getPassword());
    				
    				//If we are not Admin we cannot modify our own privs
    				if(!u.isAdmin()){
    					if(!StringUtils.equals(u.getPermissions(), newUser.getPermissions())){
    						model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.cannotChangePermissions")));
    						result.addRestMessage(this.getValidationFailedError());
    						return result.createResponseEntity(model);
    					}
    				}
    				
        	        if(!model.validate()){
        	        	result.addRestMessage(this.getValidationFailedError());
        	        }else{
        	        	
            			// Cannot make yourself disabled admin or not admin
            			boolean failed = false;
                        if (user.getId() == u.getId()) {
                            if (model.getDisabled()){
                            	model.addValidationMessage(new ProcessMessage("disabled", new TranslatableMessage("users.validate.adminDisable")));
                            	failed = true;
                            }
                            
                            if(u.isAdmin()){
                            	//We were superadmin, so we must still have it
                            	if(!model.getData().isAdmin()){
                                	model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminInvalid")));
                                	failed = true;
                            	}
                            }else{
                            	//We were not superadmin so we must not have it
                            	if(model.getData().isAdmin()){
                                	model.addValidationMessage(new ProcessMessage("permissions", new TranslatableMessage("users.validate.adminGrantInvalid")));
                                	failed = true;
                            	}
                            }
                            
                            if(failed){
                            	result.addRestMessage(getValidationFailedError());
                            	return result.createResponseEntity(model);
                            }
                        }
                		UserDao.instance.saveUser(newUser);
                        this.maybeUpdateSessionUser(user, newUser, request);
       	        	 	URI location = builder.path("v1/users/{username}").buildAndExpand(model.getUsername()).toUri();
       	        	 	result.addRestMessage(getResourceCreatedMessage(location));
        	        }
    				return result.createResponseEntity(model);
    			}
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	/**
	 * Create a new User
	 * @param model
	 * @param request
	 * @return
	 * @throws RestValidationFailedException 
	 */
	@ApiOperation(
			value = "Create New User",
			notes = "Cannot save existing user"
			)
	@ApiResponses({
			@ApiResponse(code = 201, message = "User Created", response=UserModel.class),
			@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
			@ApiResponse(code = 409, message = "User Already Exists")
			})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"})
    public ResponseEntity<UserModel> createNewUser(
    		@ApiParam( value = "User to save", required = true )
    		@RequestBody(required=true)
    		UserModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.CREATED);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(model.getUsername());
    		if(Permissions.hasAdmin(user)){
    			if (u == null) {
    				//Create new user
    				model.getData().setId(Common.NEW_ID);
    				if(model.validate()){
	    				try{
	    					User newUser = model.getData();
	    					newUser.setPassword(Common.encrypt(model.getData().getPassword()));
	    		        	UserDao.instance.saveUser(newUser);
	        		    	URI location = builder.path("v1/users/{username}").buildAndExpand(model.getUsername()).toUri();
	        		    	result.addRestMessage(getResourceCreatedMessage(location));
	        		        return result.createResponseEntity(model);
	        			}catch(Exception e){
	        				result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
	        				return result.createResponseEntity();
	        			}
    				}else{
    				
    		        	result.addRestMessage(this.getValidationFailedError());
    		        	return result.createResponseEntity(model); 
    				}
    	        }else{
    	        	model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.usernameInUse")));
            		result.addRestMessage(getValidationFailedError());
    	        	return result.createResponseEntity(model);
    	        }
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to create user : " + model.getUsername());
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Update a user's home url")
	@RequestMapping(method = RequestMethod.PUT,  produces={"application/json", "text/csv"}, value = "/{username}/homepage")
    public ResponseEntity<UserModel> updateHomeUrl(
    		@ApiParam(value = "Username", required = true, allowMultiple = false)
    		@PathVariable String username,
    		
    		@ApiParam(value = "Home Url", required = true, allowMultiple = false)
    		@RequestParam(required=true)
    		String url,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(username);
    		if(Permissions.hasAdmin(user)){
    			if (u == null) {
    				result.addRestMessage(getDoesNotExistMessage());
    	    		return result.createResponseEntity();
    	        }
    			u.setHomeUrl(url);
    			UserModel model = new UserModel(u);
    	        if(!model.validate()){
    	        	result.addRestMessage(this.getValidationFailedError());
    	        }else{
    	            UserDao.instance.saveHomeUrl(u.getId(), url);
    	            this.maybeUpdateSessionUser(user, u, request);
    	        }
    			return result.createResponseEntity(model);
    		}else{
    			if(u.getId() != user.getId()){
	    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
	    			result.addRestMessage(this.getUnauthorizedMessage());
	    			return result.createResponseEntity();
    			}else{
    				u.setHomeUrl(url);
    				UserModel model = new UserModel(u);
    				//Allow users to update themselves
    				model.getData().setId(u.getId());
        	        if(!model.validate()){
        	        	result.addRestMessage(this.getValidationFailedError());
        	        }else{
        	        	//We have confirmed that we are the user
         	            UserDao.instance.saveHomeUrl(u.getId(), url);
        	            this.maybeUpdateSessionUser(user, u, request);
        	        }
    				return result.createResponseEntity(model);
    			}
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Update a user's audio mute setting",
			notes = "If you do not provide the mute parameter the current setting will be toggled"
	)
	@RequestMapping(method = RequestMethod.PUT,  produces={"application/json", "text/csv"}, value = "/{username}/mute")
    public ResponseEntity<UserModel> updateMuted(
    		@ApiParam(value = "Username", required = true, allowMultiple = false)
    		@PathVariable String username,
    		
    		@ApiParam(value = "Mute", required = false, defaultValue="Toggle the current setting", allowMultiple = false)
    		@RequestParam(required=false)
    		Boolean mute,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(username);
    		if(Permissions.hasAdmin(user)){
    			if (u == null) {
    				result.addRestMessage(getDoesNotExistMessage());
    	    		return result.createResponseEntity();
    	        }
    			if(mute == null){
    				u.setMuted(!u.isMuted());
    			}else{
    				u.setMuted(mute);
    			}
    			UserModel model = new UserModel(u);
    	        if(!model.validate()){
    	        	result.addRestMessage(this.getValidationFailedError());
    	        }else{
    	    		UserDao.instance.saveUser(model.getData());
    	        	this.maybeUpdateSessionUser(user, model.getData(), request);
    	        }
    			return result.createResponseEntity(model);
    		}else{
    			if(u.getId() != user.getId()){
	    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access user : " + u.getUsername());
	    			result.addRestMessage(this.getUnauthorizedMessage());
	    			return result.createResponseEntity();
    			}else{
        			if(mute == null){
        				u.setMuted(!u.isMuted()); //Toggle
        			}else{
        				u.setMuted(mute);
        			}
    				UserModel model = new UserModel(u);
    				//Allow users to update themselves
    				model.getData().setId(u.getId());
        	        if(!model.validate()){
        	        	result.addRestMessage(this.getValidationFailedError());
        	        }else{
        	    		UserDao.instance.saveUser(model.getData());
        	        	this.maybeUpdateSessionUser(user, model.getData(), request);
        	        }
    				return result.createResponseEntity(model);
    			}
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query Users",
			notes = "",
			response=UserModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=UserModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<QueryDataPageStream<User>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<User>> result = new RestProcessResult<QueryDataPageStream<User>>(HttpStatus.OK);
    	
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			//Parse the RQL Query
	    		ASTNode query = this.parseRQLtoAST(request);
	    		if(!user.isAdmin()){
	    			query = addAndRestriction(query, new ASTNode("eq", "id", user.getId()));
	    		}
	    		return result.createResponseEntity(getPageStream(query));
    		}catch(InvalidRQLRestException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get User Permissions Information for all users",
			notes = "",
			response=PermissionDetails.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=PermissionDetails.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/permissions")
    public ResponseEntity<List<PermissionDetails>> getUserPermissions(HttpServletRequest request) {
		
		RestProcessResult<List<PermissionDetails>> result = new RestProcessResult<List<PermissionDetails>>(HttpStatus.OK);
    	
		User currentUser = this.checkUser(request, result);
    	if(result.isOk()){

	        List<PermissionDetails> ds = new ArrayList<>();
	        for (User user : UserDao.instance.getActiveUsers()){
	        	PermissionDetails deets = Permissions.getPermissionDetails(currentUser, null, user);
	        	if(deets != null)
	        		ds.add(deets);
	        }
    		return result.createResponseEntity(ds);
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get User Permissions Information for all users, exclude provided groups in query",
			notes = "",
			response=PermissionDetails.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=PermissionDetails.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/permissions/{query}")
    public ResponseEntity<List<PermissionDetails>> getUserPermissions(
    		@ApiParam(value = "Query of permissions to show as already added", required = true, allowMultiple = false)
    		@PathVariable String query,
    		HttpServletRequest request) {
		
		RestProcessResult<List<PermissionDetails>> result = new RestProcessResult<List<PermissionDetails>>(HttpStatus.OK);
    	
		User currentUser = this.checkUser(request, result);
    	if(result.isOk()){

	        List<PermissionDetails> ds = new ArrayList<>();
	        for (User user : UserDao.instance.getActiveUsers()){
	        	PermissionDetails deets = Permissions.getPermissionDetails(currentUser, query, user);
	        	if(deets != null)
	        		ds.add(deets);
	        }
    		return result.createResponseEntity(ds);
    	}
    	
    	return result.createResponseEntity();
	}
	
	
	@ApiOperation(
			value = "Get All User Groups",
			notes = "",
			response=String.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/permissions-groups")
    public ResponseEntity<Set<String>> getAllUserGroups(HttpServletRequest request) {
		
		RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){

            Set<String> groups = new TreeSet<>();

            for (User user : UserDao.instance.getActiveUsers())
                groups.addAll(Permissions.explodePermissionGroups(user.getPermissions()));
            
    		return result.createResponseEntity(groups);
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get All User Groups, Optionally excluding groups",
			notes = "",
			response=String.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/permissions-groups/{exclude}")
    public ResponseEntity<Set<String>> getAllUserGroups(
    		@ApiParam(value = "Exclude Groups comma separated", required = false, allowMultiple = false, defaultValue="")
    		@PathVariable String exclude,
    		HttpServletRequest request) {
		
		RestProcessResult<Set<String>> result = new RestProcessResult<Set<String>>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){

            Set<String> groups = new TreeSet<>();

            for (User user : UserDao.instance.getActiveUsers())
                groups.addAll(Permissions.explodePermissionGroups(user.getPermissions()));

            if (!StringUtils.isEmpty(exclude)) {
                for (String part : exclude.split(","))
                    groups.remove(part);
            }
    		return result.createResponseEntity(groups);
    	}
    	
    	return result.createResponseEntity();
	}
	
	
	@ApiOperation(value = "Delete A User")
	@RequestMapping(method = RequestMethod.DELETE,  produces={"application/json", "text/csv"}, value = "/{username}")
    public ResponseEntity<UserModel> deleteUser(
    		@ApiParam(value = "Username", required = true, allowMultiple = false)
    		@PathVariable String username,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserModel> result = new RestProcessResult<UserModel>(HttpStatus.OK);
		
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		User u = UserDao.instance.getUser(username);
			if (u == null) {
				result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
    		
    		UserModel model = new UserModel(u);
    		if(Permissions.hasAdmin(user)){
    			if(u.getId() == user.getId()){
                	model.addValidationMessage(new ProcessMessage("username", new TranslatableMessage("users.validate.badDelete")));
                	result.addRestMessage(getValidationFailedError());
                	return result.createResponseEntity(model);
    			}
    			UserDao.instance.deleteUser(u.getId());
    			return result.createResponseEntity(model);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to delete user : " + u.getUsername());
    			result.addRestMessage(this.getUnauthorizedMessage());
    		}
    	}
		
		return result.createResponseEntity();
	}
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(java.lang.Object)
	 */
	@Override
	public UserModel createModel(User vo) {
		return new UserModel(vo);
	}
	
	/**
	 * Ensure we update the user in the HttpSession if that user is us.  Otherwise 
	 * the User's session will be expired (at the Dao level) if we are editing another user.
	 * @param currentUser
	 * @param newUser
	 * @param request
	 */
	private void maybeUpdateSessionUser(User currentUser, User newUser, HttpServletRequest request){
		if(currentUser.getId() == newUser.getId()){
			//Update in HttpSession
			Common.setHttpUser(request, newUser);
		}
	}
}
