/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.RealTimeDataPointValue;
import com.serotonin.m2m2.rt.dataImage.RealTimeDataPointValueCache;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.RealTimeModel;
import com.wordnik.swagger.annotations.Api;

/**
 * 
 * Real time data controller returns RealTimeDataPointValues
 * 
 * 
 * 
 * @author Terry Packer
 * 
 */
@Api(value="Realtime Data", 
	description="Operations on Real time data for active points in the point hierarchy. Note that recently enabled points will not be available until the point hierarchy is saved.",
	position=5)
@RestController
@RequestMapping("/v1/realtime")
public class RealTimeDataRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(RealTimeDataRestController.class);
	
	
	/**
	 * Query the User's Real Time Data
	 * @param request
	 * @param limit
	 * @return
	 */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<RealTimeModel>> query(HttpServletRequest request) {
    	
    	RestProcessResult<List<RealTimeModel>> result = new RestProcessResult<List<RealTimeModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	
    	if(result.isOk()){
    		ASTNode model;
			try{
				model = this.parseRQLtoAST(request);
		    	List<RealTimeDataPointValue> values = RealTimeDataPointValueCache.instance.getUserView(user.getPermissions());
		    	values = model.accept(new RQLToObjectListQuery<RealTimeDataPointValue>(), values);
		    	List<RealTimeModel> models = new ArrayList<RealTimeModel>();
		    	for(RealTimeDataPointValue value : values){
		    		models.add(new RealTimeModel(value));
		    	}
		    	return result.createResponseEntity(models);
			} catch (Exception e) {
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
			}
    	}
    	
    	return result.createResponseEntity();
    	
    }
	
	/**
	 * Get all of the Users Real Time Data
	 * @param request
	 * @param limit
	 * @return
	 */
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public ResponseEntity<List<RealTimeModel>> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")int limit) {
    	
    	RestProcessResult<List<RealTimeModel>> result = new RestProcessResult<List<RealTimeModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	
    	if(result.isOk()){
	    	List<RealTimeDataPointValue> values = RealTimeDataPointValueCache.instance.getUserView(user.getPermissions());
	    	ASTNode root = new ASTNode("limit", limit);
	    	values = root.accept(new RQLToObjectListQuery<RealTimeDataPointValue>(), values);
	    	List<RealTimeModel> models = new ArrayList<RealTimeModel>();
	    	//TODO Fix up Visitor to be able to create models on the fly?
	    	for(RealTimeDataPointValue value : values){
	    		models.add(new RealTimeModel(value));
	    	}
	    	return result.createResponseEntity(models);
    	}
    	
    	return result.createResponseEntity();
    	
    }
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-xid/{xid}")
    public ResponseEntity<RealTimeModel> get(@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<RealTimeModel> result = new RestProcessResult<RealTimeModel>(HttpStatus.OK);
		User user = checkUser(request, result); //Check the user
		
		//If no messages then go for it
		if(result.isOk()){
	    	List<RealTimeDataPointValue> values = RealTimeDataPointValueCache.instance.getUserView(user.getPermissions());
	    	ASTNode root = new ASTNode("xid", xid);

	    	values = root.accept(new RQLToObjectListQuery<RealTimeDataPointValue>(), values);
	    	
	        if (values.size() == 0) {
	        	LOG.debug("Attempted access of Real time point that is not enabled or DNE.");
	        	result.addRestMessage(HttpStatus.NOT_FOUND, new TranslatableMessage("common.default", "Point doesn't exist or is not enabled."));
	            return result.createResponseEntity();
	        }
	        RealTimeModel model = new RealTimeModel(values.get(0));
	        return result.createResponseEntity(model);
	        
		}else{
			return result.createResponseEntity();
		}
    }
	
}
