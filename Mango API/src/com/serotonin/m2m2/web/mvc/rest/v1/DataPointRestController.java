/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.view.text.PlainRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.template.DataPointPropertiesTemplateVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @author Terry Packer
 * 
 */
@Api(value="Data Points", description="Operations on Data points", position=1)
@RestController(value="DataPointRestControllerV1")
@RequestMapping("/v1/dataPoints")
public class DataPointRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(DataPointRestController.class);
	
	public DataPointRestController(){
		LOG.info("Creating Data Point Rest Controller.");
	}

	
	@ApiOperation(
			value = "Get all data points",
			notes = "Only returns points available to logged in user"
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<List<DataPointModel>> getAllDataPoints(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")int limit) {

        
        RestProcessResult<List<DataPointModel>> result = new RestProcessResult<List<DataPointModel>>(HttpStatus.OK);
        
        User user = this.checkUser(request, result);
        if(result.isOk()){
        	
           	List<DataPointVO> dataPoints = DaoRegistry.dataPointDao.getAll();
            List<DataPointModel> userDataPoints = new ArrayList<DataPointModel>();
        	
	        for(DataPointVO vo : dataPoints){
	        	try{
	        		if(Permissions.hasDataPointReadPermission(user, vo)){
	        			userDataPoints.add(new DataPointModel(vo));
	        			limit--;
	        		}
	        		//Check the limit, TODO make this work like the DOJO Query
	        		if(limit <= 0)
	        			break;
	        	}catch(PermissionException e){
	        		//Munched
	        		//TODO maybe don't throw this from check permissions?
	        	}
	        }
	        result.addRestMessage(getSuccessMessage());
	        return result.createResponseEntity(userDataPoints);
        }
        
        return result.createResponseEntity();
    }
	
	
	@ApiOperation(
			value = "Get existing data point",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<DataPointModel> getDataPoint(
    		@ApiParam(value = "Valid Data Point XIDs", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {

		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
	        DataPointVO vo = DataPointDao.instance.getByXid(xid);
	        if (vo == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        //Check permissions
	    	try{
	    		if(Permissions.hasDataPointReadPermission(user, vo))
	    			return result.createResponseEntity(new DataPointModel(vo));
	    		else{
	    			LOG.warn("User: " + user.getUsername() + " tried to access data point with xid " + vo.getXid());
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	    		}
	    	}catch(PermissionException e){
	    		LOG.warn(e.getMessage(), e);
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();	    		
	    	}
        }
        return result.createResponseEntity();
    }
	
	/**
	 * Update a data point in the system
	 * @param vo
	 * @param xid
	 * @param builder
	 * @param request
	 * @return
	 */
	@ApiOperation(
			value = "Update an existing data point",
			notes = "Content may be CSV or JSON"
			)
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<DataPointModel> updateDataPoint(@PathVariable String xid,
    		@RequestBody DataPointModel model, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){

			DataPointVO vo = model.getData();
	        DataPointVO existingDp = DataPointDao.instance.getByXid(xid);
	        if (existingDp == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        
	        //Check permissions
	    	try{
	    		if(!Permissions.hasDataPointReadPermission(user, vo)){
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	
	    		}
	    	}catch(PermissionException e){
	    		result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
        	}
	
	        vo.setId(existingDp.getId());
	        //Check the Template and see if we need to use it
	        if(model.getTemplateXid() != null){
            	
            	DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
            	if(template != null){
            		template.updateDataPointVO(vo);
            	}else{
            		result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", model.getTemplateXid())));
            	}
            }else{
                vo.setTextRenderer(new PlainRenderer()); //Could use None Renderer here
            }
	        
	        ProcessResult validation = new ProcessResult();
	        vo.validate(validation);
	        
	        if(validation.getHasMessages()){
	        	result.addRestMessage(model.addValidationMessages(validation));
	        	return result.createResponseEntity(model); 
	        }else{
	
	        	//We will always override the DS Info with the one from the XID Lookup
	            DataSourceVO<?> dsvo = DataSourceDao.instance.getDataSource(existingDp.getDataSourceXid());
	            
	            //TODO this implies that we may need to have a different JSON Converter for data points
	            //Need to set DataSourceId among other things
	            vo.setDataSourceId(existingDp.getDataSourceId());
	            
	            
	            if (dsvo == null){
	            	result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", xid));
	            	return result.createResponseEntity();
	            }else {
	                //Compare this point to the existing point in DB to ensure
	                // that we aren't moving a point to a different type of Data Source
	                DataPointDao dpDao = new DataPointDao();
	                DataPointVO oldPoint = dpDao.getDataPoint(vo.getId());
	                
	                //Does the old point have a different data source?
	                if(oldPoint != null&&(oldPoint.getDataSourceId() != dsvo.getId())){
	                    vo.setDataSourceId(dsvo.getId());
	                    vo.setDataSourceName(dsvo.getName());
	                }
	            }
	
	            Common.runtimeManager.saveDataPoint(vo);
	        }
	        
	        //Put a link to the updated data in the header?
	    	URI location = builder.path("/rest/v1/dataPoints/{xid}").buildAndExpand(xid).toUri();
	    	
	    	result.addRestMessage(getResourceUpdatedMessage(location));
	        return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }
	

	@ApiOperation(
			value = "Insert/Update multiple data points",
			notes = "CSV content must be limited to 1 type of data source."
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"})
    public ResponseEntity<List<DataPointModel>> saveDataPoints(
    		@RequestBody List<DataPointModel> models, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<List<DataPointModel>> result = new RestProcessResult<List<DataPointModel>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
        if(result.isOk()){
        	for(DataPointModel model : models){
    			DataPointVO vo = model.getData();
    	        DataPointVO existingDp = DataPointDao.instance.getByXid(vo.getXid());
    	        if (existingDp == null) {
    	    		result.addRestMessage(getDoesNotExistMessage());
    	    		return result.createResponseEntity();
    	        }
    	        
    	        //Check permissions
    	    	try{
    	    		if(!Permissions.hasDataPointReadPermission(user, vo)){
    	    			result.addRestMessage(getUnauthorizedMessage()); //TODO add what point
    	        		continue;
    	
    	    		}
    	    	}catch(PermissionException e){
    	    		result.addRestMessage(getUnauthorizedMessage()); //TODO add what point
            		continue;
            	}
    	
    	        vo.setId(existingDp.getId());
    	        //Check the Template and see if we need to use it
    	        if(model.getTemplateXid() != null){
                	
                	DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
                	if(template != null){
                		template.updateDataPointVO(vo);
                	}else{
                		result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", model.getTemplateXid())));
                		continue;
                	}
                }else{
                    vo.setTextRenderer(new PlainRenderer()); //Could use None Renderer here
                }
    	        
    	        ProcessResult validation = new ProcessResult();
    	        vo.validate(validation);
    	        
    	        if(validation.getHasMessages()){
    	        	result.addRestMessage(model.addValidationMessages(validation));
    	        }else{
    	
    	        	//We will always override the DS Info with the one from the XID Lookup
    	            DataSourceVO<?> dsvo = DataSourceDao.instance.getDataSource(existingDp.getDataSourceXid());
    	            
    	            //Need to set DataSourceId among other things
    	            vo.setDataSourceId(existingDp.getDataSourceId());
    	            if (dsvo == null){
    	            	result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", existingDp.getDataSourceXid()));
    	            }else {
    	                //Compare this point to the existing point in DB to ensure
    	                // that we aren't moving a point to a different type of Data Source
    	                DataPointDao dpDao = new DataPointDao();
    	                DataPointVO oldPoint = dpDao.getDataPoint(vo.getId());
    	                
    	                //Does the old point have a different data source?
    	                if(oldPoint != null&&(oldPoint.getDataSourceId() != dsvo.getId())){
    	                    vo.setDataSourceId(dsvo.getId());
    	                    vo.setDataSourceName(dsvo.getName());
    	                }
    	            }
    	
    	            Common.runtimeManager.saveDataPoint(vo);
    	        }

        	}
	        return result.createResponseEntity(models);
        }
        //Not logged in
        return result.createResponseEntity();
    }

	/**
	 * Delete one Data Point
	 * @param xid
	 * @param request
	 * @return
	 */
	@ApiOperation(
			value = "Delete a data point",
			notes = "The user must have permission to the data source"
			)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ResponseEntity<DataPointModel> delete(@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>();
		
		//TODO Fix up to use delete by XID?
		DataPointVO vo = DataPointDao.instance.getByXid(xid);
		if (vo == null) {
			result.addRestMessage(getDoesNotExistMessage());
    		return result.createResponseEntity();
    	}
		
		//Check permissions
        User user = Common.getUser(request);
    	try{
    		//TODO Is this the correct permission to check?
    		if(!Permissions.hasDataPointReadPermission(user, vo)){
    			result.addRestMessage(getUnauthorizedMessage());
    			return result.createResponseEntity();
    		}
    	}catch(PermissionException e){
			result.addRestMessage(getUnauthorizedMessage());
			return result.createResponseEntity();
    	}
		
		try{
			DataPointDao.instance.delete(vo.getId());
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
			return result.createResponseEntity();
		}
		
		//All good
		return result.createResponseEntity(new DataPointModel(vo));
    }
	
	@ApiOperation(
			value = "Get all data points for data source",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/dataSource/{xid}")
    public ResponseEntity<List<DataPointModel>> getDataPointsForDataSource(
    		@ApiParam(value = "Valid Data Source XID", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {

		RestProcessResult<List<DataPointModel>> result = new RestProcessResult<List<DataPointModel>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	
        	DataSourceVO<?> dataSource = DaoRegistry.dataSourceDao.getDataSource(xid);
        	if(dataSource == null){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
        	}
        	try{
        		if(!Permissions.hasDataSourcePermission(user, dataSource)){
        			LOG.warn("User: " + user.getUsername() + " tried to access data source with xid " + xid);
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	    		}
	    	}catch(PermissionException e){
	    		LOG.warn(e.getMessage(), e);
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();	    		
	    	}
        	
           	List<DataPointVO> dataPoints = DaoRegistry.dataPointDao.getDataPoints(dataSource.getId(), null);
            List<DataPointModel> userDataPoints = new ArrayList<DataPointModel>();
        	
	        for(DataPointVO vo : dataPoints){
	        	try{
	        		if(Permissions.hasDataPointReadPermission(user, vo)){
	        			userDataPoints.add(new DataPointModel(vo));
	        		}
	        	}catch(PermissionException e){
	        		//Munched
	        	}
	        }
	        result.addRestMessage(getSuccessMessage());
	        return result.createResponseEntity(userDataPoints);
        }
        
        return result.createResponseEntity();
	}
}
