/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.workItem.BackgroundProcessingQueueCounts;
import com.serotonin.m2m2.web.mvc.rest.v1.model.workItem.BackgroundProcessingRejectedTaskStats;
import com.serotonin.m2m2.web.mvc.rest.v1.model.workItem.BackgroundProcessingRunningStats;
import com.serotonin.m2m2.web.mvc.rest.v1.model.workitem.WorkItemModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Work Items", description="Mango Work Items")
@RestController
@RequestMapping("/work-items")
public class WorkItemRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(WorkItemRestController.class);
	
	@ApiOperation(value = "Get all work items", notes = "Returns a list of all work items, optionally filterable on classname")
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<WorkItemModel>> getAll(@RequestParam(value = "classname", required = false, defaultValue="") String classname,
    		HttpServletRequest request) {
		RestProcessResult<List<WorkItemModel>> result = new RestProcessResult<List<WorkItemModel>>(HttpStatus.OK);
    	
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		if(Permissions.hasAdminPermission(user)){
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
	@RequestMapping(method = RequestMethod.GET, value = "/by-priority/{priority}")
    public ResponseEntity<List<WorkItemModel>> getWorkItemsByPriority(
    		@ApiParam(value = "priority", required = true, allowMultiple = false)
    		@PathVariable String priority,
    		@RequestParam(value = "classname", required = false, defaultValue="") String classname,
    		HttpServletRequest request) {
		
		RestProcessResult<List<WorkItemModel>> result = new RestProcessResult<List<WorkItemModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			List<WorkItemModel> modelList = new ArrayList<WorkItemModel>();
    			List<WorkItemModel> list;
    			if(priority.equalsIgnoreCase("HIGH")){
    				list = Common.backgroundProcessing.getHighPriorityServiceItems();
    			}else if(priority.equalsIgnoreCase("MEDIUM")){
    				list = Common.backgroundProcessing.getMediumPriorityServiceQueueItems();
    			}else if(priority.equalsIgnoreCase("LOW")){
    				list = Common.backgroundProcessing.getLowPriorityServiceQueueItems();
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
    			}else{
    				for(WorkItemModel model : list){
   						modelList.add(model);
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
	
	
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Queued Work Item Counts", notes = "Returns Work Item names to instance count for High, Medium and Low thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/queue-counts")
    public ResponseEntity<BackgroundProcessingQueueCounts> getQueueCounts(HttpServletRequest request) throws IOException {
        
        BackgroundProcessingQueueCounts model = new BackgroundProcessingQueueCounts();
        model.setHighPriorityServiceQueueClassCounts(Common.backgroundProcessing.getHighPriorityServiceQueueClassCounts());
        model.setMediumPriorityServiceQueueClassCounts(Common.backgroundProcessing.getMediumPriorityServiceQueueClassCounts());
        model.setLowPriorityServiceQueueClassCounts(Common.backgroundProcessing.getLowPriorityServiceQueueClassCounts());
        
        return ResponseEntity.ok(model);
    }
    
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Running Work Item Statistics", notes = "Returns information on all tasks running in the High and Medium thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/running-stats")
    public ResponseEntity<BackgroundProcessingRunningStats> getRunningStats(HttpServletRequest request) throws IOException {
        
        BackgroundProcessingRunningStats model = new BackgroundProcessingRunningStats();
        model.setHighPriorityOrderedQueueStats(Common.backgroundProcessing.getHighPriorityOrderedQueueStats());
        model.setMediumPriorityOrderedQueueStats(Common.backgroundProcessing.getMediumPriorityOrderedQueueStats());
        
        return ResponseEntity.ok(model);
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Get Rejected Task Statistics", notes = "Returns information on all tasks rejected from the High and Medium thread pools")
    @RequestMapping(method = RequestMethod.GET, value = "/rejected-stats")
    public ResponseEntity<BackgroundProcessingRejectedTaskStats> getRejectedStats(HttpServletRequest request) throws IOException {
        
        BackgroundProcessingRejectedTaskStats model = new BackgroundProcessingRejectedTaskStats();
        model.setHighPriorityRejectedTaskStats(Common.backgroundProcessing.getHighPriorityRejectionHandler().getRejectedTaskStats());
        model.setMediumPriorityRejectedTaskStats(Common.backgroundProcessing.getMediumPriorityRejectionHandler().getRejectedTaskStats());
        
        return ResponseEntity.ok(model);
    }
    
}
