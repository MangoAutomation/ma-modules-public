/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.maint.work.EmailWorkItem;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
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
	
	@ApiOperation(value = "Send a test email")
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"}, value = "/email/test")
    public ResponseEntity<String> sendTestEmail(
    		@RequestParam(value = "email", required = true, defaultValue = "") String email,
    		@RequestParam(value = "username", required = true, defaultValue = "") String username,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<String> result = new RestProcessResult<String>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		if(Permissions.hasAdmin(user)){
    			try{
		            Translations translations = Common.getTranslations();
		            Map<String, Object> model = new HashMap<>();
		            model.put("message", new TranslatableMessage("ftl.userTestEmail", username));
		            MangoEmailContent cnt = new MangoEmailContent("testEmail", model, translations,
		                    translations.translate("ftl.testEmail"), Common.UTF8);
		            EmailWorkItem.queueEmail(email, cnt);
		            return result.createResponseEntity(new TranslatableMessage("common.testEmailSent", email).translate(Common.getTranslations()));
    			}catch(Exception e){
    				result.addRestMessage(this.getInternalServerErrorMessage(e.getMessage()));
    			}
    		}else{
    			LOG.warn("Non admin user: " + user.getUsername() + " attempted to send a test email.");
    			result.addRestMessage(this.getUnauthorizedMessage());
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	

}
