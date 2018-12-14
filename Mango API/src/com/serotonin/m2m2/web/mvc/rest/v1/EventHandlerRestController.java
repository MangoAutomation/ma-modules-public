/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.EventHandlerDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.definitions.permissions.SuperadminPermissionDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredPageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventHandler.EventHandlerStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.handlers.AbstractEventHandlerModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * REST access for Event Handlers
 * 
 * @author Terry Packer
 */
@Api(value="Event Handlers", description="REST access to Event Handlers")
@RestController
@RequestMapping("/event-handlers")
public class EventHandlerRestController extends MangoVoRestController<AbstractEventHandlerVO<?>, AbstractEventHandlerModel<?>, EventHandlerDao>{

	private static Log LOG = LogFactory.getLog(EventHandlerRestController.class);
	
	/**
	 * @param dao
	 */
	public EventHandlerRestController() {
		super(EventHandlerDao.getInstance());
	}

	@ApiOperation(
			value = "Get EventHandler by XID",
			notes = "EventType permission required"
			)
	@RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<?>> get(
    		@ApiParam(value = "Valid Eventh Handler XID", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		RestProcessResult<AbstractEventHandlerModel<?>> result = new RestProcessResult<AbstractEventHandlerModel<?>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
	        AbstractEventHandlerVO<?> vo = EventHandlerDao.getInstance().getByXid(xid);
	        if (vo == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }else{
	        	//Check Permissions
	        	if(Permissions.hasAdminPermission(user))
	        		return result.createResponseEntity(vo.asModel());
	        	else
	        	    result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("permissions.accessDenied", user.getUsername(), SuperadminPermissionDefinition.GROUP_NAME));
	        }
	        
        }
        return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Query Event Handlers",
			notes = "Use RQL formatted query",
			response=AbstractEventHandlerModel.class,
			responseContainer="List"
			)
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<AbstractEventHandlerVO<?>>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<AbstractEventHandlerVO<?>>> result = new RestProcessResult<QueryDataPageStream<AbstractEventHandlerVO<?>>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
				ASTNode node = RQLUtils.parseRQLtoAST(request.getQueryString());
				EventHandlerStreamCallback callback = new EventHandlerStreamCallback(this, user);
				FilteredPageQueryStream<AbstractEventHandlerVO<?>, AbstractEventHandlerModel<?>, EventHandlerDao> stream = 
						new FilteredPageQueryStream<AbstractEventHandlerVO<?>, AbstractEventHandlerModel<?>, EventHandlerDao>(EventHandlerDao.getInstance(), this, node, callback);
				stream.setupQuery();
				return result.createResponseEntity(stream);
    		}catch(InvalidRQLRestException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}

	@ApiOperation(
			value = "Update an existing event handler",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<?>> update(
    		@PathVariable String xid,
    		@ApiParam(value = "Updated model", required = true)
    		@RequestBody(required=true) AbstractEventHandlerModel<?> model, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<AbstractEventHandlerModel<?>> result = new RestProcessResult<AbstractEventHandlerModel<?>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	
			AbstractEventHandlerVO<?> vo = model.getData();
			AbstractEventHandlerVO<?> existing = EventHandlerDao.getInstance().getByXid(xid);
	        if (existing == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	
	        //Check Event Type Permission
	        if(!Permissions.hasAdminPermission(user)){
				result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("permissions.accessDenied", user.getUsername(), SuperadminPermissionDefinition.GROUP_NAME));
				return result.createResponseEntity();
	        }
	        
	        //Ensure we keep the same ID
	        vo.setId(existing.getId());
	        
	        if(!model.validate()){
	        	result.addRestMessage(this.getValidationFailedError());
	        	return result.createResponseEntity(model); 
	        }else{
	        	String initiatorId = request.getHeader("initiatorId");
	        	EventHandlerDao.getInstance().saveFull(vo, initiatorId);
	        }
	        
	        //Put a link to the updated data in the header?
	    	URI location = builder.path("/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
	    	
	    	result.addRestMessage(getResourceUpdatedMessage(location));
	        return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Save a new event handler",
			notes = "User must have event type permission"
			)
	@RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventHandlerModel<?>> save(
    		@RequestBody(required=true) AbstractEventHandlerModel<?> model, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<AbstractEventHandlerModel<?>> result = new RestProcessResult<AbstractEventHandlerModel<?>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	
	        //Check Event Type Permission
	        if(!Permissions.hasAdminPermission(user)){
	            result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("permissions.accessDenied", user.getUsername(), SuperadminPermissionDefinition.GROUP_NAME));
				return result.createResponseEntity();
	        }
	        
	        //Set XID if required
	        if(StringUtils.isEmpty(model.getXid())){
	        	model.setXid(EventHandlerDao.getInstance().generateUniqueXid());
	        }
	        
	        if(!model.validate()){
	        	result.addRestMessage(this.getValidationFailedError());
	        	return result.createResponseEntity(model); 
	        }else{
				AbstractEventHandlerVO<?> vo = model.getData();
	        	String initiatorId = request.getHeader("initiatorId");
	        	EventHandlerDao.getInstance().saveFull(vo, initiatorId);
	        }
	        
	        //Put a link to the updated data in the header?
	    	URI location = builder.path("/event-handlers/{xid}").buildAndExpand(model.getXid()).toUri();
	    	
	    	result.addRestMessage(getResourceCreatedMessage(location));
	        return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Delete an event handler",
			notes = "The user must have event type permission"
			)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<?>> delete(@PathVariable String xid, UriComponentsBuilder builder, HttpServletRequest request) {
		RestProcessResult<AbstractEventHandlerModel<?>> result = new RestProcessResult<AbstractEventHandlerModel<?>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			AbstractEventHandlerVO<?> existing = EventHandlerDao.getInstance().getByXid(xid);
			if(existing == null) {
				result.addRestMessage(this.getDoesNotExistMessage());
				return result.createResponseEntity();
			}else {
		        //Check Event Type Permission
		        if(!Permissions.hasAdminPermission(user)){
		            result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("permissions.accessDenied", user.getUsername(), SuperadminPermissionDefinition.GROUP_NAME));
					return result.createResponseEntity();
		        }
		        //All Good Delete It
		        String initiatorId = request.getHeader("initiatorId");
		        EventHandlerDao.getInstance().delete(existing.getId(), initiatorId);
				return result.createResponseEntity(existing.asModel());
			}
		}
		else {
			return result.createResponseEntity();
		}
    }
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractBasicVO)
	 */
	@Override
	public AbstractEventHandlerModel<?> createModel(AbstractEventHandlerVO<?> vo) {
		if(vo != null)
			return vo.asModel();
		else
			return null;
	}


	/**
	 * Override to add check for Data Source Permissions since that is required
	 */
	@Override
	protected User checkUser(HttpServletRequest request, @SuppressWarnings("rawtypes") RestProcessResult result) {
		User user = super.checkUser(request, result);
		if(user != null){
			if(!Permissions.hasDataSourcePermission(user)){
				result.addRestMessage(HttpStatus.UNAUTHORIZED, new TranslatableMessage("common.default", "No Data Source Permission"));
			}
		}
		return user;
	}
	
}
