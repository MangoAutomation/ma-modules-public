/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Runtime Manager", description="Operations on Data Source Runtime Manager")
@RestController(value="RuntimeManagerRestController")
@RequestMapping("/v1/runtime-manager")

public class RuntimeManagerRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(RuntimeManagerRestController.class);

	@ApiOperation(
			value = "Force Refresh a data point",
			notes = "Not all data sources implement this feature",
			response=Void.class
			)
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json"}, value = "/force-refresh/{xid}")
	public ResponseEntity<Void> forceRefreshDataPoint(
			@ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
			@PathVariable String xid, HttpServletRequest request){
		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);

		try{
			User user = this.checkUser(request, result);
	    	if(result.isOk()){
	    		
	    		if(xid == null){
	    			result.addRestMessage(getDoesNotExistMessage());
	        		return result.createResponseEntity();
	    		}
	    		
	    		DataPointVO vo = DataPointDao.instance.getByXid(xid);
	    		if(vo == null){
	    			result.addRestMessage(getDoesNotExistMessage());
	        		return result.createResponseEntity();
	    		}
	    		
	    		try{
	    			if(!Permissions.hasDataPointReadPermission(user, vo)){
	    				LOG.warn("User " + user.getUsername() + " attempted to refesh data point  with xid: " + vo.getXid() + " without read permission");
		    			result.addRestMessage(getUnauthorizedMessage());
		        		return result.createResponseEntity();
	    			}
	    		}catch(PermissionException e){
					LOG.warn("User " + user.getUsername() + " attempted to refesh data point with xid: " + vo.getXid() + " without read permission");
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	    		}
	    		DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getId());
	    		if(rt == null){
	    			result.addRestMessage(getPointNotEnabledMessage());
	        		return result.createResponseEntity();
	    		}
	    		
	    		Common.runtimeManager.forcePointRead(vo.getId());
	    	}
		}catch(Exception e){
    		result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
    	}
		
		return result.createResponseEntity();
	}

	/**
	 * @return
	 */
	private RestMessage getPointNotEnabledMessage() {
		return new RestMessage(HttpStatus.PRECONDITION_FAILED, new TranslatableMessage("rest.error.pointNotEnabled"));
	}
	
	
}
