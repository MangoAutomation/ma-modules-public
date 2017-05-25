/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemInfoDefinition;
import com.serotonin.m2m2.rt.maint.work.EmailWorkItem;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryResultModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.system.TimezoneUtility;
import com.serotonin.m2m2.web.mvc.spring.security.MangoSecurityConfiguration;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

/**
 * Class to provide server information
 * 
 * @author Terry Packer
 */
@Api(value="Server Information v2", description="Server Information")
@RestController
@RequestMapping("/v2/server")
public class ServerRestV2Controller extends AbstractMangoRestV2Controller{

	private List<TimezoneModel> allTimezones;
	private TimezoneModel defaultServerTimezone;
	
	public ServerRestV2Controller(){
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
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="/timezones")
    public ResponseEntity<PageQueryResultModel<TimezoneModel>> queryTimezone(HttpServletRequest request) {
		ASTNode root = this.parseRQLtoAST(request);
		List<TimezoneModel> list = root.accept(new RQLToObjectListQuery<TimezoneModel>(), allTimezones);
		PageQueryResultModel<TimezoneModel> model = new PageQueryResultModel<TimezoneModel>(list, allTimezones.size() + 1);
		return new ResponseEntity<PageQueryResultModel<TimezoneModel>>(model, HttpStatus.OK);
	}

	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Send a test email", notes="Sends email to supplied address")
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"}, value = "/email/test")
    public ResponseEntity<String> sendTestEmail(
    		@RequestParam(value = "email", required = true, defaultValue = "") String email,
    		@RequestParam(value = "username", required = true, defaultValue = "") String username,
    		HttpServletRequest request){

		try{
	        Translations translations = Common.getTranslations();
	        Map<String, Object> model = new HashMap<>();
	        model.put("message", new TranslatableMessage("ftl.userTestEmail", username));
	        MangoEmailContent cnt = new MangoEmailContent("testEmail", model, translations,
	                translations.translate("ftl.testEmail"), Common.UTF8);
	        EmailWorkItem.queueEmail(email, cnt);
	        return new ResponseEntity<String>(new TranslatableMessage("common.testEmailSent", email).translate(Common.getTranslations()), HttpStatus.OK);
		}catch(Exception e){
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Restart Mango", notes="Returns location url in header for status updates while web interface is still active")
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json"}, produces={"application/json"}, value = "/restart")
    public ResponseEntity<Void> restart(UriComponentsBuilder builder, HttpServletRequest request) {
		ProcessResult r = ModulesDwr.scheduleShutdown();
		if(r.getData().get("shutdownUri") != null){
            URI location = builder.path("/status/mango").buildAndExpand().toUri();
	    	return getResourceCreated(null, location.toString());
        }
		else
        	throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("modules.restartAlreadyScheduled"));
        
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "List session information for all sessions", notes = "Admin only")
	@RequestMapping(method = RequestMethod.GET,  value="/http-sessions", produces={"application/json"})
	public ResponseEntity<List<SessionInformation>> listSessions(
            @AuthenticationPrincipal User user,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<SessionInformation> sessions = new ArrayList<SessionInformation>();
    	final List<Object> allPrincipals = MangoSecurityConfiguration.sessionRegistry().getAllPrincipals();
    	
        for (final Object principal : allPrincipals) {
        	List<SessionInformation> sessionInfo = MangoSecurityConfiguration.sessionRegistry().getAllSessions(principal, true);
    		//Expire sessions, the user was deleted
    		for(SessionInformation info : sessionInfo){
    			sessions.add(info);
    		}
        }
        return new ResponseEntity<>(sessions, HttpStatus.OK);
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Get all available system information", notes = "")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
	})
	@RequestMapping( method = {RequestMethod.GET}, produces = {"application/json"}, value="system-info" )
	public ResponseEntity<Map<String, Object>> getSystemInfo(@AuthenticationPrincipal User user) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(SystemInfoDefinition<?> def : ModuleRegistry.getSystemInfoDefinitions().values())
			map.put(def.getKey(), def.getValue());
		return new ResponseEntity<Map<String,Object>>(map, HttpStatus.OK);
	}

	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Get one piece of system info by key", notes = "")
	@ApiResponses({
		@ApiResponse(code = 500, message = "Internal error", response=ResponseEntity.class),
		@ApiResponse(code = 404, message = "Not Found", response=ResponseEntity.class),
	})
	@RequestMapping( method = {RequestMethod.GET}, value="/system-info/{key}", produces = {"application/json"} )
	public ResponseEntity<Object> getOne(@AuthenticationPrincipal User user,
			@ApiParam(value = "Valid System Info Key", required = true, allowMultiple = false)
			@PathVariable String key) {
		
		SystemInfoDefinition<?> setting = ModuleRegistry.getSystemInfoDefinition(key);
		if(setting != null)
			return new ResponseEntity<Object>(setting.getValue(), HttpStatus.OK);
		throw new NotFoundRestException();
	}
	
}
