/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonString;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.AngularJSModuleDefinitionGroupModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleUpgradesModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * 
 * @author Terry Packer
 */
@Api(value="Module Definitions", description="Access Module Definitions")
@RestController
@RequestMapping("/v1/modules")
public class ModulesRestController extends MangoRestController{
	
	private final String WEB = "/web";

	@ApiOperation(value = "AngularJS Modules", notes = "Publicly Available Angular JS Modules")
    @RequestMapping(method = RequestMethod.GET, value = "/angularjs-modules/public",  produces={"application/json"})
    public ResponseEntity<AngularJSModuleDefinitionGroupModel> getPublicAngularJSModules(HttpServletRequest request) {
		
		RestProcessResult<AngularJSModuleDefinitionGroupModel> result = new RestProcessResult<AngularJSModuleDefinitionGroupModel>(HttpStatus.OK);
		
		List<AngularJSModuleDefinition> definitions = ModuleRegistry.getAngularJSDefinitions();
		List<String> urls = new ArrayList<String>();
		for(AngularJSModuleDefinition def : definitions)
			urls.add(def.getModule().getWebPath() + WEB + def.getJavaScriptFilename());
		
		AngularJSModuleDefinitionGroupModel model = new AngularJSModuleDefinitionGroupModel();
		model.setUrls(urls);
		
		return result.createResponseEntity(model);
    }
	
	@ApiOperation(value = "Get Available Upgrades", notes = "Check the store for Upgrades")
    @RequestMapping(method = RequestMethod.GET, value = "/upgrades-available",  produces={"application/json"})
    public ResponseEntity<ModuleUpgradesModel> getUpgrades(HttpServletRequest request) {
		
		RestProcessResult<ModuleUpgradesModel> result = new RestProcessResult<ModuleUpgradesModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if(result.isOk()){
    		if(Permissions.hasAdmin(user)){
    			//Do the check
    			try{
    				JsonValue jsonResponse = ModulesDwr.getAvailableUpgrades();
    				if (jsonResponse instanceof JsonString){
    					result.addRestMessage(getInternalServerErrorMessage(jsonResponse.toString()));
    				}else {
    	    			List<ModuleModel> upgrades = new ArrayList<ModuleModel>(); 
    	    			List<ModuleModel> newInstalls = new ArrayList<ModuleModel>();
    	    			ModuleUpgradesModel model = new ModuleUpgradesModel(upgrades, newInstalls);

    	                JsonObject root = jsonResponse.toJsonObject();
    	                JsonValue jsonUpgrades = root.get("upgrades");
    	                JsonArray jsonUpgradesArray = jsonUpgrades.toJsonArray();
    	                for(JsonValue v : jsonUpgradesArray){
    	                	upgrades.add(new ModuleModel(v.getJsonValue("name").toString(), v.getJsonValue("version").toString()));
    	                }
    	                JsonValue jsonInstalls = root.get("newInstalls");
    	                JsonArray jsonInstallsArray = jsonInstalls.toJsonArray();
    	                for(JsonValue v : jsonInstallsArray){
    	                	newInstalls.add(new ModuleModel(v.getJsonValue("name").toString(), v.getJsonValue("version").toString()));
    	                }
    	                return result.createResponseEntity(model);
    	            }
    			}catch(Exception e){
    				result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
    			}
    		}else{
    			result.addRestMessage(this.getUnauthorizedMessage());
    		}
		}
		return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Download Upgrades and optionally backup and restart",
			notes = "Use Modules web socket to track progress"
			)
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value="/upgrade")
	
    public ResponseEntity<Void> upgrade(
    		@ApiParam(value = "Perform Backup first", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean backup,

    		@ApiParam(value = "Restart when completed", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean restart,

    		@ApiParam(value = "Desired Upgrades", required = true)
    		@RequestBody(required=true)  ModuleUpgradesModel model, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	if(user.isAdmin()){
        		//Start Downloads
        		String status = ModulesDwr.startDownloads(model.fullModulesList(), backup, restart);
        		if(status == null){
	            	return result.createResponseEntity();
            	}else{
            		result.addRestMessage(HttpStatus.NOT_MODIFIED, new TranslatableMessage("common.default", status));
        		}
        		
        	}else{
        		result.addRestMessage(this.getUnauthorizedMessage());	
        	}
        }
        return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Cancel Download of Upgrades",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json"}, produces={"application/json"}, value="/upgrade")
    public ResponseEntity<Void> cancelUpgrade(HttpServletRequest request) {

		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
		User user = this.checkUser(request, result);
        if(result.isOk()){
        	if(user.isAdmin()){
        		//Cancel if possible
        		if(ModulesDwr.tryCancelUpgrade()) {
	            	result.addRestMessage(HttpStatus.OK, new TranslatableMessage("common.cancelled"));
            	}else{
            		result.addRestMessage(HttpStatus.NOT_MODIFIED, new TranslatableMessage("modules.versionCheck.notRunning"));
            	}
        	}else{
        		result.addRestMessage(this.getUnauthorizedMessage());	
        	}
        }
        return result.createResponseEntity();
	}
	
}
