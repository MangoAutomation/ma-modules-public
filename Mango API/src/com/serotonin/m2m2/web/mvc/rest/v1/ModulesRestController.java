/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.util.MangoStoreClient;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.io.StreamUtils;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonString;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.ICoreLicense;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModulesDwr;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.CredentialsModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.AngularJSModuleDefinitionGroupModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.CoreModuleModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleUpgradeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.ModuleUpgradesModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.UpdateLicensePayloadModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.modules.UpgradeStatusModel;
import com.serotonin.provider.Providers;
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
	public ResponseEntity<Map<String, String>> listMissingModuleDependencies(HttpServletRequest request) {

		RestProcessResult<Map<String, String>> result = new RestProcessResult<>(HttpStatus.OK);
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
						List<ModuleUpgradeModel> upgrades = new ArrayList<>();
						List<ModuleUpgradeModel> newInstalls = new ArrayList<>();
						ModuleUpgradesModel model = new ModuleUpgradesModel(upgrades, newInstalls);

						JsonObject root = jsonResponse.toJsonObject();
						JsonValue jsonUpgrades = root.get("upgrades");
						JsonArray jsonUpgradesArray = jsonUpgrades.toJsonArray();
						Iterator<JsonValue> it = jsonUpgradesArray.iterator();
						while(it.hasNext()) {
						    JsonValue v = it.next();
						    if (v.getJsonValue("name") == null) {
						        it.remove();
						        continue;
						    }
                            String name = v.getJsonValue("name").toString();
                            Module module = "core".equals(name) ? ModuleRegistry.getCoreModule() : ModuleRegistry.getModule(name);
                            if (module == null) {
                                it.remove();
                                continue;
                            }
                            upgrades.add(new ModuleUpgradeModel(module, v));
						}
						JsonValue jsonInstalls = root.get("newInstalls");
						JsonArray jsonInstallsArray = jsonInstalls.toJsonArray();
						for (JsonValue v : jsonInstallsArray) {
							newInstalls.add(new ModuleUpgradeModel(v));
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
	@RequestMapping(method = RequestMethod.PUT, produces = {"application/json" }, value = "/deletion-state/{moduleName}")
	public ResponseEntity<ModuleModel> markForDeletion(
	        @ApiParam(value = "Module name", required = false, allowMultiple = false)
	        @PathVariable(required = false)
	        String moduleName,
	        
			@ApiParam(value = "Deletion State", required = true, defaultValue = "false", allowMultiple = false)
			@RequestParam(required = true)
	        boolean delete,
	        
			@ApiParam(value = "Module model", required = false)
	        @RequestBody(required = false)
	        ModuleModel model,
	        
			HttpServletRequest request) {

		RestProcessResult<ModuleModel> result = new RestProcessResult<ModuleModel>(HttpStatus.OK);
		User user = this.checkUser(request, result);
		if (result.isOk()) {
			if (user.isAdmin()) {
				Module module = ModuleRegistry.getModule(moduleName == null ? model.getName() : moduleName);
				if (module != null) {
					module.setMarkedForDeletion(delete);
			        return result.createResponseEntity(new ModuleModel(module));
				} else {
					result.addRestMessage(getDoesNotExistMessage());
				}
			} else {
				result.addRestMessage(this.getUnauthorizedMessage());
			}
		}
		return result.createResponseEntity(model);
	}

	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Download your license from the store", notes = "Admin Only")
	@RequestMapping(
				method = RequestMethod.PUT, 
				consumes = { "application/json" }, 
				produces = { "application/json" }, 
				value = "/download-license")
	public ResponseEntity<Void> downloadLicense(
			@ApiParam(value = "Connection retries", required = false, defaultValue = "0", allowMultiple = false)
			@RequestParam(required = false, defaultValue="0") int retries,

			@ApiParam(value = "User Credentials", required = true) 
			@RequestBody(required = true) CredentialsModel model,
			HttpServletRequest request) {
		
		try{
			String storeUrl = Common.envProps.getString("store.url");
			//Login to the store
			MangoStoreClient client = new MangoStoreClient(storeUrl);
			client.login(model.getUsername(), model.getPassword(), retries);
			
	        // Send the token request
	        String guid = Providers.get(ICoreLicense.class).getGuid();
	        String distributor = Common.envProps.getString("distributor");
	        String token = client.getLicenseToken(guid, distributor, retries);
	        
	        //With the token we can make the request to download the file
	        String license = client.getLicense(token, retries);
	        
	        saveLicense(license);
	        
	        return new ResponseEntity<Void>(HttpStatus.OK);
		}catch(Exception e){
			throw new GenericRestException(HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	@PreAuthorize("isAdmin()")
	@ApiOperation(value = "Get the update license payload, to make requests to store", notes = "Admin Only")
	@RequestMapping(
				method = RequestMethod.GET, 
				consumes = { "application/json" }, 
				produces = { "application/json" }, 
				value = "/update-license-payload")
	public ResponseEntity<UpdateLicensePayloadModel> getUpdateLicensePayload(HttpServletRequest request) {
        
		Map<String, String> jsonModules = new HashMap<>();
        List<Module> modules = ModuleRegistry.getModules();
        Module.sortByName(modules);

        Module core = ModuleRegistry.getCoreModule();
        modules.add(0, core);
		for (Module module : modules) {
            jsonModules.put(module.getName(), module.getVersion().toString());
        }

		return new ResponseEntity<>(new UpdateLicensePayloadModel(
				Providers.get(ICoreLicense.class).getGuid(),
				SystemSettingsDao.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION),
				Common.envProps.getString("distributor"),
				jsonModules), HttpStatus.OK);
	}

	
	/**
	 * Create a Core Module Model
	 * 
	 * @return
	 */
	private ModuleModel getCoreModule() {
	    CoreModuleModel coreModel = new CoreModuleModel(ModuleRegistry.getCoreModule());
	    coreModel.setGuid(Providers.get(ICoreLicense.class).getGuid());
        coreModel.setInstanceDescription(SystemSettingsDao.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        coreModel.setDistributor(Common.envProps.getString("distributor"));
        coreModel.setUpgradeVersionState(SystemSettingsDao.getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE));
	    return coreModel;
	}
	
    private void saveLicense(String license) throws Exception {

        // If there is an existing license file, move it to a backup name. First check if the backup name exists, and 
        // if so, delete it.
        File licenseFile = new File(Common.MA_HOME, "m2m2.license.xml");
        File backupFile = new File(Common.MA_HOME, "m2m2.license.old.xml");

        if (licenseFile.exists()) {
            if (backupFile.exists())
                backupFile.delete();
            licenseFile.renameTo(backupFile);
        }

        // Save the data
        StreamUtils.writeFile(licenseFile, license);

        // Reload the license file.
        Providers.get(IMangoLifecycle.class).loadLic();
    }
    
}
