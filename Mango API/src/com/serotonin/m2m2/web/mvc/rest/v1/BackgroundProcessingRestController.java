/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.maint.BackgroundProcessing;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.backgroundProcessing.ThreadPoolSettingsModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Background Processing", description="Background Processing Settings")
@RestController
@RequestMapping("/background-processing")
public class BackgroundProcessingRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(BackgroundProcessingRestController.class);

	@ApiOperation(value = "Get the High Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
	@RequestMapping(method = RequestMethod.GET, value = "/high-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> getHighPriorityThreadPoolSettings( HttpServletRequest request) {
		
		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			ThreadPoolExecutor executor = (ThreadPoolExecutor) Common.timer.getExecutorService();
    			int corePoolSize = executor.getCorePoolSize();
    			int maximumPoolSize = executor.getMaximumPoolSize();
    			int activeCount = executor.getActiveCount();
    			int largestPoolSize = executor.getLargestPoolSize();
    			
    			ThreadPoolSettingsModel model = new ThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    			return result.createResponseEntity(model);
			}else{
				LOG.warn("Non admin user: " + user.getUsername() + " attempted to access high priority thread pool settings.");
				result.addRestMessage(this.getUnauthorizedMessage());
				return result.createResponseEntity();
			}
		}else{
			return result.createResponseEntity();
		}
 	}
	
	@ApiOperation(
			value = "Update high priority queue settings",
			notes = ""
	)
	@RequestMapping(method = RequestMethod.PUT, value = "/high-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> setHighPrioritySettings(
    		
    		@ApiParam(value = "Settings", required = true, allowMultiple = false)
    		@RequestBody
    		ThreadPoolSettingsModel model,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			//Validate the settings
    			ThreadPoolExecutor executor = (ThreadPoolExecutor) Common.timer.getExecutorService();
    			int currentCorePoolSize = executor.getCorePoolSize();
    			int currentMaxPoolSize = executor.getMaximumPoolSize();
    			
    			if((model.getMaximumPoolSize() != null)&&(model.getMaximumPoolSize() < BackgroundProcessing.HIGH_PRI_MAX_POOL_SIZE_MIN)){
    				//Test to ensure we aren't setting too low
    				model.getMessages().add(new RestValidationMessage(
    						new TranslatableMessage("validate.greaterThanOrEqualTo", BackgroundProcessing.HIGH_PRI_MAX_POOL_SIZE_MIN),
    						RestMessageLevel.ERROR,
    						"corePoolSize"));
    				result.addRestMessage(this.getValidationFailedError());
    			}else if(!validate(model, currentCorePoolSize, currentMaxPoolSize)){
    	        	result.addRestMessage(this.getValidationFailedError());
    	        }else{
	    			if(model.getCorePoolSize() != null){
	    				executor.setCorePoolSize(model.getCorePoolSize());
	    				SystemSettingsDao.instance.setIntValue(SystemSettingsDao.HIGH_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
	    			}else{
	    				//Get the info for the user
	        			int corePoolSize = executor.getCorePoolSize();
	        			model.setCorePoolSize(corePoolSize);
	    			}
	    			if(model.getMaximumPoolSize() != null){
	    				executor.setMaximumPoolSize(model.getMaximumPoolSize());
	    				SystemSettingsDao.instance.setIntValue(SystemSettingsDao.HIGH_PRI_MAX_POOL_SIZE, model.getMaximumPoolSize());
	    			}else{
	    				//Get the info for the user
	        			int maximumPoolSize = executor.getMaximumPoolSize();
	        			model.setMaximumPoolSize(maximumPoolSize);
	    			}
	    			//Get the settings for the model
	    			int activeCount = executor.getActiveCount();
	    			int largestPoolSize = executor.getLargestPoolSize();
	    			model.setActiveCount(activeCount);
	    			model.setLargestPoolSize(largestPoolSize);
    	        }
    			
    			return result.createResponseEntity(model);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to set high priority thread pool settings.");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get the Medium Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
	@RequestMapping(method = RequestMethod.GET, value = "/medium-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> getMediumPriorityThreadPoolSettings( HttpServletRequest request) {
		
		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			int corePoolSize = Common.backgroundProcessing.getMediumPriorityServiceCorePoolSize();
    			int maximumPoolSize = Common.backgroundProcessing.getMediumPriorityServiceMaximumPoolSize();
    			int activeCount = Common.backgroundProcessing.getMediumPriorityServiceActiveCount();
    			int largestPoolSize = Common.backgroundProcessing.getMediumPriorityServiceLargestPoolSize();
    			
    			ThreadPoolSettingsModel model = new ThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    			return result.createResponseEntity(model);
			}else{
				LOG.warn("Non admin user: " + user.getUsername() + " attempted to access medium priority thread pool settings.");
				result.addRestMessage(this.getUnauthorizedMessage());
				return result.createResponseEntity();
			}
		}
		return result.createResponseEntity();
 	}
	
	@ApiOperation(
			value = "Update medium priority queue settings",
			notes = "Only corePoolSize and maximumPoolSize are used"
	)
	@RequestMapping(method = RequestMethod.PUT, value = "/medium-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> setMediumPrioritySettings(
    		
    		@ApiParam(value = "Settings", required = true, allowMultiple = false)
    		@RequestBody
    		ThreadPoolSettingsModel model,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			//Validate the settings
    			int currentCorePoolSize = SystemSettingsDao.instance.getIntValue(SystemSettingsDao.MED_PRI_CORE_POOL_SIZE);
    			if((model.getCorePoolSize() != null)&&(model.getCorePoolSize() < BackgroundProcessing.MED_PRI_MAX_POOL_SIZE_MIN)){
    				//Test to ensure we aren't setting too low
    				model.getMessages().add(new RestValidationMessage(
    						new TranslatableMessage("validate.greaterThanOrEqualTo", BackgroundProcessing.MED_PRI_MAX_POOL_SIZE_MIN),
    						RestMessageLevel.ERROR,
    						"corePoolSize"));
    				result.addRestMessage(this.getValidationFailedError());
    			}else if(!validate(model, currentCorePoolSize, model.getCorePoolSize() == null ? currentCorePoolSize : model.getCorePoolSize())){
    	        	result.addRestMessage(this.getValidationFailedError());
    	        }else{
	    			if(model.getCorePoolSize() != null){
	    				Common.backgroundProcessing.setMediumPriorityServiceCorePoolSize(model.getCorePoolSize());
	    				SystemSettingsDao.instance.setIntValue(SystemSettingsDao.MED_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
	    			}else{
	    				//Get the info for the user
	        			int corePoolSize = Common.backgroundProcessing.getMediumPriorityServiceCorePoolSize();
	        			model.setCorePoolSize(corePoolSize);
	    			}
	    			if(model.getMaximumPoolSize() == null){
	    				//Get the info for the user
	        			int maximumPoolSize = Common.backgroundProcessing.getMediumPriorityServiceMaximumPoolSize();
	        			model.setMaximumPoolSize(maximumPoolSize);
	    			}
	    			//Get the settings for the model
	    			int activeCount = Common.backgroundProcessing.getMediumPriorityServiceActiveCount();
	    			int largestPoolSize = Common.backgroundProcessing.getMediumPriorityServiceLargestPoolSize();
	    			model.setActiveCount(activeCount);
	    			model.setLargestPoolSize(largestPoolSize);
    	        }
    			
    			return result.createResponseEntity(model);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to set medium priority thread pool settings.");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get the Low Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
	@RequestMapping(method = RequestMethod.GET, value = "/low-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> getLowPriorityThreadPoolSettings( HttpServletRequest request) {
		
		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			int corePoolSize = Common.backgroundProcessing.getLowPriorityServiceCorePoolSize();
    			int maximumPoolSize = Common.backgroundProcessing.getLowPriorityServiceMaximumPoolSize();
    			int activeCount = Common.backgroundProcessing.getLowPriorityServiceActiveCount();
    			int largestPoolSize = Common.backgroundProcessing.getLowPriorityServiceLargestPoolSize();
    			
    			ThreadPoolSettingsModel model = new ThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    			return result.createResponseEntity(model);
			}else{
				LOG.warn("Non admin user: " + user.getUsername() + " attempted to access low priority thread pool settings.");
				result.addRestMessage(this.getUnauthorizedMessage());
				return result.createResponseEntity();
			}
		}
		return result.createResponseEntity();
 	}
	
	@ApiOperation(
			value = "Update low priority service settings",
			notes = "Only corePoolSize and maximumPoolSize are used"
	)
	@RequestMapping(method = RequestMethod.PUT, value = "/low-priority-thread-pool-settings")
    public ResponseEntity<ThreadPoolSettingsModel> setLowPrioritySettings(
    		
    		@ApiParam(value = "Settings", required = true, allowMultiple = false)
    		@RequestBody
    		ThreadPoolSettingsModel model,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<ThreadPoolSettingsModel> result = new RestProcessResult<ThreadPoolSettingsModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdminPermission(user)){
    			//Validate the settings
    			int currentCorePoolSize = SystemSettingsDao.instance.getIntValue(SystemSettingsDao.LOW_PRI_CORE_POOL_SIZE);
    			
    			if((model.getCorePoolSize() != null)&&(model.getCorePoolSize() < BackgroundProcessing.LOW_PRI_MAX_POOL_SIZE_MIN)){
    				//Test to ensure we aren't setting too low
    				model.getMessages().add(new RestValidationMessage(
    						new TranslatableMessage("validate.greaterThanOrEqualTo", BackgroundProcessing.LOW_PRI_MAX_POOL_SIZE_MIN),
    						RestMessageLevel.ERROR,
    						"corePoolSize"));
    				result.addRestMessage(this.getValidationFailedError());
    			}else if(!validate(model, currentCorePoolSize, model.getCorePoolSize() == null ? currentCorePoolSize : model.getCorePoolSize())){
    			    result.addRestMessage(this.getValidationFailedError());
    	        }else{
	    			if(model.getCorePoolSize() != null){
	    				Common.backgroundProcessing.setLowPriorityServiceCorePoolSize(model.getCorePoolSize());
	    				SystemSettingsDao.instance.setIntValue(SystemSettingsDao.LOW_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
	    			}else{
	    				//Get the info for the user
	        			int corePoolSize = Common.backgroundProcessing.getLowPriorityServiceCorePoolSize();
	        			model.setCorePoolSize(corePoolSize);
	    			}
	    			if(model.getMaximumPoolSize() == null){
	    				//Get the info for the user
	        			int maximumPoolSize = Common.backgroundProcessing.getLowPriorityServiceMaximumPoolSize();
	        			model.setMaximumPoolSize(maximumPoolSize);
	    			}
	    			//Get the settings for the model
	    			int activeCount = Common.backgroundProcessing.getLowPriorityServiceActiveCount();
	    			int largestPoolSize = Common.backgroundProcessing.getLowPriorityServiceLargestPoolSize();
	    			model.setActiveCount(activeCount);
	    			model.setLargestPoolSize(largestPoolSize);
    	        }
    			
    			return result.createResponseEntity(model);
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to set low priority thread pool settings.");
    			result.addRestMessage(this.getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	
	/**
	 * Helper for validation
	 * @param model
	 * @param currentCorePoolSize
	 * @param currentMaximumPoolSize
	 * @return
	 */
	protected boolean validate(ThreadPoolSettingsModel model, int currentCorePoolSize, int currentMaximumPoolSize){
		boolean passed = true;
		model.setMessages(new ArrayList<RestValidationMessage>());
		if((model.getCorePoolSize() != null)&&(model.getCorePoolSize() < 1)){
			model.getMessages().add(new RestValidationMessage(
					new TranslatableMessage("validate.greaterThanZero"),
					RestMessageLevel.ERROR,
					"corePoolSize"));
			passed = false;
		}
		//Compare both if they are being added
		if((model.getMaximumPoolSize() != null)&&(model.getCorePoolSize() != null)&&(model.getMaximumPoolSize() < model.getCorePoolSize())){
			model.getMessages().add(new RestValidationMessage(
			        new TranslatableMessage("validate.maxGreaterThanMin"),
					RestMessageLevel.ERROR,
					"maximumPoolSize"));
			passed = false;
		}
		//Compare the max pool size to the existing if we aren't changing the Core size
		if((model.getCorePoolSize() == null)&&(model.getMaximumPoolSize() != null)){
			
			if(model.getMaximumPoolSize() < currentCorePoolSize){
				model.setCorePoolSize(currentCorePoolSize);
				model.getMessages().add(new RestValidationMessage(
				        new TranslatableMessage("validate.maxGreaterThanMin"),
						RestMessageLevel.ERROR,
						"maximumPoolSize"));
				passed = false;
			}
		}
		
		//Compare the max pool size to the existing if we aren't changing the Core size
		if((model.getMaximumPoolSize() == null)&&(model.getCorePoolSize() != null)){
			
			if(model.getCorePoolSize() > currentMaximumPoolSize){
				model.setMaximumPoolSize(currentMaximumPoolSize);
				model.getMessages().add(new RestValidationMessage(
				        new TranslatableMessage("validate.minLessThanMax"),
						RestMessageLevel.ERROR,
						"corePoolSize"));
				passed = false;
			}
		}	
			
		return passed;
	}
}
