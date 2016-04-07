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

/**
 * @author Terry Packer
 * 
 */
@Api(value="Data Points", description="Data points")
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
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/list")
    public ResponseEntity<List<DataPointModel>> getAllDataPoints(HttpServletRequest request, 
    		@ApiParam(value = "Limit the number of results", required=false)
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
			value = "Get data point by XID",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<DataPointModel> getDataPoint(
    		@ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
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
			value = "Get data point by ID",
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
    public ResponseEntity<DataPointModel> updateDataPoint(
    		@PathVariable String xid,
    		@ApiParam(value = "Updated data point model", required = true)
    		@RequestBody DataPointModel model, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	boolean contentTypeCsv = false;
        	if(request.getContentType().toLowerCase().contains("text/csv"))
        		contentTypeCsv = true;
        	
			DataPointVO vo = model.getData();
	        DataPointVO existingDp = DataPointDao.instance.getByXid(xid);
	        if (existingDp == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        
	        //Check permissions
	    	try{
	    		if(!Permissions.hasDataSourcePermission(user, vo.getDataSourceId())){
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	
	    		}
	    	}catch(PermissionException e){
	    		result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
        	}
	
	        vo.setId(existingDp.getId());
    		//Set all properties that are not in the template or the spreadsheet
    		//TODO probably move these into one or the other
	        DataPointDao.instance.setEventDetectors(vo); //Use ID to get detectors
    		vo.setPointFolderId(existingDp.getPointFolderId());
    		
	        //Check the Template and see if we need to use it
	        if(model.getTemplateXid() != null){
            	
            	DataPointPropertiesTemplateVO template = (DataPointPropertiesTemplateVO) TemplateDao.instance.getByXid(model.getTemplateXid());
            	if(template != null){
            		template.updateDataPointVO(vo);
            		template.updateDataPointVO(model.getData());
            	}else{
            		model.addValidationMessage("validate.invalidReference", RestMessageLevel.ERROR, "templateXid");
            		result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.dataPoint.badReference", model.getTemplateXid())));
            	}
            }else{
        		if(contentTypeCsv){
            		model.addValidationMessage("validate.required", RestMessageLevel.ERROR, "templateXid");
            		result.addRestMessage(this.getValidationFailedError());
            		return result.createResponseEntity(model);
        		}
                vo.setTextRenderer(new PlainRenderer()); //Could use None Renderer here
                if(vo.getChartColour() == null)
                	vo.setChartColour(""); //Can happen when CSV comes in without template       
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
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json;charset=UTF-8", "text/csv;charset=UTF-8"}, produces={"application/json"})
    public ResponseEntity<List<DataPointModel>> saveDataPoints(
    		@ApiParam(value = "List of updated data point models", required = true)
    		@RequestBody List<DataPointModel> models, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<List<DataPointModel>> result = new RestProcessResult<List<DataPointModel>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
        if(result.isOk()){
        	boolean contentTypeCsv = false;
        	if(request.getContentType().toLowerCase().contains("text/csv"))
        		contentTypeCsv = true;
        	
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
            	//If we don't have a reference data source we need to set one
            	if(ds == null){
            		ds = myDataSource;
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
    	        	vo.setId(existingDp.getId());  //Must Do this as ID is NOT in the model
            		//Set all properties that are not in the template or the spreadsheet
            		//TODO probably move these into one or the other
            		vo.setPointFolderId(existingDp.getPointFolderId());
    	        	DataPointDao.instance.setEventDetectors(vo); //Use ID to get detectors
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
                		template.updateDataPointVO(model.getData());
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

                	}else{
                		if(contentTypeCsv){
                    		model.addValidationMessage("validate.required", RestMessageLevel.ERROR, "templateXid");
                    		result.addRestMessage(this.getValidationFailedError());
                    		continue;
                		}
                		vo.setTextRenderer(new PlainRenderer()); //Could use None Renderer here
                		if(vo.getChartColour() == null)
                        	vo.setChartColour(""); //Can happen when CSV comes in without template
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
	@RequestMapping(method = RequestMethod.DELETE, value = "/{xid}", produces={"application/json", "text/csv"})
    public ResponseEntity<DataPointModel> delete(@PathVariable String xid, UriComponentsBuilder builder, HttpServletRequest request) {
		RestProcessResult<DataPointModel> result = new RestProcessResult<DataPointModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			DataPointVO existing = DaoRegistry.dataPointDao.getByXid(xid);
			if(existing == null) {
				result.addRestMessage(this.getDoesNotExistMessage());
				return result.createResponseEntity();
			}
			else {
				try {
					//Ensure we have permission to edit the data source
					if(!Permissions.hasDataSourcePermission(user, existing.getDataSourceId())) {
						result.addRestMessage(this.getUnauthorizedMessage());
						return result.createResponseEntity();
					}
				}
				catch (PermissionException e) {
					LOG.warn(e.getMessage(), e);
					result.addRestMessage(this.getUnauthorizedMessage());
					return result.createResponseEntity();
				}

				Common.runtimeManager.deleteDataPoint(existing);
				return result.createResponseEntity(new DataPointModel(existing));
			}
		}
		else {
			return result.createResponseEntity();
		}
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
			notes = "Use RQL formatted query",
			response=DataPointModel.class,
			responseContainer="List"
			)
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
