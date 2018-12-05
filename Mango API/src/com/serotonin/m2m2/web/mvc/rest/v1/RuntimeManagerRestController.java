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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Runtime Manager", description="Operations on Data Source Runtime Manager")
@RestController(value="RuntimeManagerRestController")
@RequestMapping("/runtime-manager")
public class RuntimeManagerRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(RuntimeManagerRestController.class);

	@ApiOperation(
			value = "Force Refresh a data point",
			notes = "Not all data sources implement this feature",
			response=Void.class
			)
	@RequestMapping(method = RequestMethod.PUT, value = "/force-refresh/{xid}")
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
	    		
	    		DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
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
	    			result.addRestMessage(getPointNotEnabledMessage(xid));
	        		return result.createResponseEntity();
	    		}
	    		
	    		Common.runtimeManager.forcePointRead(vo.getId());
	    	}
		}catch(Exception e){
    		result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
    	}
		
		return result.createResponseEntity();
	}

    @ApiOperation(
            value = "Relinquish the value of a data point",
            notes = "Only BACnet data points allow this",
            response=Void.class
            )
    @RequestMapping(method = RequestMethod.POST, value = "/relinquish/{xid}")
    public void relinquish(
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid, 
            @AuthenticationPrincipal User user,
            HttpServletRequest request){
        
        DataPointVO dataPoint = DataPointDao.getInstance().getByXid(xid);
        if (dataPoint == null)
            throw new NotFoundRestException();
        
        Permissions.ensureDataPointReadPermission(user, dataPoint);

        DataPointRT rt = Common.runtimeManager.getDataPoint(dataPoint.getId());
        if(rt == null)
            throw new GenericRestException(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.error.pointNotEnabled", xid));

        //Get the Data Source and Relinquish the point
        DataSourceRT<?> dsRt = Common.runtimeManager.getRunningDataSource(rt.getDataSourceId());
        if(dsRt == null)
            throw new GenericRestException(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.error.dataSourceNotEnabled", xid));
       
        dsRt.relinquish(rt);

    }
	
	/**
	 * @return
	 */
	private RestMessage getPointNotEnabledMessage(String dpXid) {
		return new RestMessage(HttpStatus.PRECONDITION_FAILED, new TranslatableMessage("rest.error.pointNotEnabled", dpXid));
	}
	
	
}
