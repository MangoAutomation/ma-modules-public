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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 * 
 */
@Api(value="Data Sources", description="Data Sources")
@RestController
@RequestMapping("/v1/data-sources")
public class DataSourceRestController extends MangoRestController{

	public DataSourceRestController(){
		LOG.info("Creating DS Rest Controller");
	}
	private static Log LOG = LogFactory.getLog(DataSourceRestController.class);
	
	@ApiOperation(
			value = "Get all data sources",
			notes = "Only returns data sources available to logged in user"
			)
    @RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<AbstractDataSourceModel<?>>> getAllDataSources(HttpServletRequest request) {
    	
    	RestProcessResult<List<AbstractDataSourceModel<?>>> result = new RestProcessResult<List<AbstractDataSourceModel<?>>>(HttpStatus.OK);
    	
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
	        List<DataSourceVO<?>> dataSources = DaoRegistry.dataSourceDao.getAll();
	        List<AbstractDataSourceModel<?>> models = new ArrayList<AbstractDataSourceModel<?>>();
	        for(DataSourceVO<?> ds : dataSources){
	        	try{
	        		if(Permissions.hasDataSourcePermission(user, ds))
	        			models.add(ds.asModel());
	        	}catch(PermissionException e){
	        		//Munch Munch
	        	}
	        	
	        }
	        return result.createResponseEntity(models);
    	}
    	return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Get data source by xid",
			notes = "Only returns data sources available to logged in user"
			)
	@RequestMapping(method = RequestMethod.GET, value = "/{xid}", produces={"application/json"})
    public ResponseEntity<AbstractDataSourceModel<?>> getDataSource(HttpServletRequest request, @PathVariable String xid) {
		
		RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
    	if(result.isOk()){
            DataSourceVO<?> vo = DaoRegistry.dataSourceDao.getByXid(xid);

            if (vo == null) {
                return new ResponseEntity<AbstractDataSourceModel<?>>(HttpStatus.NOT_FOUND);
            }else{
            	try{
	        		if(Permissions.hasDataSourcePermission(user, vo))
	        			return result.createResponseEntity(vo.asModel());
	        		else{
	    	    		result.addRestMessage(getUnauthorizedMessage());
	            		return result.createResponseEntity();
	        		}
	        	}catch(PermissionException e){
	        		LOG.warn(e.getMessage(), e);
		    		result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	        	}
            }
    	}
        return result.createResponseEntity();
    }
	
	
	
	/**
	 * Put a data source into the system
	 * @param xid
	 * @param model
     * @param builder
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Update data source")
	@RequestMapping(method = RequestMethod.PUT, value = "/{xid}", produces={"application/json"})
    public ResponseEntity<AbstractDataSourceModel<?>> updateDataSource(
    		@PathVariable String xid,
    		@RequestBody AbstractDataSourceModel<?> model, 
    		UriComponentsBuilder builder, 
    		HttpServletRequest request) {

		RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
			DataSourceVO<?> vo = model.getData();
			
	        DataSourceVO<?> existing = DaoRegistry.dataSourceDao.getByXid(xid);
	        if (existing == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        
	        //Check permissions
	    	try{
	    		if(!Permissions.hasDataSourcePermission(user, existing.getId())){
	    			result.addRestMessage(getUnauthorizedMessage());
	        		return result.createResponseEntity();
	    		}
	    	}catch(PermissionException e){
	    		LOG.warn(e.getMessage(), e);
	    		result.addRestMessage(getUnauthorizedMessage());
        		return result.createResponseEntity();
        	}
	
	        vo.setId(existing.getId());
	        
	        ProcessResult validation = new ProcessResult();
	        vo.validate(validation);
	        
	        if(!model.validate()){
	        	result.addRestMessage(this.getValidationFailedError());
	        	return result.createResponseEntity(model); 
	        }else{
	            Common.runtimeManager.saveDataSource(vo);
	        }
	        
	        //Put a link to the updated data in the header?
	    	URI location = builder.path("/v1/data-sources/{xid}").buildAndExpand(xid).toUri();
	    	result.addRestMessage(getResourceUpdatedMessage(location));
	        return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }

	@ApiOperation(value = "Save data source")
	@RequestMapping(
			method = {RequestMethod.POST},
			produces = {"application/json"}
	)
	public ResponseEntity<AbstractDataSourceModel<?>> saveDataSource(@RequestBody AbstractDataSourceModel<?> model, UriComponentsBuilder builder, HttpServletRequest request) {
		RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			DataSourceVO<?> vo = model.getData();
			DataSourceVO<?> existing = (DataSourceVO<?>)DaoRegistry.dataSourceDao.getByXid(model.getXid());
			if(existing != null) {
				result.addRestMessage(this.getAlreadyExistsMessage());
				return result.createResponseEntity();
			} else {
				try {
					if(!Permissions.hasDataSourcePermission(user)) {
						result.addRestMessage(this.getUnauthorizedMessage());
						return result.createResponseEntity();
					}
				} catch (PermissionException pe) {
					LOG.warn(pe.getMessage(), pe);
					result.addRestMessage(this.getUnauthorizedMessage());
					return result.createResponseEntity();
				}

				ProcessResult validation = new ProcessResult();
				vo.validate(validation);
				if(!model.validate()) {
					result.addRestMessage(this.getValidationFailedError());
					return result.createResponseEntity(model);
				}
				else {
					Common.runtimeManager.saveDataSource(vo);
					DataSourceVO<?> created = (DataSourceVO<?>)DaoRegistry.dataSourceDao.getByXid(model.getXid());
					URI location = builder.path("/v1/data-sources/{xid}").buildAndExpand(new Object[]{created.asModel().getXid()}).toUri();
					result.addRestMessage(this.getResourceCreatedMessage(location));
					return result.createResponseEntity(created.asModel());
				}
			}
		} else {
			return result.createResponseEntity();
		}
	}


	@ApiOperation(value = "Delete data source")
	@RequestMapping(
			method = {RequestMethod.DELETE},
			value = {"/{xid}"},
			produces = {"application/json"}
	)
	public ResponseEntity<AbstractDataSourceModel<?>> deleteDataSource(@PathVariable String xid, UriComponentsBuilder builder, HttpServletRequest request) {
		RestProcessResult<AbstractDataSourceModel<?>> result = new RestProcessResult<AbstractDataSourceModel<?>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			DataSourceVO<?> existing = (DataSourceVO<?>)DaoRegistry.dataSourceDao.getByXid(xid);
			if(existing == null) {
				result.addRestMessage(this.getDoesNotExistMessage());
				return result.createResponseEntity();
			} else {
				try {
					if(!Permissions.hasDataSourcePermission(user, existing.getId())) {
						result.addRestMessage(this.getUnauthorizedMessage());
						return result.createResponseEntity();
					}
				} catch (PermissionException pe) {
					LOG.warn(pe.getMessage(), pe);
					result.addRestMessage(this.getUnauthorizedMessage());
					return result.createResponseEntity();
				}

				Common.runtimeManager.deleteDataSource(existing.getId());
				return result.createResponseEntity(existing.asModel());
			}
		}
		return result.createResponseEntity();
	}

}
