/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.db.query.appender.ExportCodeColumnQueryAppender;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.UserCommentDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * 
 * TODO Flesh out after we get the events endpoint rest api defined
 * 
 * @author Terry Packer
 *
 */
@Api(value="User Comments", description="User Comments")
@RestController()
@RequestMapping("/comments")
public class UserCommentRestController extends MangoVoRestController<UserCommentVO, UserCommentModel, UserCommentDao>{
	
	private static Log LOG = LogFactory.getLog(UserCommentRestController.class);
	
	public UserCommentRestController(){
		super(UserCommentDao.getInstance());
		
		this.appenders.put("commentType", new ExportCodeColumnQueryAppender(UserCommentVO.COMMENT_TYPE_CODES));
		
	}

	@ApiOperation(
			value = "Get all User Comments",
			notes = "",
			response=UserCommentModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, value="/list")
    public ResponseEntity<QueryStream<UserCommentVO, UserCommentModel, UserCommentDao>> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")Integer limit) {

        RestProcessResult<QueryStream<UserCommentVO, UserCommentModel, UserCommentDao>> result = new RestProcessResult<QueryStream<UserCommentVO, UserCommentModel, UserCommentDao>>(HttpStatus.OK);
        
        this.checkUser(request, result);
    	
        if(result.isOk()){
        	return result.createResponseEntity(getStream(new ASTNode("limit", limit)));
    	}
        return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query User Comments",
			notes = "",
			response=UserCommentModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.POST, value = "/query")
    public ResponseEntity<QueryDataPageStream<UserCommentVO>> query(
    		
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) ASTNode query, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<UserCommentVO>> result = new RestProcessResult<QueryDataPageStream<UserCommentVO>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		return result.createResponseEntity(getPageStream(query));
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query User Comments",
			notes = "",
			response=UserCommentModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<UserCommentVO>> queryRQL(
    		   		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<UserCommentVO>> result = new RestProcessResult<QueryDataPageStream<UserCommentVO>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		try{
	    		ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
	    		return result.createResponseEntity(getPageStream(query));
    		}catch(InvalidRQLRestException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	/**
	 * Create a new User Comment
	 * 
	 * The timestamp and UserID are optional
	 * Username is not used for input
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws RestValidationFailedException 
	 */
	@ApiOperation(
			value = "Create New User Comment",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserCommentModel> createNewUserComment(
    		@ApiParam( value = "User Comment to save", required = true )
    		@RequestBody(required=true)
    		UserCommentModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserCommentModel> result = new RestProcessResult<UserCommentModel>(HttpStatus.CREATED);
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    			
    		//Assign a userId if there isn't one
    		if(model.getUserId() == 0){
    			model.setUserId(user.getId());
    			model.setUsername(user.getUsername());
    		}
    		//Don't let non admin users create notes from other people
    		if(!hasEditPermission(model.getData(), user)){
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    		
    		if(model.getTimestamp() <=0){
    			model.setTimestamp(System.currentTimeMillis());
    		}
    		if(model.validate()){
    			try{
    				String initiatorId = request.getHeader("initiatorId");
	    			UserCommentDao.getInstance().save(model.getData(), initiatorId);
	    			LOG.info("User with name/id: " + user.getUsername() + "/" + user.getId() + " created a User Comment for user: " + model.getData().getUserId());
	    			URI location = builder.path("v1/comments/{xid}").buildAndExpand(model.getXid()).toUri();
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
    	}
    	
    	return result.createResponseEntity();
	}

	@ApiOperation(value = "Delete A User Comment by XID")
	@RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ResponseEntity<UserCommentModel> deleteUserComment(
    		@ApiParam(value = "xid", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserCommentModel> result = new RestProcessResult<UserCommentModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		UserCommentVO u = UserCommentDao.getInstance().getByXid(xid);
			if (u == null) {
				result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
    		
    		//Check permissions
    		if(hasEditPermission(u, user)){
    			//Delete it
    			String initiatorId = request.getHeader("initiatorId");
    			UserCommentDao.getInstance().delete(u.getId(), initiatorId);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to delete user comment : " + u.getUsername());
    			result.addRestMessage(this.getUnauthorizedMessage());
    		}
    	}
		
		return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get user comment by xid", notes = "Returns the user comment specified by the given xid")
	@RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<UserCommentModel> getUserComment(
    		@ApiParam(value = "Valid xid", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<UserCommentModel> result = new RestProcessResult<UserCommentModel>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		UserCommentVO u = UserCommentDao.getInstance().getByXid(xid);
			if (u == null) {
				result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }else{
			
	        	UserCommentModel model = new UserCommentModel(u);
	        	return result.createResponseEntity(model);
    		}
    	}
    	return result.createResponseEntity();
	}

	@ApiOperation(value = "Updates a user comment")
	@RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<UserCommentModel> updateUserComment(
    		@PathVariable String xid,
    		@RequestBody(required=true) UserCommentModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserCommentModel> result = new RestProcessResult<UserCommentModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		UserCommentVO u = UserCommentDao.getInstance().getByXid(xid);

			if (u == null) {
				result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    		
	        }else{
        		//Change the owner
        		if(model.getUserId() == 0){
        			model.setUserId(user.getId());
        			model.setUsername(user.getUsername());
        		}
        		//Check permissions
	        	if(hasEditPermission(model.getData(), user)){
		        	//Validate and Update
		        	if(!model.validate()){
	    	        	result.addRestMessage(this.getValidationFailedError());
	    	        }else{
	    	        	UserCommentDao.getInstance().save(model.getData());
	    	        	URI location = builder.path("v1/comments/{xid}").buildAndExpand(model.getXid()).toUri();
	    		    	result.addRestMessage(getResourceUpdatedMessage(location));
	    	        }
		        	return result.createResponseEntity(model);
	        	}else{
		        	result.addRestMessage(this.getUnauthorizedMessage());
	    			return result.createResponseEntity();
	        	}
	        }
    	}
    	return result.createResponseEntity();
	}
	
	/**
	 * A user has comment permission if they are Admin or Created the comment.
	 * @param vo
	 * @param user
	 * @return
	 */
	private boolean hasEditPermission(UserCommentVO vo, User user){
		if(user.isAdmin())
			return true;
		else if(vo.getUserId() == user.getId())
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	public UserCommentModel createModel(UserCommentVO vo) {
		return new UserCommentModel(vo);
	}
	
}
