/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.serotonin.m2m2.db.dao.UserCommentDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.query.QueryModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * TODO Flesh out after we get the events endpoint rest api defined
 * 
 * @author Terry Packer
 *
 */
@Api(value="User Comments", description="Operations on User Comments")
@RestController()
@RequestMapping("/v1/comments")
public class UserCommentRestController extends MangoVoRestController<UserCommentVO, UserCommentModel>{
	
	private static Log LOG = LogFactory.getLog(UserCommentRestController.class);
	
	public UserCommentRestController(){
		super(UserCommentDao.instance);
	}

	@ApiOperation(
			value = "Get all User Comments",
			notes = "",
			response=UserCommentModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=UserCommentModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<JsonArrayStream> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")Integer limit) {

        RestProcessResult<JsonArrayStream> result = new RestProcessResult<JsonArrayStream>(HttpStatus.OK);
        
        this.checkUser(request, result);
    	
        if(result.isOk()){
        	QueryModel query = new QueryModel();
        	query.setLimit(limit);
    		return result.createResponseEntity(getStream(query));
    	}
        return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query User Comments",
			notes = "",
			response=UserCommentModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=UserCommentModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/query")
    public ResponseEntity<JsonArrayStream> query(
    		
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) QueryModel query, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<JsonArrayStream> result = new RestProcessResult<JsonArrayStream>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		return result.createResponseEntity(getStream(query));
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
	@ApiResponses({
			@ApiResponse(code = 201, message = "User Created", response=UserCommentModel.class),
			@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
			@ApiResponse(code = 409, message = "User Already Exists")
			})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"})
    public ResponseEntity<UserCommentModel> createNewUserComment(
    		@ApiParam( value = "User Comment to save", required = true )
    		@RequestBody
    		UserCommentModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<UserCommentModel> result = new RestProcessResult<UserCommentModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    			
    		//Assign a userId if there isn't one
    		if(model.getUserId() == 0){
    			model.setUserId(user.getId());
    			model.setUsername(user.getUsername());
    		}
    		//Don't let non admin users create notes from other people
    		if((model.getUserId() != user.getId()) && !user.isAdmin()){
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    		
    		if(model.getTimestamp() <=0){
    			model.setTimestamp(System.currentTimeMillis());
    		}
    		if(model.validate()){
    			try{
	    			UserCommentDao.instance.save(model.getData());
	    			LOG.info("User with name/id: " + user.getUsername() + "/" + user.getId() + " created a User Comment for user: " + model.getData().getUserId());
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


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	public UserCommentModel createModel(UserCommentVO vo) {
		return new UserCommentModel(vo);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#mapComparisons(java.util.List)
	 */
	@Override
	public void mapComparisons(List<QueryComparison> list) {
		//Check for the attribute commentType
		for(QueryComparison param : list){
			if(param.getAttribute().equalsIgnoreCase("commentType")){
				param.setCondition(Integer.toString(UserCommentVO.COMMENT_TYPE_CODES.getId(param.getCondition())));
			}
		}
	}

	
}
