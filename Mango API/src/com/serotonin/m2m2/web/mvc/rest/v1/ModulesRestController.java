/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.db.pair.StringStringPair;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonString;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.shared.DependencyData;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.AngularJSModuleDefinitionGroupModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleUpgradesModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.UpgradeStatusModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * 
 * @author Terry Packer
 */
@Api(value = "Module Definitions", description = "Access Module Definitions")
@RestController
@RequestMapping("/v1/modules")
public class ModulesRestController extends MangoRestController {

	private final String WEB = "/web";

	@ApiOperation(value = "AngularJS Modules", notes = "Publicly Available Angular JS Modules")
	@RequestMapping(method = RequestMethod.GET, value = "/angularjs-modules/public", produces = { "application/json" })
	public ResponseEntity<AngularJSModuleDefinitionGroupModel> getPublicAngularJSModules(HttpServletRequest request) {

		RestProcessResult<AngularJSModuleDefinitionGroupModel> result = new RestProcessResult<AngularJSModuleDefinitionGroupModel>(
				HttpStatus.OK);

		List<AngularJSModuleDefinition> definitions = ModuleRegistry.getAngularJSDefinitions();
		List<String> urls = new ArrayList<String>();
		for (AngularJSModuleDefinition def : definitions)
			urls.add(def.getModule().getWebPath() + WEB + def.getJavaScriptFilename());

		AngularJSModuleDefinitionGroupModel model = new AngularJSModuleDefinitionGroupModel();
		model.setUrls(urls);

		return result.createResponseEntity(model);
	}

	@ApiOperation(value = "Get Core Module", notes = "For checking current licensing and version")
	@RequestMapping(method = RequestMethod.GET, value = "/core", produces = { "application/json" })
	public ResponseEntity<MappingJacksonValue> getCore(HttpServletRequest request) {

		RestProcessResult<MappingJacksonValue> result = new RestProcessResult<>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
		    ModuleModel coreModule = getCoreModule();
		    MappingJacksonValue jacksonValue = new MappingJacksonValue(coreModule);
			if (Permissions.hasAdmin(user)) {
			    jacksonValue.setSerializationView(ModuleModel.AdminView.class);
			} else {
                jacksonValue.setSerializationView(Object.class);
			}
            return result.createResponseEntity(jacksonValue);
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "List Current Installed Modules", notes = "List all installed")
	@RequestMapping(method = RequestMethod.GET, value = "/list", produces = { "application/json" })
	public ResponseEntity<List<ModuleModel>> listModules(HttpServletRequest request) {

		RestProcessResult<List<ModuleModel>> result = new RestProcessResult<List<ModuleModel>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (Permissions.hasAdmin(user)) {
				List<ModuleModel> models = new ArrayList<ModuleModel>();
				ModuleModel core = getCoreModule();
				List<Module> modules = ModuleRegistry.getModules();
				models.add(core);
				for (Module module : modules)
					models.add(new ModuleModel(module));
				
				//Add the unloaded modules at the end?
				List<Module> unloaded = ModuleRegistry.getUnloadedModules();
				for(Module module : unloaded){
					ModuleModel model = new ModuleModel(module);
					model.setUnloaded(true);
					models.add(model);
				}
				
				//TODO Sort them?
				
				return result.createResponseEntity(models);
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}
	
	@ApiOperation(value = "List Current Missing Module Dependencies", notes = "List all installed")
	@RequestMapping(method = RequestMethod.GET, value = "/list-missing-dependencies", produces = { "application/json" })
	public ResponseEntity<Map<String, DependencyData>> listMissingModuleDependencies(HttpServletRequest request) {

		RestProcessResult<Map<String, DependencyData>> result = new RestProcessResult<Map<String, DependencyData>>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (Permissions.hasAdmin(user)) {
				return result.createResponseEntity(ModuleRegistry.getMissingDependencies());
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "Get Available Upgrades", notes = "Check the store for Upgrades")
	@RequestMapping(method = RequestMethod.GET, value = "/upgrades-available", produces = { "application/json" })
	public ResponseEntity<ModuleUpgradesModel> getUpgrades(HttpServletRequest request) {

		RestProcessResult<ModuleUpgradesModel> result = new RestProcessResult<ModuleUpgradesModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (Permissions.hasAdmin(user)) {
				// Do the check
				try {
					JsonValue jsonResponse = ModulesDwr.getAvailableUpgrades();
					if (jsonResponse instanceof JsonString) {
						result.addRestMessage(getInternalServerErrorMessage(jsonResponse.toString()));
					} else {
						List<ModuleModel> upgrades = new ArrayList<ModuleModel>();
						List<ModuleModel> newInstalls = new ArrayList<ModuleModel>();
						ModuleUpgradesModel model = new ModuleUpgradesModel(upgrades, newInstalls);

						JsonObject root = jsonResponse.toJsonObject();
						JsonValue jsonUpgrades = root.get("upgrades");
						JsonArray jsonUpgradesArray = jsonUpgrades.toJsonArray();
						for (JsonValue v : jsonUpgradesArray) {
							upgrades.add(new ModuleModel(v));
						}
						JsonValue jsonInstalls = root.get("newInstalls");
						JsonArray jsonInstallsArray = jsonInstalls.toJsonArray();
						for (JsonValue v : jsonInstallsArray) {
							newInstalls.add(new ModuleModel(v));
						}
						return result.createResponseEntity(model);
					}
				} catch (Exception e) {
					result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				}
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "Download Upgrades and optionally backup and restart", notes = "Use Modules web socket to track progress")
	@RequestMapping(method = RequestMethod.POST, consumes = { "application/json" }, produces = {
			"application/json" }, value = "/upgrade")

	public ResponseEntity<Void> upgrade(
			@ApiParam(value = "Perform Backup first", required = false, defaultValue = "false", allowMultiple = false) @RequestParam(required = false, defaultValue = "false") boolean backup,

			@ApiParam(value = "Restart when completed", required = false, defaultValue = "false", allowMultiple = false) @RequestParam(required = false, defaultValue = "false") boolean restart,

			@ApiParam(value = "Desired Upgrades", required = true) @RequestBody(required = true) ModuleUpgradesModel model,
			UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);

		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (user.isAdmin()) {
				// Start Downloads
				String status = ModulesDwr.startDownloads(model.fullModulesList(), backup, restart);
				if (status == null) {
					return result.createResponseEntity();
				} else {
					result.addRestMessage(HttpStatus.NOT_MODIFIED, new TranslatableMessage("common.default", status));
				}

			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "Cancel Download of Upgrades", notes = "")
	@RequestMapping(method = RequestMethod.PUT, consumes = { "application/json" }, produces = {
			"application/json" }, value = "/upgrade")
	public ResponseEntity<Void> cancelUpgrade(HttpServletRequest request) {

		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (user.isAdmin()) {
				// Cancel if possible
				if (ModulesDwr.tryCancelUpgrade()) {
					result.addRestMessage(HttpStatus.OK, new TranslatableMessage("common.cancelled"));
				} else {
					result.addRestMessage(HttpStatus.NOT_MODIFIED,
							new TranslatableMessage("modules.versionCheck.notRunning"));
				}
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "Get Current Upgrade Task Status", notes = "")
	@RequestMapping(method = RequestMethod.GET, value = "/upgrade-status", produces = { "application/json" })
	public ResponseEntity<UpgradeStatusModel> getUpgradeStatus(HttpServletRequest request) {

		RestProcessResult<UpgradeStatusModel> result = new RestProcessResult<UpgradeStatusModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (Permissions.hasAdmin(user)) {
				ProcessResult status = ModulesDwr.monitorDownloads();
				UpgradeStatusModel model = new UpgradeStatusModel();
				if (status.getHasMessages()) {
					// Not running
					model.setRunning(false);
				} else {
					List<ModuleModel> modules = new ArrayList<ModuleModel>();
					@SuppressWarnings("unchecked")
					List<StringStringPair> results = (List<StringStringPair>) status.getData().get("results");
					for (StringStringPair r : results)
						modules.add(new ModuleModel(r.getKey(), r.getValue()));
					model.setResults(modules);
					model.setFinished((boolean) status.getData().get("finished"));
					model.setCancelled((boolean) status.getData().get("cancelled"));
					model.setWillRestart((boolean) status.getData().get("restart"));
					Object error = status.getData().get("error");
					if (error != null)
						model.setError((String) error);
					model.setStage((String) status.getData().get("stage"));
				}

				return result.createResponseEntity(model);
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity();
	}

	@ApiOperation(value = "Set Marked For Deletion state of Module", notes = "Marking a module for deletion will un-install it upon restart")
	@RequestMapping(method = RequestMethod.PUT, consumes = { "application/json" }, produces = {
			"application/json" }, value = "/deletion-state")
	public ResponseEntity<ModuleModel> markForDeletion(
			@ApiParam(value = "Deletion Statue", required = false, defaultValue = "false", allowMultiple = false) @RequestParam(required = true) boolean delete,

			@ApiParam(value = "Desired Upgrades", required = true) @RequestBody(required = true) ModuleModel model,
			HttpServletRequest request) {

		RestProcessResult<ModuleModel> result = new RestProcessResult<ModuleModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (user.isAdmin()) {
				Module module = ModuleRegistry.getModule(model.getName());
				if (module != null) {
					module.setMarkedForDeletion(delete);
				} else {
					result.addRestMessage(getDoesNotExistMessage());
				}
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity(model);
	}

	/**
	 * Create a Core Module Model
	 * 
	 * @return
	 */
	private ModuleModel getCoreModule() {
		ModuleModel model = new ModuleModel(ModuleRegistry.getCoreModule());
		model.setVersion(Common.getVersion().getFullString());
		return model;
	}
}
