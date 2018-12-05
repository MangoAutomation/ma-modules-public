/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.io.Files;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.ModuleRestV2Exception;
import com.infiniteautomation.mango.rest.v2.util.MangoStoreClient;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.io.StreamUtils;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonString;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.ICoreLicense;
import com.serotonin.m2m2.IMangoLifecycle;
import com.serotonin.m2m2.UpgradeVersionState;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleNotificationListener.UpgradeState;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.maint.work.BackupWorkItem;
import com.serotonin.m2m2.rt.maint.work.DatabaseBackupWorkItem;
import com.serotonin.m2m2.shared.ModuleUtils;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value = "Module Definitions", description = "Access Module Definitions")
@RestController
@RequestMapping("/modules")
public class ModulesRestController extends MangoRestController {

    private static final String WEB = "/web";
    private static final String MODULES_WEB_DIR = Constants.DIR_WEB + "/" + Constants.DIR_MODULES;
    private static final String WEB_MODULE_PREFIX = MODULES_WEB_DIR + "/" + ModuleUtils.Constants.MODULE_PREFIX;
    private static final File coreDir = new File(Common.MA_HOME);
    private static final File moduleDir = new File(coreDir, MODULES_WEB_DIR);

    public static AngularJSModuleDefinitionGroupModel getAngularJSModules() {
        List<AngularJSModuleDefinition> definitions = ModuleRegistry.getAngularJSDefinitions();
        List<String> urls = new ArrayList<String>();
        for (AngularJSModuleDefinition def : definitions) {
            String url = UriComponentsBuilder.fromPath(def.getModule().getWebPath())
                    .path(WEB)
                    .path(def.getJavaScriptFilename())
                    .queryParam("v", def.getModule().getVersion().toString())
                    .build()
                    .toUriString();

            urls.add(url);
        }

        AngularJSModuleDefinitionGroupModel model = new AngularJSModuleDefinitionGroupModel();
        model.setUrls(urls);

        return model;
    }

    @ApiOperation(value = "AngularJS Modules", notes = "Publicly Available Angular JS Modules")
    @RequestMapping(method = RequestMethod.GET, value = "/angularjs-modules/public")
    public ResponseEntity<AngularJSModuleDefinitionGroupModel> getPublicAngularJSModules(HttpServletRequest request) {

        RestProcessResult<AngularJSModuleDefinitionGroupModel> result = new RestProcessResult<>(HttpStatus.OK);
        AngularJSModuleDefinitionGroupModel model = getAngularJSModules();
        return result.createResponseEntity(model);
    }

    @ApiOperation(value = "Get Core Module", notes = "For checking current licensing and version")
    @RequestMapping(method = RequestMethod.GET, value = "/core")
    public ResponseEntity<MappingJacksonValue> getCore(HttpServletRequest request) {

        RestProcessResult<MappingJacksonValue> result = new RestProcessResult<>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            ModuleModel coreModule = getCoreModule();
            MappingJacksonValue jacksonValue = new MappingJacksonValue(coreModule);
            if (Permissions.hasAdminPermission(user)) {
                jacksonValue.setSerializationView(ModuleModel.AdminView.class);
            } else {
                jacksonValue.setSerializationView(Object.class);
            }
            return result.createResponseEntity(jacksonValue);
        }
        return result.createResponseEntity();
    }

    @ApiOperation(value = "List Current Installed Modules", notes = "List all installed")
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public ResponseEntity<List<ModuleModel>> listModules(HttpServletRequest request) {

        RestProcessResult<List<ModuleModel>> result = new RestProcessResult<List<ModuleModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if (Permissions.hasAdminPermission(user)) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/list-missing-dependencies")
    public ResponseEntity<Map<String, String>> listMissingModuleDependencies(HttpServletRequest request) {

        RestProcessResult<Map<String, String>> result = new RestProcessResult<>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if (Permissions.hasAdminPermission(user)) {
                return result.createResponseEntity(ModuleRegistry.getMissingDependencies());
            } else {
                result.addRestMessage(this.getUnauthorizedMessage());
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(value = "Get Available Upgrades", notes = "Check the store for Upgrades")
    @RequestMapping(method = RequestMethod.GET, value = "/upgrades-available")
    public ResponseEntity<ModuleUpgradesModel> getUpgrades(HttpServletRequest request) {

        RestProcessResult<ModuleUpgradesModel> result = new RestProcessResult<ModuleUpgradesModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if (Permissions.hasAdminPermission(user)) {
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
                } catch(SocketTimeoutException e) {
                    result.addRestMessage(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.error.requestTimeout", Common.envProps.getString("store.url")));
                } catch(UnknownHostException e) {
                    result.addRestMessage(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("rest.error.unknownHost", Common.envProps.getString("store.url")));
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
    @RequestMapping(method = RequestMethod.POST, value = "/upgrade")

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
    @RequestMapping(method = RequestMethod.PUT, value = "/upgrade")
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
    @RequestMapping(method = RequestMethod.GET, value = "/upgrade-status")
    public ResponseEntity<UpgradeStatusModel> getUpgradeStatus(HttpServletRequest request) {

        RestProcessResult<UpgradeStatusModel> result = new RestProcessResult<UpgradeStatusModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if (Permissions.hasAdminPermission(user)) {
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
                    model.setStage(((UpgradeState) status.getData().get("stage")).name());
                }

                return result.createResponseEntity(model);
            } else {
                result.addRestMessage(this.getUnauthorizedMessage());
            }
        }
        return result.createResponseEntity();
    }

    @ApiOperation(value = "Set Marked For Deletion state of Module", notes = "Marking a module for deletion will un-install it upon restart")
    @RequestMapping(method = RequestMethod.PUT, value = "/deletion-state")
    public ResponseEntity<ModuleModel> markModuleForDeletion(
            @ApiParam(value = "Deletion State", required = true, defaultValue = "false", allowMultiple = false)
            @RequestParam(name="delete", required = true) boolean delete,
            @ApiParam(value = "Module model")
            @RequestBody() ModuleModel model,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        user.ensureHasAdminPermission();
        Module module = ModuleRegistry.getModule(model.getName());
        if(module == null)
            throw new NotFoundException();
        module.setMarkedForDeletion(delete);
        if(module.isMarkedForDeletion() != delete)
            throw new ModuleRestV2Exception(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.modules.error.dependencyFailure"));
        
        return ResponseEntity.ok(new ModuleModel(module));
    }
    
    @ApiOperation(value = "Set Marked For Deletion state of Module", notes = "Marking a module for deletion will un-install it upon restart")
    @RequestMapping(method = RequestMethod.PUT, value = "/deletion-state/{moduleName}")
    public ResponseEntity<ModuleModel> markForDeletion(
            @PathVariable String moduleName,

            @ApiParam(value = "Deletion State", required = true, defaultValue = "false", allowMultiple = false)
            @RequestParam(name="delete", required = true) boolean delete,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        Module module = ModuleRegistry.getModule(moduleName);
        if(module == null)
            throw new NotFoundException();
        module.setMarkedForDeletion(delete);
        if(module.isMarkedForDeletion() != delete)
            throw new ModuleRestV2Exception(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.modules.error.dependencyFailure"));
        
       return ResponseEntity.ok(new ModuleModel(module));
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Download your license from the store", notes = "Admin Only")
    @RequestMapping(
            method = RequestMethod.PUT,
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
            value = "/update-license-payload")
    public ResponseEntity<UpdateLicensePayloadModel> getUpdateLicensePayload(
            @ApiParam(value = "Set content disposition to attachment", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean download,
            HttpServletRequest request) {

        Map<String, String> jsonModules = new HashMap<>();
        List<Module> modules = ModuleRegistry.getModules();
        Module.sortByName(modules);

        Module core = ModuleRegistry.getCoreModule();
        modules.add(0, core);
        for (Module module : modules) {
            if(!module.isMarkedForDeletion())
                jsonModules.put(module.getName(), module.getVersion().toString());
        }

        // Add in the unloaded modules so we don't re-download them if we don't have to
        for (Module module : ModuleRegistry.getUnloadedModules())
            if (!module.isMarkedForDeletion())
                jsonModules.put(module.getName(), module.getVersion().toString());

        String storeUrl = Common.envProps.getString("store.url");
        int upgradeVersionState = SystemSettingsDao.instance.getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE);
        int currentVersionState = UpgradeVersionState.DEVELOPMENT;
        Properties props = new Properties();
        File propFile = new File(Common.MA_HOME + File.separator + "release.properties");
        try {
            if (propFile.exists()) {
                InputStream in;
                in = new FileInputStream(propFile);
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
                String versionState = props.getProperty("versionState");
                try {
                    if (versionState != null)
                        currentVersionState = Integer.valueOf(versionState);
                } catch (NumberFormatException e) { }
            }
        } catch (IOException e1) {
            //Ignore
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, download ? "attachment" : "inline");

        return new ResponseEntity<>(new UpdateLicensePayloadModel(
                Providers.get(ICoreLicense.class).getGuid(),
                SystemSettingsDao.instance.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION),
                Common.envProps.getString("distributor"),
                jsonModules, storeUrl, upgradeVersionState, currentVersionState),
                responseHeaders, HttpStatus.OK);
    }

    private static final Object UPLOAD_UPGRADE_LOCK = new Object();
    private static Object UPLOAD_UPGRADE_IN_PROGRESS;

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Upload upgrade zip bundle, to be installed on restart",
    notes = "The bundle can be downloaded from the Mango Store")
    @RequestMapping(method = RequestMethod.POST, value = "/upload-upgrades")
    public void uploadUpgrades(
            @ApiParam(value = "Perform Backup first", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required = false, defaultValue = "false")
            boolean backup,

            @ApiParam(value = "Restart after upload completes", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required = false, defaultValue = "false")
            boolean restart,

            MultipartHttpServletRequest multipartRequest) throws IOException {

        synchronized (UPLOAD_UPGRADE_LOCK){
            if(UPLOAD_UPGRADE_IN_PROGRESS == null) {
                UPLOAD_UPGRADE_IN_PROGRESS = new Object();
            }else {
                throw new BadRequestException(new TranslatableMessage("rest.error.upgradeUploadInProgress"));
            }
        }

        try {

            if (backup) {
                // Do the backups. They run async, so this returns immediately. The shutdown will
                // wait for the
                // background processes to finish though.
                BackupWorkItem.queueBackup(
                        SystemSettingsDao.instance.getValue(SystemSettingsDao.BACKUP_FILE_LOCATION));
                DatabaseBackupWorkItem.queueBackup(SystemSettingsDao
                        .instance.getValue(SystemSettingsDao.DATABASE_BACKUP_FILE_LOCATION));
            }

            List<MultipartFile> files = new ArrayList<>();
            MultiValueMap<String, MultipartFile> filemap = multipartRequest.getMultiFileMap();
            for (String nameField : filemap.keySet()) {
                files.addAll(filemap.get(nameField));
            }

            // Validate the zip
            if (files.size() == 0)
                throw new BadRequestException(new TranslatableMessage("rest.error.noFileProvided"));

            // Create the temp directory into which to download, if necessary.
            File tempDir = new File(Common.MA_HOME, ModuleUtils.DOWNLOAD_DIR);
            if (!tempDir.exists())
                tempDir.mkdirs();

            // Delete anything that is currently the temp directory.
            FileUtils.cleanDirectory(tempDir);

            try {
                //Save the upload(s) to the temp dir
                for (MultipartFile file : files) {
                    File newFile = new File(tempDir, file.getOriginalFilename());
                    try(FileOutputStream fos = new FileOutputStream(newFile)){
                        org.springframework.util.StreamUtils.copy(file.getInputStream(), fos);
                    }
                }

                String[] potentialUpgrades = tempDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if(name.endsWith(".zip"))
                            return true;
                        else
                            return false;
                    }
                });

                boolean didUpgrade = false;
                for(String potentialUpgrade : potentialUpgrades) {
                    File file = new File(tempDir, potentialUpgrade);
                    boolean core = false;
                    boolean hasWebModules = false;
                    try(FileInputStream fis = new FileInputStream(file)){
                        // Test to see if it is a core or a bundle of only zips or many zip files
                        try (ZipInputStream is = new ZipInputStream(fis)) {
                            ZipEntry entry = is.getNextEntry();
                            if (entry == null) {
                                // Not a zip file or empty, either way we don't care
                                throw new BadRequestException(new TranslatableMessage("rest.error.badUpgradeFile"));
                            } else {
                                do {
                                    if("release.signed".equals(entry.getName())) {
                                        core = true;
                                        break;
                                    }else if(entry.getName().startsWith(WEB_MODULE_PREFIX)) {
                                        hasWebModules = true;
                                    }
                                } while ((entry = is.getNextEntry()) != null);
                            }
                        }
                    }

                    if(core) {
                        //move file to core directory
                        Files.move(file, new File(coreDir, "m2m2-core-upgrade.zip"));
                        didUpgrade = true;
                    }else if(hasWebModules){
                        //This is a zip with modules in web/modules move them all out into the MA_HOME/web/modules dir
                        try(FileInputStream fis = new FileInputStream(file)){
                            try (ZipInputStream is = new ZipInputStream(fis)) {
                                ZipEntry entry;
                                while((entry  = is.getNextEntry()) != null) {
                                    if(entry.getName().startsWith(WEB_MODULE_PREFIX)) {
                                        File newModule = new File(coreDir, entry.getName());
                                        try(FileOutputStream fos = new FileOutputStream(newModule)){
                                            org.springframework.util.StreamUtils.copy(is, fos);
                                        }
                                        didUpgrade = true;
                                    }
                                }
                            }
                        }
                    }else {
                        //if its a module move it to the modules folder
                        if(isModule(file)) {
                            //Its extra work but we better check that it is a module from the store:
                            Files.move(file, new File(moduleDir, file.getName()));
                            didUpgrade = true;
                        }else {
                            //Is this a zip of modules?
                            try(FileInputStream fis = new FileInputStream(file)){
                                try (ZipInputStream is = new ZipInputStream(fis)) {
                                    ZipEntry entry;
                                    while((entry  = is.getNextEntry()) != null) {
                                        if(entry.getName().startsWith(ModuleUtils.Constants.MODULE_PREFIX)) {
                                            //Extract it and confirm it is a module
                                            File newModule = new File(tempDir, entry.getName());
                                            try(FileOutputStream fos = new FileOutputStream(newModule)){
                                                org.springframework.util.StreamUtils.copy(is, fos);
                                            }
                                            if(isModule(newModule)) {
                                                Files.move(newModule, new File(moduleDir, newModule.getName()));
                                                didUpgrade = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Ensure we have some upgrades
                if (!didUpgrade)
                    throw new BadRequestException(new TranslatableMessage("rest.error.invalidUpgradeFile"));
            }finally {
                FileUtils.deleteDirectory(tempDir);
            }
        }finally {
            //TODO We could retain the lock indefinitely if there is a restart request?
            //Release the lock
            synchronized(UPLOAD_UPGRADE_LOCK) {
                UPLOAD_UPGRADE_IN_PROGRESS = null;
            }
        }

        if (restart)
            ModulesDwr.scheduleRestart();
    }

    private boolean isModule(File file) throws FileNotFoundException, IOException {
        try(FileInputStream fis = new FileInputStream(file)){
            try (ZipInputStream is = new ZipInputStream(fis)) {
                ZipEntry entry;
                while((entry  = is.getNextEntry()) != null) {
                    if(entry.getName().equals(ModuleUtils.Constants.MODULE_SIGNED)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Create a Core Module Model
     *
     * @return
     */
    private ModuleModel getCoreModule() {
        CoreModuleModel coreModel = new CoreModuleModel(ModuleRegistry.getCoreModule());
        coreModel.setGuid(Providers.get(ICoreLicense.class).getGuid());
        coreModel.setInstanceDescription(SystemSettingsDao.instance.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        coreModel.setDistributor(Common.envProps.getString("distributor"));
        coreModel.setUpgradeVersionState(SystemSettingsDao.instance.getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE));
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
