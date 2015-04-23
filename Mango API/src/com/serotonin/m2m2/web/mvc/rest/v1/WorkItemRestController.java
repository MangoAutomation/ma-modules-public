/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WorkItemModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Work Items", description="Operations on Work Items")
@RestController
@RequestMapping("/v1/work-items")
public class WorkItemRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(WorkItemRestController.class);
	
	@ApiOperation(value = "Get all work items", notes = "Returns a list of all work items, optionally filterable on classname")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<List<WorkItemModel>> getAll(@RequestParam(value = "classname", required = false, defaultValue="") String classname,
    		HttpServletRequest request) {
		RestProcessResult<List<WorkItemModel>> result = new RestProcessResult<List<WorkItemModel>>(HttpStatus.OK);
    	
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		if(Permissions.hasAdmin(user)){
    	    	List<WorkItemModel> modelList = new ArrayList<WorkItemModel>();
    	    	modelList.addAll(Common.backgroundProcessing.getHighPriorityServiceItems());
    	    	modelList.addAll(Common.backgroundProcessing.getMediumPriorityServiceQueueItems());
    	    	modelList.addAll(Common.backgroundProcessing.getLowPriorityServiceQueueItems());
    	    	if(!classname.isEmpty()){
    	    		List<WorkItemModel> filteredList = new ArrayList<WorkItemModel>();
    	    		for(WorkItemModel model : modelList){
    					if(model.getClassname().equalsIgnoreCase(classname)){
    						filteredList.add(model);
    					}
    				}
    	    		return result.createResponseEntity(filteredList);
    	    	}
    			return result.createResponseEntity(modelList);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access all work items");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
    }
	
	@ApiOperation(value = "Get list of work items by classname", notes = "Returns the Work Item specified by the given classname and priority")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/by-priority/{priority}")
    public ResponseEntity<List<WorkItemModel>> getWorkItemsByPriority(
    		@ApiParam(value = "priority", required = true, allowMultiple = false)
    		@PathVariable String priority,
    		@RequestParam(value = "classname", required = false, defaultValue="") String classname,
    		HttpServletRequest request) {
		
		RestProcessResult<List<WorkItemModel>> result = new RestProcessResult<List<WorkItemModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdmin(user)){
    			List<WorkItemModel> modelList = new ArrayList<WorkItemModel>();
    			List<WorkItemModel> list;
    			if(priority.equalsIgnoreCase("HIGH")){
    				list = Common.backgroundProcessing.getHighPriorityServiceItems();

    			}else if(priority.equalsIgnoreCase("MEDIUM")){
    				list = Common.backgroundProcessing.getMediumPriorityServiceQueueItems();
    				for(WorkItemModel model : list){
    					if(model.getClassname().equalsIgnoreCase(classname)){
    						modelList.add(model);
    					}
    				}
    			}else if(priority.equalsIgnoreCase("LOW")){
    				list = Common.backgroundProcessing.getLowPriorityServiceQueueItems();
    				for(WorkItemModel model : list){
    					if(model.getClassname().equalsIgnoreCase(classname)){
    						modelList.add(model);
    					}
    				}
    			}else{
    				//Return invalid input message
    				// TODO Create this type of method in the base class
    				result.addRestMessage(this.getInternalServerErrorMessage("Invalid Priority type"));
    				return result.createResponseEntity();
    			}
    			
    			//Filter if we need to
    			if(!classname.isEmpty()){
    				for(WorkItemModel model : list){
    					if(model.getClassname().equalsIgnoreCase(classname)){
    						modelList.add(model);
    					}
    				}
    			}
    			
    			return result.createResponseEntity(modelList);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to access work items.");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
}
