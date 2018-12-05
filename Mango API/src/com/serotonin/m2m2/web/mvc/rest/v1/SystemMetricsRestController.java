/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Allows access to all System Metrics that are Monitored.
 * 
 * There are many core metrics and modules may also install thier own metrics.
 * 
 * 
 * @author Terry Packer
 */
@Api(value="System Metrics", description="Access to the current value for any System Metric")
@RestController
@RequestMapping("/system-metrics")
public class SystemMetricsRestController extends MangoRestController{

	//Permissions Definition for Internal Metrics
	private final String internalMetricsPermission = "internal.status";
	
	@ApiOperation(
			value = "Get the current value for all System Metrics",
			notes = "TBD Add RQL Support to this endpoint"
			)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ValueMonitor<?>>> query(HttpServletRequest request){
        
    	RestProcessResult<List<ValueMonitor<?>>> result = new RestProcessResult<List<ValueMonitor<?>>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		//Check Permissions
    		String permissions = SystemSettingsDao.instance.getValue(internalMetricsPermission);
    		if(Permissions.hasPermission(user, permissions)){
    			return result.createResponseEntity(Common.MONITORED_VALUES.getMonitors());
    		}else{
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
    		}
    		
    	}
    	return result.createResponseEntity();
	}
 
	@ApiOperation(
			value = "Get the current value for one System Metric by its ID",
			notes = ""
			)
    @RequestMapping(method = RequestMethod.GET, value="/{id}")
    public ResponseEntity<ValueMonitor<?>> get(
    		@ApiParam(value = "Valid Monitor id", required = true, allowMultiple = false)
    		@PathVariable String id, HttpServletRequest request) {

    	RestProcessResult<ValueMonitor<?>> result = new RestProcessResult<ValueMonitor<?>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		//Check Permissions
    		String permissions = SystemSettingsDao.instance.getValue(internalMetricsPermission);
    		if(Permissions.hasPermission(user, permissions)){
    			List<ValueMonitor<?>> values = Common.MONITORED_VALUES.getMonitors();
    			ValueMonitor<?> value = null;
    			for(ValueMonitor<?> v : values){
    				if(v.getId().equals(id)){
    					value = v;
    					break;
    				}
    			}
    			if(value != null)
    				return result.createResponseEntity(value);
    			else{
    				result.addRestMessage(getDoesNotExistMessage());
    			}
    		}else{
    			result.addRestMessage(getUnauthorizedMessage());
    		}
    	}
    	return result.createResponseEntity();
	}
}
