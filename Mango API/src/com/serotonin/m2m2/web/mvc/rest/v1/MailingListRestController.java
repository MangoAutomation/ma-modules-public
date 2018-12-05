/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.service.MailingListService;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.mailingList.MailingList;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.email.MailingListModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Mailing Lists", description="Mailing Lists")
@RestController
@RequestMapping("/mailing-lists")
public class MailingListRestController extends MangoRestController{
	
	private final MailingListDao dao;
	private final MailingListService service;
	
	@Autowired
	public MailingListRestController(MailingListDao dao, MailingListService service){
		this.dao = dao;
		this.service = service;
	}

	
	@ApiOperation(value = "Get Mailing List by XID", notes = "Returns a Mailing List")
	@RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<MailingListModel> get(
    		@ApiParam(value = "Valid mailing list xid", required = true, allowMultiple = false)
    		@PathVariable String xid, HttpServletRequest request) {
		
		RestProcessResult<MailingListModel> result = new RestProcessResult<MailingListModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		MailingList list = this.dao.getFullByXid(xid);
    		
    		if(list != null){
    		    service.ensureReadPermission(user, list);
    			MailingListModel model = new MailingListModel(list);
                return result.createResponseEntity(model);
    		}else{
    			result.addRestMessage(getDoesNotExistMessage());
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Get Mailing List", notes = "Returns all Mailing Lists, eventually will be RQL endpoint")
	@RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MailingListModel>> getAll(HttpServletRequest request) {
		
		RestProcessResult<List<MailingListModel>> result = new RestProcessResult<List<MailingListModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		List<MailingList> lists = this.dao.getAllFull();
    		if(lists != null){
    			List<MailingListModel> models = new ArrayList<MailingListModel>();
    			for(MailingList list : lists){
                    if(service.hasReadPermission(user, list))
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
