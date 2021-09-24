/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

import com.github.zafarkhaja.semver.Version;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.GenericRestException;
import com.infiniteautomation.mango.rest.latest.exception.ModuleRestException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.latest.model.CredentialsModel;
import com.infiniteautomation.mango.rest.latest.model.modules.AngularJSModuleDefinitionGroupModel;
import com.infiniteautomation.mango.rest.latest.model.modules.AngularJSModuleDefinitionGroupModel.ModuleInfo;
import com.infiniteautomation.mango.rest.latest.model.modules.CoreModuleModel;
import com.infiniteautomation.mango.rest.latest.model.modules.ModuleModel;
import com.infiniteautomation.mango.rest.latest.model.modules.ModuleModel.AdminView;
import com.infiniteautomation.mango.rest.latest.model.modules.ModuleUpgradeModel;
import com.infiniteautomation.mango.rest.latest.model.modules.ModuleUpgradesModel;
import com.infiniteautomation.mango.rest.latest.model.modules.UpdateLicensePayloadModel;
import com.infiniteautomation.mango.rest.latest.model.modules.UpgradeStatusModel;
import com.infiniteautomation.mango.rest.latest.model.modules.UpgradeUploadResult;
import com.infiniteautomation.mango.rest.latest.model.modules.UpgradeUploadResult.InvalidModule;
import com.infiniteautomation.mango.rest.latest.util.MangoStoreClient;
import com.infiniteautomation.mango.spring.service.ModulesService;
import com.infiniteautomation.mango.spring.service.ModulesService.UpgradeStatus;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.FeatureDisabledException;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.json.JsonException;
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
import com.serotonin.m2m2.module.ModuleRegistry.CoreModule;
import com.serotonin.m2m2.rt.maint.work.BackupWorkItem;
import com.serotonin.m2m2.rt.maint.work.DatabaseBackupWorkItem;
import com.serotonin.m2m2.shared.ModuleUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.mvc.spring.security.permissions.AnonymousAccess;
import com.serotonin.provider.Providers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value = "Module Definitions access")
@RestController
@RequestMapping("/modules")
public class ModulesRestController {
    private final Logger LOG = LoggerFactory.getLogger(ModulesRestController.class);
    private static final String WEB_MODULE_PREFIX = Constants.DIR_WEB + "/" + Constants.DIR_MODULES + "/" + ModuleUtils.Constants.MODULE_PREFIX;

    private final Environment env;
    private final ModulesService service;
    private final PermissionService permissionService;

    @Autowired
    public ModulesRestController(Environment env, ModulesService service, PermissionService permissionService) {
        this.env = env;
        this.service = service;
        this.permissionService = permissionService;
    }

    public static final DateTimeFormatter PRE_RELEASE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd.HHmmss-1")
            .withZone(ZoneId.from(ZoneOffset.UTC));

    public static AngularJSModuleDefinitionGroupModel getAngularJSModules(boolean developmentMode) {
        AngularJSModuleDefinitionGroupModel model = new AngularJSModuleDefinitionGroupModel();
        URI webUri = Common.MA_HOME_PATH.resolve(Constants.DIR_WEB).toUri();

        for (AngularJSModuleDefinition def : ModuleRegistry.getAngularJSDefinitions()) {
            Module module = def.getModule();
            Version moduleVersion = module.getVersion();

            String version = moduleVersion.toString();
            String preReleaseVersion = moduleVersion.getPreReleaseVersion();
            if ("SNAPSHOT".equals(preReleaseVersion)) {
                Date buildDate = module.getBuildDate();
                if (developmentMode) {
                    Path filePath = def.getAbsoluteJavaScriptPath();
                    try {
                        buildDate = new Date(Files.getLastModifiedTime(filePath).toMillis());
                    } catch (IOException e) {
                        // ignore
                    }
                }
                String preRelease = PRE_RELEASE_FORMATTER.format(buildDate.toInstant());
                version = moduleVersion.setPreReleaseVersion(preRelease).toString();
            }

            URI uri = webUri.relativize(def.getAbsoluteJavaScriptPath().toUri());

            String urlWithVersion = UriComponentsBuilder.fromUri(uri)
                    .queryParam("v", version)
                    .build()
                    .toUriString();

            ModuleInfo info = new ModuleInfo();
            info.setUrl(uri.toString());
            info.setVersion(version);
            info.setName(module.getName());
            info.setUpgradedDate(module.getUpgradedDate());
            model.add("/" + urlWithVersion, info);
        }

        return model;
    }

    @ApiOperation(value = "AngularJS Modules", notes = "Publicly Available Angular JS Modules")
    @RequestMapping(method = RequestMethod.GET, value = "/angularjs-modules/public")
    @AnonymousAccess
    public AngularJSModuleDefinitionGroupModel getPublicAngularJSModules() {
        return getAngularJSModules(env.getProperty("development.enabled", Boolean.class, false));
    }

    @ApiOperation(value = "Get Core Module", notes = "For checking current licensing and version", response = ModuleModel.class)
    @RequestMapping(method = RequestMethod.GET, value = "/core")
    public MappingJacksonValue getCore(@AuthenticationPrincipal PermissionHolder user) {

        CoreModuleModel coreModel = new CoreModuleModel(ModuleRegistry.getModule(ModuleRegistry.CORE_MODULE_NAME));
        coreModel.setGuid(Providers.get(ICoreLicense.class).getGuid());
        coreModel.setInstanceDescription(SystemSettingsDao.getInstance().getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        coreModel.setDistributor(Common.envProps.getString("distributor"));
        coreModel.setUpgradeVersionState(SystemSettingsDao.getInstance().getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE));
        coreModel.setStoreUrl(Common.envProps.getString("store.url"));

        MappingJacksonValue jacksonValue = new MappingJacksonValue(coreModel);
        if (permissionService.hasAdminRole(user)) {
            jacksonValue.setSerializationView(AdminView.class);
        } else {
            jacksonValue.setSerializationView(Object.class);
        }
        return jacksonValue;
    }

    @ApiOperation(value = "List Current Installed Modules", notes = "List all installed")
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public List<ModuleModel> listModules(@AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);

        List<ModuleModel> models = new ArrayList<ModuleModel>();
        List<Module> modules = ModuleRegistry.getModules();
        for (Module module : modules) {
            if(module instanceof CoreModule) {
                CoreModuleModel coreModel = new CoreModuleModel(ModuleRegistry.getModule(ModuleRegistry.CORE_MODULE_NAME));
                coreModel.setGuid(Providers.get(ICoreLicense.class).getGuid());
                coreModel.setInstanceDescription(SystemSettingsDao.getInstance().getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
                coreModel.setDistributor(Common.envProps.getString("distributor"));
                coreModel.setUpgradeVersionState(SystemSettingsDao.getInstance().getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE));
                coreModel.setStoreUrl(Common.envProps.getString("store.url"));
                models.add(coreModel);
            }else {
                models.add(new ModuleModel(module));
            }
        }

        //Add the unloaded modules at the end?
        List<Module> unloaded = ModuleRegistry.getUnloadedModules();
        for(Module module : unloaded){
            ModuleModel model = new ModuleModel(module);
            model.setUnloaded(true);
            models.add(model);
        }
        return models;
    }

    @ApiOperation(value = "List Current Missing Module Dependencies", notes = "List all installed")
    @RequestMapping(method = RequestMethod.GET, value = "/list-missing-dependencies")
    public Map<String, String> listMissingModuleDependencies(@AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        return ModuleRegistry.getMissingDependencies();
    }

    @ApiOperation(value = "Get Available Upgrades", notes = "Check the store for Upgrades")
    @RequestMapping(method = RequestMethod.GET, value = "/upgrades-available")
    public ModuleUpgradesModel getUpgrades(@AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        // Do the check
        try {
            JsonValue jsonResponse = null;
            //This is handled oddly in the ModulesDwr so check it here for validation
            String baseUrl = Common.envProps.getString("store.url");
            if(!StringUtils.isEmpty(baseUrl)) {
                jsonResponse = service.getAvailableUpgrades();
            }else {
                ProcessResult result = new ProcessResult();
                result.addGenericMessage("modules.versionCheck.storeNotSet");
                throw new ValidationFailedRestException(result);
            }

            if(jsonResponse == null) {
                //Indicates that the store url is not set, which we check for above so this really means the response was a null JSON value
                throw new BadRequestException(new TranslatableMessage("modules.versionCheck.storeResponseEmpty"));
            }else if (jsonResponse instanceof JsonString) {
                throw new ServerErrorException(new TranslatableMessage("common.default", jsonResponse.toString()));
            } else {
                List<ModuleUpgradeModel> upgrades = new ArrayList<>();
                List<ModuleUpgradeModel> newInstalls = new ArrayList<>();
                List<ModuleModel> unavailableModules = new ArrayList<>();

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
                    Module module = ModuleRegistry.getModule(name);
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

                //Extract any unavailable modules
                if(root.containsKey("unavailableModules")) {
                    JsonValue jsonUnavailableModules = root.get("unavailableModules");
                    JsonArray jsonUnavailableModulesArray = jsonUnavailableModules.toJsonArray();
                    List<Module> modules = ModuleRegistry.getModules();
                    for (JsonValue v : jsonUnavailableModulesArray) {
                        Module unavailable = null;
                        String moduleName = v.toString();
                        for (Module module : modules) {
                            if (StringUtils.equals(module.getName(), moduleName)) {
                                unavailable = module;
                                break;
                            }
                        }
                        if (unavailable == null) {
                            //Didn't find it?  Store must be wrong?
                            LOG.warn("Store reported unavailable module " + moduleName + " but it isn't installed.");
                        } else {
                            unavailableModules.add(new ModuleModel(unavailable));
                        }
                    }
                }
                model.setUnavailableModules(unavailableModules);

                return model;
            }
        } catch(SocketTimeoutException e) {
            throw new ServerErrorException(new TranslatableMessage("rest.error.requestTimeout", Common.envProps.getString("store.url")));
        } catch(UnknownHostException e) {
            throw new ServerErrorException(new TranslatableMessage("rest.error.unknownHost", Common.envProps.getString("store.url")));
        } catch (IOException | JsonException | HttpException e) {
            throw new ServerErrorException(e);
        }
    }

    @ApiOperation(value = "Download Upgrades and optionally backup and restart", notes = "Use Modules web socket to track progress")
    @RequestMapping(method = RequestMethod.POST, value = "/upgrade")

    public ResponseEntity<TranslatableMessage> upgrade(
            @ApiParam(value = "Perform Backup first", required = false, defaultValue = "false", allowMultiple = false) @RequestParam(required = false, defaultValue = "false") boolean backup,

            @ApiParam(value = "Restart when completed", required = false, defaultValue = "false", allowMultiple = false) @RequestParam(required = false, defaultValue = "false") boolean restart,

            @ApiParam(value = "Desired Upgrades", required = true) @RequestBody(required = true) ModuleUpgradesModel model,
            @AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        // Start Downloads
        String status = service.startDownloads(model.fullModulesList(), backup, restart);
        if (status == null) {
            return new ResponseEntity<>(new TranslatableMessage("rest.httpStatus.200"), HttpStatus.OK);
        } else {
            //headers are for compatibility with v1 for the UI
            HttpHeaders headers = new HttpHeaders();
            headers.add("messages", stripAndTrimHeader(status, -1));
            return new ResponseEntity<>(new TranslatableMessage("common.default", status), HttpStatus.NOT_MODIFIED);
        }
    }

    @ApiOperation(value = "Cancel Download of Upgrades", notes = "")
    @RequestMapping(method = RequestMethod.PUT, value = "/upgrade")
    public ResponseEntity<TranslatableMessage> cancelUpgrade(@AuthenticationPrincipal User user) {
        permissionService.ensureAdminRole(user);
        // Cancel if possible
        if (service.tryCancelUpgrade()) {
            //headers are for compatibility with v1 for the UI
            HttpHeaders headers = new HttpHeaders();
            headers.add("messages", new TranslatableMessage("common.cancelled").translate(user.getTranslations()));
            return new ResponseEntity<>(new TranslatableMessage("common.cancelled"), HttpStatus.OK);
        } else {
            //headers are for compatibility with v1 for the UI
            HttpHeaders headers = new HttpHeaders();
            headers.add("messages", new TranslatableMessage("modules.versionCheck.notRunning").translate(user.getTranslations()));
            return new ResponseEntity<>(new TranslatableMessage("modules.versionCheck.notRunning"), HttpStatus.NOT_MODIFIED);
        }
    }

    @ApiOperation(value = "Get Current Upgrade Task Status", notes = "")
    @RequestMapping(method = RequestMethod.GET, value = "/upgrade-status")
    public UpgradeStatusModel getUpgradeStatus(@AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        UpgradeStatus status = service.monitorDownloads();
        UpgradeStatusModel model = new UpgradeStatusModel();
        if (status.getStage() == UpgradeState.IDLE) {
            // Not running
            model.setRunning(false);
        } else {
            List<ModuleModel> modules = new ArrayList<ModuleModel>();
            List<StringStringPair> results = status.getResults();
            for (StringStringPair r : results)
                modules.add(new ModuleModel(r.getKey(), r.getValue()));
            model.setResults(modules);
            model.setFinished(status.isFinished());
            model.setCancelled(status.isCancelled());
            model.setWillRestart(status.isRestart());
            model.setError(status.getError());
            model.setStage(status.getStage().name());
        }
        return model;
    }

    @ApiOperation(value = "Set Marked For Deletion state of Module", notes = "Marking a module for deletion will un-install it upon restart")
    @RequestMapping(method = RequestMethod.PUT, value = "/deletion-state")
    public ModuleModel markModuleForDeletion(
            @ApiParam(value = "Deletion State", required = true, defaultValue = "false", allowMultiple = false)
            @RequestParam(name="delete", required = true) boolean delete,
            @ApiParam(value = "Module model")
            @RequestBody() ModuleModel model,
            @AuthenticationPrincipal PermissionHolder user,
            HttpServletRequest request) {
        permissionService.ensureAdminRole(user);
        Module module = ModuleRegistry.getModule(model.getName());
        if(module == null)
            throw new NotFoundException();
        module.setMarkedForDeletion(delete);
        if(module.isMarkedForDeletion() != delete)
            throw new ModuleRestException(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.modules.error.dependencyFailure"));

        return new ModuleModel(module);
    }

    @ApiOperation(value = "Set Marked For Deletion state of Module", notes = "Marking a module for deletion will un-install it upon restart")
    @RequestMapping(method = RequestMethod.PUT, value = "/deletion-state/{moduleName}")
    public ResponseEntity<ModuleModel> markForDeletion(
            @PathVariable String moduleName,

            @ApiParam(value = "Deletion State", required = true, defaultValue = "false", allowMultiple = false)
            @RequestParam(name="delete", required = true) boolean delete,
            @AuthenticationPrincipal PermissionHolder user,
            HttpServletRequest request) {

        Module module = ModuleRegistry.getModule(moduleName);
        if(module == null)
            throw new NotFoundException();
        module.setMarkedForDeletion(delete);
        if(module.isMarkedForDeletion() != delete)
            throw new ModuleRestException(HttpStatus.BAD_REQUEST, new TranslatableMessage("rest.modules.error.dependencyFailure"));

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

        for (Module module : modules) {
            if(!module.isMarkedForDeletion())
                jsonModules.put(module.getName(), module.getVersion().toString());
        }

        // Add in the unloaded modules so we don't re-download them if we don't have to
        for (Module module : ModuleRegistry.getUnloadedModules())
            if (!module.isMarkedForDeletion())
                jsonModules.put(module.getName(), module.getVersion().toString());

        String storeUrl = Common.envProps.getString("store.url");
        int upgradeVersionState = SystemSettingsDao.getInstance().getIntValue(SystemSettingsDao.UPGRADE_VERSION_STATE);
        int currentVersionState = UpgradeVersionState.DEVELOPMENT;
        Properties props = new Properties();
        File propFile = Common.MA_HOME_PATH.resolve("release.properties").toFile();
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
                SystemSettingsDao.getInstance().getValue(SystemSettingsDao.INSTANCE_DESCRIPTION),
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
    public UpgradeUploadResult uploadUpgrades(
            @ApiParam(value = "Perform Backup first", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required = false, defaultValue = "false")
            boolean backup,

            @ApiParam(value = "Restart after upload completes", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required = false, defaultValue = "false")
            boolean restart,

            MultipartHttpServletRequest multipartRequest) throws IOException {

        if (env.getProperty("store.disableUpgrades", Boolean.class, false)) {
            throw new FeatureDisabledException(new TranslatableMessage("modules.error.upgradesDisabled"));
        }

        synchronized (UPLOAD_UPGRADE_LOCK){
            if(UPLOAD_UPGRADE_IN_PROGRESS == null) {
                UPLOAD_UPGRADE_IN_PROGRESS = new Object();
            }else {
                throw new BadRequestException(new TranslatableMessage("rest.error.upgradeUploadInProgress"));
            }
        }
        UpgradeUploadResult result = new UpgradeUploadResult();
        try {

            if (backup) {
                // Do the backups. They run async, so this returns immediately. The shutdown will
                // wait for the
                // background processes to finish though.
                BackupWorkItem.queueBackup(
                        SystemSettingsDao.getInstance().getValue(SystemSettingsDao.BACKUP_FILE_LOCATION));
                DatabaseBackupWorkItem.queueBackup(SystemSettingsDao
                        .getInstance().getValue(SystemSettingsDao.DATABASE_BACKUP_FILE_LOCATION));
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
            Path tempDir = Common.getTempPath().resolve(ModuleUtils.DOWNLOAD_DIR);
            Files.createDirectories(tempDir);

            // Delete anything that is currently the temp directory.
            FileUtils.cleanDirectory(tempDir.toFile());

            try {
                //Save the upload(s) to the temp dir
                for (MultipartFile file : files) {
                    Path newFile = tempDir.resolve(file.getOriginalFilename());
                    try (InputStream is = file.getInputStream()) {
                        Files.copy(is, newFile);
                    }
                }

                List<Path> potentialUpgrades = Files.list(tempDir)
                        .filter(p -> p.toString().endsWith(".zip"))
                        .collect(Collectors.toList());

                List<String> upgraded = new ArrayList<>();
                List<String> unsigned = new ArrayList<>();

                boolean developmentMode = env.getProperty("development.enabled", Boolean.class, false);
                for(Path file : potentialUpgrades) {
                    boolean core = false;
                    boolean hasWebModules = false;
                    // Test to see if it is a core or a bundle of only zips or many zip files
                    try (ZipInputStream is = new ZipInputStream(Files.newInputStream(file))) {
                        ZipEntry entry = is.getNextEntry();
                        if (entry == null) {
                            // Not a zip file or empty, either way we don't care
                            throw new BadRequestException(new TranslatableMessage("rest.error.badUpgradeFile"));
                        } else {
                            do {
                                String entryName = entry.getName();
                                if("release.signed".equals(entryName) || "release.properties".equals(entryName)) {
                                    if(!developmentMode && "release.properties".equals(entryName)) {
                                        throw new BadRequestException(new TranslatableMessage("modules.unsigned.core.development"));
                                    }
                                    core = true;
                                    break;
                                }else if(entry.getName().startsWith(WEB_MODULE_PREFIX)) {
                                    hasWebModules = true;
                                }
                            } while ((entry = is.getNextEntry()) != null);
                        }
                    }

                    if(core) {
                        //move file to core directory
                        Files.move(file, Common.MA_HOME_PATH.resolve("m2m2-core-upgrade.zip"));
                        upgraded.add(file.getFileName().toString());
                    }else if(hasWebModules) {
                        //This is a zip with modules in web/modules move them all out into the MA_HOME/web/modules dir
                        try (ZipInputStream is = new ZipInputStream(Files.newInputStream(file))) {
                            ZipEntry entry;
                            while ((entry = is.getNextEntry()) != null) {
                                if(entry.getName().startsWith(WEB_MODULE_PREFIX)) {
                                    Path newModule = Common.MA_HOME_PATH.resolve(entry.getName());
                                    Files.copy(is, newModule);
                                    upgraded.add(newModule.getFileName().toString());
                                }
                            }
                        }
                    }else {
                        //if its a module move it to the modules folder
                        if(isModule(file, unsigned)) {
                            //Its extra work but we better check that it is a module from the store:
                            Files.move(file, Common.MODULES.resolve(file.getFileName()));
                            upgraded.add(file.getFileName().toString());
                        }else {
                            //Is this a zip of modules?
                            try (ZipInputStream is = new ZipInputStream(Files.newInputStream(file))) {
                                ZipEntry entry;
                                while((entry  = is.getNextEntry()) != null) {
                                    if(entry.getName().startsWith(ModuleUtils.Constants.MODULE_PREFIX)) {
                                        //Extract it and confirm it is a module
                                        Path newModule = tempDir.resolve(entry.getName());
                                        Files.copy(is, newModule);
                                        if(isModule(newModule, unsigned)) {
                                            Files.move(newModule, Common.MODULES.resolve(newModule.getFileName()));
                                            upgraded.add(newModule.getFileName().toString());
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

                // Ensure we have some upgrades
                if (upgraded.size() == 0 && unsigned.size() == 0) {
                    throw new BadRequestException(new TranslatableMessage("rest.error.invalidUpgradeFile"));
                }else if(upgraded.size() == 0 && unsigned.size() > 0) {
                    throw new BadRequestException(new TranslatableMessage("modules.unsigned.development"));
                }

                result.setToUpgrade(upgraded);
                List<InvalidModule> invalid = new ArrayList<>();
                result.setInvalid(invalid);
                for(String u : unsigned) {
                    invalid.add(new InvalidModule(u, new TranslatableMessage("modules.unsigned.development")));
                }
            }finally {
                FileUtils.deleteDirectory(tempDir.toFile());
            }
        }finally {
            //TODO We could retain the lock indefinitely if there is a restart request?
            //Release the lock
            synchronized(UPLOAD_UPGRADE_LOCK) {
                UPLOAD_UPGRADE_IN_PROGRESS = null;
            }
        }

        if (restart && result.getToUpgrade().size() > 0) {
            IMangoLifecycle lifecycle = Providers.get(IMangoLifecycle.class);
            lifecycle.scheduleShutdown(null, true, Common.getUser());
            result.setRestart(restart);
        }
        return result;
    }

    private boolean isModule(Path file, List<String> unsigned) throws FileNotFoundException, IOException {
        boolean developmentMode = env.getProperty("development.enabled", Boolean.class, false);
        try (ZipInputStream is = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while((entry  = is.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (ModuleUtils.Constants.MODULE_SIGNED.equals(entryName) || ModuleUtils.Constants.MODULE_PROPERTIES.equals(entryName)) {
                    if(!developmentMode && ModuleUtils.Constants.MODULE_PROPERTIES.equals(entryName)) {
                        unsigned.add(file.getFileName().toString());
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void saveLicense(String license) throws Exception {
        Path coreDataPath = ModuleRegistry.CORE_MODULE.moduleDataPath();

        // If there is an existing license file, move it to a backup name. First check if the backup name exists, and
        // if so, delete it.
        Path licenseFile = coreDataPath.resolve("m2m2.license.xml");
        Path backupFile = coreDataPath.resolve("m2m2.license.old.xml");

        if (Files.exists(licenseFile)) {
            Files.move(licenseFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Save the data
        try (Writer writer = Files.newBufferedWriter(licenseFile)) {
            writer.write(license);
        }

        // Reload the license file.
        Providers.get(IMangoLifecycle.class).loadLic();
    }

    /**
     * Util to remove CR/LF and ensure max length
     */
    private static final Pattern CR_OR_LF = Pattern.compile("\\r|\\n");
    public String stripAndTrimHeader(String message, int maxLength) {
        message = StringUtils.strip(StringUtils.trimToEmpty(message));
        Matcher matcher = CR_OR_LF.matcher(message);
        if (matcher.find()) {
            message = message.substring(0, matcher.start());
        }

        if (maxLength < 0) {
            return message;
        }

        return StringUtils.truncate(message, maxLength);
    }

}
