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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.AngularJSModuleDefinitionGroupModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

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
	
}
