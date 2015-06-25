/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

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

import com.infiniteautomation.mango.db.query.RQLToSQLParseException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.view.text.PlainRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.template.DataPointPropertiesTemplateVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointStreamCallback;
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
@RequestMapping("/v1/data-points")
public class DataPointRestController extends MangoVoRestController<DataPointVO, DataPointModel, DataPointDao>{

	private static Log LOG = LogFactory.getLog(DataPointRestController.class);
	
	public DataPointRestController(){
		super(DaoRegistry.dataPointDao);
		LOG.info("Creating Data Point Rest Controller.");
		
		//Fill in any model mappings
		//TODO this.modelMap.put("", "");
		
		
	}

	
	@ApiOperation(
			value = "Get all data points",
			notes = "Only returns points available to logged in user"
			)
	@ApiResponses(value = { 
	@ApiResponse(code = 200, message = "Ok"),
	@ApiResponse(code = 403, message = "User does not have access")
	})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/list")
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
	
	
	@ApiOperation(
			value = "Get existing data point",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/by-id/{id}")
    public ResponseEntity<DataPointModel> getDataPointById(
    		@ApiParam(value = "Valid Data Point ID", required = true, allowMultiple = false)
    		@PathVariable int id, HttpServletRequest request) {

		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
	        DataPointVO vo = DataPointDao.instance.get(id);
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
	        
	        if(!model.validate()){
	        	result.addRestMessage(this.getValidationFailedError());
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
	    	URI location = builder.path("/v1/data-points/{xid}").buildAndExpand(xid).toUri();
	    	
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
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json;charset=UTF-8", "text/csv;charset=UTF-8"})
    public ResponseEntity<List<DataPointModel>> saveDataPoints(
    		@RequestBody List<DataPointModel> models, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<List<DataPointModel>> result = new RestProcessResult<List<DataPointModel>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
        if(result.isOk()){
        	DataPointModel first;
        	DataSourceVO<?> ds = null;
        	if(models.size() > 0){
        		first = models.get(0);
        		ds = DaoRegistry.dataSourceDao.getByXid(first.getDataSourceXid());
        	}
        	
        	for(DataPointModel model : models){
    			DataPointVO vo = model.getData();
    			DataSourceVO<?> myDataSource = DaoRegistry.dataSourceDao.getByXid(vo.getDataSourceXid());
    			if(myDataSource == null){
    				model.addValidationMessage("validate.invalidReference", RestMessageLevel.ERROR, "dataSourceXid");
    				continue;
    			}
    			//First check to see that the data source types match
    			if(!ds.getDefinition().getDataSourceTypeName().equals(myDataSource.getDefinition().getDataSourceTypeName())){
    				model.addValidationMessage("validate.incompatibleDataSourceType", RestMessageLevel.ERROR, "dataSourceXid");
    				continue;
    			}
    			//Set the ID for the data source 
    			vo.setDataSourceId(myDataSource.getId());
    			
    			//Are we a new one?
    	        DataPointVO existingDp = DataPointDao.instance.getByXid(vo.getXid());
    	        boolean updated = true;
    	        if (existingDp == null) {
    	    		updated = false;
    	        }else{
    	        	DataPointDao.instance.setEventDetectors(existingDp);
    	        	vo.setId(existingDp.getId());  //Must Do this as ID is NOT in the model
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
  
    	        //Check the Template and see if we need to use it
    	        if(model.getTemplateXid() != null){
                	DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
                	if(template != null){
                		template.updateDataPointVO(vo);
                	}else{
                		model.addValidationMessage("validate.invalidReference", RestMessageLevel.ERROR, "templateXid");
                		result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", model.getTemplateXid())));
                		continue;
                	}
                }else{
                	//We need to update the various pieces
                	if(updated){
                		DataPointPropertiesTemplateVO tempTemplate = new DataPointPropertiesTemplateVO();
                		tempTemplate.updateTemplate(existingDp);
                		tempTemplate.updateDataPointVO(vo);

                		//Kludge to allow this template to not be our real template
                		vo.setTemplateId(null);
                		
                		//Set all properties that are not in the template or the spreadsheet
                		//TODO probably move these into one or the other
                		vo.setPointFolderId(existingDp.getPointFolderId());

                		vo.setEventDetectors(existingDp.getEventDetectors());
                	}else{
                		vo.setTextRenderer(new PlainRenderer()); //Could use None Renderer here
                	}
                    
                }
    	        
    	        if(model.validate()){
    	        	if(updated)
    	        		model.addValidationMessage("common.updated", RestMessageLevel.INFORMATION, "all");
    	        	else
    	        		model.addValidationMessage("common.saved", RestMessageLevel.INFORMATION, "all");
    	        	//Save it
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
			notes = "The user must have permission to the data point"
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
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/data-source/{xid}")
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
	
	@ApiOperation(
			value = "Query Data Points",
			notes = "",
			response=DataPointModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=DataPointModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/query")
    public ResponseEntity<QueryDataPageStream<DataPointVO>> query(
    		
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) ASTNode root, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<DataPointVO>> result = new RestProcessResult<QueryDataPageStream<DataPointVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		DataPointStreamCallback callback = new DataPointStreamCallback(this, user);
    		return result.createResponseEntity(getPageStream(root, callback));
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query Data Points",
			notes = "",
			response=DataPointModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=DataPointModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<QueryDataPageStream<DataPointVO>> queryRQL(
    		   		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<DataPointVO>> result = new RestProcessResult<QueryDataPageStream<DataPointVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			ASTNode node = this.parseRQLtoAST(request);
    			DataPointStreamCallback callback = new DataPointStreamCallback(this, user);
    			return result.createResponseEntity(getPageStream(node, callback));
    			
    			
	    		//QueryModel query = this.parseRQL(request);
	    		//DataPointStreamCallback callback = new DataPointStreamCallback(this, user);
	    		//return result.createResponseEntity(getPageStream(query, callback));
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}

	@ApiOperation(
			value = "Bulk Update Set Permissions",
			notes = "",
			response=Long.class
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/bulk-apply-set-permissions")
    public ResponseEntity<Long> bulkApplySetPermissions(
    		
    		@ApiParam(value="Permissions", required=true)
    		@RequestBody(required=true) String permissions, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(!user.isAdmin()){
    			LOG.warn("User " + user.getUsername() + " attempted to set bulk permissions");
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
    		}
    	
    		try{
    			ASTNode node = this.parseRQLtoAST(request);
    			
    			long changed = this.dao.bulkUpdatePermissions(node, permissions, true);
    			return result.createResponseEntity(changed);
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Bulk Update Read Permissions",
			notes = "",
			response=Long.class
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/bulk-apply-read-permissions")
    public ResponseEntity<Long> bulkApplyReadPermissions(
    		
    		@ApiParam(value="Permissions", required=true)
    		@RequestBody(required=true) String permissions, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(!user.isAdmin()){
    			LOG.warn("User " + user.getUsername() + " attempted to set bulk permissions");
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
    		}
    	
    		try{
    			ASTNode node = this.parseRQLtoAST(request);
    			
    			long changed = this.dao.bulkUpdatePermissions(node, permissions, false);
    			return result.createResponseEntity(changed);
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Bulk Clear Set Permissions",
			notes = "",
			response=Long.class
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/bulk-clear-set-permissions")
    public ResponseEntity<Long> bulkClearSetPermissions(HttpServletRequest request) {
		
		RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(!user.isAdmin()){
    			LOG.warn("User " + user.getUsername() + " attempted to clear bulk permissions");
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
    		}
    	
    		try{
    			ASTNode node = this.parseRQLtoAST(request);
    			
    			long changed = this.dao.bulkClearPermissions(node, true);
    			return result.createResponseEntity(changed);
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Bulk Clear Read Permissions",
			notes = "",
			response=Long.class
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=String.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/bulk-clear-read-permissions")
    public ResponseEntity<Long> bulkClearReadPermissions(HttpServletRequest request) {
		
		RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(!user.isAdmin()){
    			LOG.warn("User " + user.getUsername() + " attempted to clear bulk permissions");
    			result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
    		}
    	
    		try{
    			ASTNode node = this.parseRQLtoAST(request);
    			
    			long changed = this.dao.bulkClearPermissions(node, false);
    			return result.createResponseEntity(changed);
    		}catch(UnsupportedEncodingException | RQLToSQLParseException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	public DataPointModel createModel(DataPointVO vo) {
		return new DataPointModel(vo);
	}
	
}
