/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.email.MailingListModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Mailing List Access", description="Operations on Mailing Lists")
@RestController
@RequestMapping("/v1/mailing-lists")
public class MailingListRestController extends MangoRestController{
	
	private MailingListDao dao;
	
	public MailingListRestController(){
		this.dao = new MailingListDao();
	}

	
	@ApiOperation(value = "Get Mailing List", notes = "Returns a Mailing List")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<MailingListModel> get(
    		@ApiParam(value = "Valid mailing list xid", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<MailingListModel> result = new RestProcessResult<MailingListModel>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		
    		MailingList list = this.dao.getMailingList(xid);
    		
    		if(list != null){
    			MailingListModel model = new MailingListModel(list);
                return result.createResponseEntity(model);
    		}else{
    			result.addRestMessage(getDoesNotExistMessage());
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get Mailing List", notes = "Returns all Mailing Lists, eventually will be RQL endpoint")
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"})
    public ResponseEntity<List<MailingListModel>> getAll(HttpServletRequest request) {
		
		RestProcessResult<List<MailingListModel>> result = new RestProcessResult<List<MailingListModel>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		
    		List<MailingList> lists = this.dao.getMailingLists();
    		
    		if(lists != null){
    			List<MailingListModel> models = new ArrayList<MailingListModel>();
    			for(MailingList list : lists){
    				models.add(new MailingListModel(list));
    			}
                return result.createResponseEntity(models);
    		}else{
    			result.addRestMessage(getDoesNotExistMessage());
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
}
