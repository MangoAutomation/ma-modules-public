/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneUtility;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @author Terry Packer
 *
 */
@Api(value="Users", description="Operations on Server")
@RestController
@RequestMapping("/v1/server")
public class ServerRestController extends MangoRestController{
	
	private static Log LOG = LogFactory.getLog(ServerRestController.class);
	
	private List<TimezoneModel> allTimezones;
	private TimezoneModel defaultServerTimezone;
	
	public ServerRestController(){
		this.allTimezones  = TimezoneUtility.getTimeZoneIdsWithOffset();
		this.defaultServerTimezone = new TimezoneModel("", new TranslatableMessage("users.timezone.def").translate(Common.getTranslations()), 0);
		//Always add the default to the start of the list
		this.allTimezones.add(0, this.defaultServerTimezone);
	}

	@ApiOperation(
			value = "Query Timezones",
			notes = "",
			response=TimezoneModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=TimezoneModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="timezones")
    public ResponseEntity<PageQueryResultModel<TimezoneModel>> queryTimezone(HttpServletRequest request) {
		
		RestProcessResult<PageQueryResultModel<TimezoneModel>> result = new RestProcessResult<PageQueryResultModel<TimezoneModel>>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			//Parse the RQL Query
	    		ASTNode root = this.parseRQLtoAST(request);
	    		
	    		List<TimezoneModel> list = root.accept(new RQLToObjectListQuery<TimezoneModel>(), allTimezones);
	    		
	    		PageQueryResultModel<TimezoneModel> model = new PageQueryResultModel<TimezoneModel>(list, allTimezones.size() + 1);
	    		return result.createResponseEntity(model);
    		}catch(UnsupportedEncodingException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}

}
