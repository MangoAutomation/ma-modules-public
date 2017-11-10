/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfigDao;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.GenericRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResource;
import com.infiniteautomation.mango.rest.v2.util.MangoRestTemporaryResourceContainer;
import com.infiniteautomation.mangoApi.websocket.JsonConfigImportWebSocketDefinition;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonTypeWriter;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventHandlerDao;
import com.serotonin.m2m2.db.dao.JsonDataDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessMessage;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.EmportDwr;
import com.serotonin.m2m2.web.dwr.emport.ImportTask;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonConfigImportStateEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.config.JsonConfigImportWebSocketHandler;
import com.serotonin.timer.RejectedTaskReason;
import com.serotonin.util.ProgressiveTaskListener;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value="JSON Emport", description="Import/Export JSON Configurations")
@RestController
@RequestMapping("/v2/json-emport")
public class JsonEmportV2Controller extends AbstractMangoRestV2Controller {

    private final MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources;
    private final JsonConfigImportWebSocketHandler websocket;
    
    
    public JsonEmportV2Controller(){
        this.websocket = (JsonConfigImportWebSocketHandler)ModuleRegistry.getWebSocketHandlerDefinition(JsonConfigImportWebSocketDefinition.TYPE_NAME).getHandlerInstance();
        this.websocket.setController(this);
        this.importStatusResources = new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
    }
    
    @PreAuthorize("isAdmin()")
    @ApiOperation(
            value = "Get Status For Import",
            notes = "",
            response=ImportStatusProvider.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/import/{id}")
    public ResponseEntity<ImportStatusProvider> getImportStatus(
            @ApiParam(value = "Valid Resource ID", required = true,
                    allowMultiple = false) @PathVariable String id,
            HttpServletRequest request) {
 
        ImportStatusProvider provider = this.importStatusResources.get(id);
        if (provider == null) {
            throw new NotFoundRestException();
        } else {
            return new ResponseEntity<>(provider, HttpStatus.OK);
        }
    }
    
    @PreAuthorize("isAdmin()")
    @ApiOperation(
            value = "Update an Import in Progress",
            notes = "Currently only cancel action is supported"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/import/{resourceId}")
    public ResponseEntity<Void> updateImport(
            HttpServletRequest request, 
            @RequestBody(required=true) JsonEmportControlModel model, 
            @ApiParam(value="Resource id", required=true, allowMultiple=false)
            @PathVariable String resourceId,
            UriComponentsBuilder builder) throws RestValidationFailedException {

        ImportStatusProvider provider = this.importStatusResources.get(resourceId);
        if(provider == null){
            throw new NotFoundRestException();
        }else{
            //Currently our only action
            if(model.isCancel()){
                provider.cancel();
            }
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Upload 1 configuration json file", notes = "Files should only contain the json object to be imported")
    @RequestMapping(method = RequestMethod.POST, value = "/upload-file", consumes={"multipart/form-data", "multipart/form-data;boundary=-----SWAG_BOUND"})
    public ResponseEntity<ImportStatusProvider> uploadConfigurationFile(
            MultipartHttpServletRequest multipartRequest,
            UriComponentsBuilder builder,
            HttpServletRequest request,
            @ApiParam(value = "timeout for Status Resource to Expire, defaults to 5 minutes", required = false, allowMultiple = false)
            @RequestParam(value="timeout", required=false) Long timeout,
            @AuthenticationPrincipal User user) throws RestValidationFailedException, IOException, JsonException {

        Map<String, MultipartFile> map = multipartRequest.getFileMap();
        if(map.size() != 1)
            throw new BadRequestException(new TranslatableMessage("rest.error.oneFileOnly"));
        
        Iterator<String> itr =  multipartRequest.getFileNames();
        MultipartFile file = multipartRequest.getFile(itr.next());
        
        if (!file.isEmpty()) {
            JsonReader jr = new JsonReader(Common.JSON_CONTEXT, new String(file.getBytes()));
            JsonObject jo = jr.read(JsonObject.class);
            
            String resourceId = importStatusResources.generateResourceId();
            ImportStatusProvider statusProvider = new ImportStatusProvider(importStatusResources, resourceId, websocket, timeout, jo, user);
            
            //Setup the Temporary Resource
            this.importStatusResources.put(resourceId, statusProvider);
            URI location = builder.path("/v2/json-emport/import/{id}").buildAndExpand(resourceId).toUri();
            return getResourceCreated(statusProvider, location);
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.noFileProvided"));
        }
    }
    
    @PreAuthorize("isAdmin()")
    @ApiOperation(value = "Import Configuration", notes="Submit the request and get a URL for the results")
    @RequestMapping(method = {RequestMethod.POST})
    public ResponseEntity<ImportStatusProvider> importConfiguration(
            HttpServletRequest request,
            UriComponentsBuilder builder,
            @ApiParam(value = "Optional timeout for resource to expire, defaults to 5 minutes", required = false, allowMultiple = false)
            @RequestParam(value="timeout", required=false) Long timeout,
            @RequestBody(required=true) JsonValue config,
            @AuthenticationPrincipal User user){
        
        if (config instanceof JsonObject) {
            //Setup the Temporary Resource
            String resourceId = importStatusResources.generateResourceId();
            ImportStatusProvider statusProvider = new ImportStatusProvider(importStatusResources, resourceId, websocket, timeout, config.toJsonObject(), user);
            this.importStatusResources.put(resourceId, statusProvider);
            URI location = builder.path("/v2/json-emport/import/{id}").buildAndExpand(resourceId).toUri();
            return getResourceCreated(statusProvider, location);
        }
        else {
            throw new GenericRestException(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.invalidImportData"));
        }
    }

    @PreAuthorize("isAdmin()")
    @ApiOperation(
            value = "List Exportable Elements",
            notes = "Provided as parameters to choose what to export",
            response=String.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public ResponseEntity<List<StringStringPair>> listExportElements(HttpServletRequest request) {
        
        List<StringStringPair> elements = new ArrayList<StringStringPair>();
        elements.add(new StringStringPair("header.dataSources", EmportDwr.DATA_SOURCES));
        elements.add(new StringStringPair("header.dataPoints", EmportDwr.DATA_POINTS));
        elements.add(new StringStringPair("header.users", EmportDwr.USERS));
        elements.add(new StringStringPair("header.mailingLists", EmportDwr.MAILING_LISTS));
        elements.add(new StringStringPair("header.publishers", EmportDwr.PUBLISHERS));
        elements.add(new StringStringPair("header.eventHandlers", EmportDwr.EVENT_HANDLERS));
        elements.add(new StringStringPair("header.pointHierarchy", EmportDwr.POINT_HIERARCHY));
        elements.add(new StringStringPair("header.systemSettings", EmportDwr.SYSTEM_SETTINGS));
        elements.add(new StringStringPair("header.pointPropertyTemplates", EmportDwr.TEMPLATES));
        elements.add(new StringStringPair("header.virtualSerialPorts", EmportDwr.VIRTUAL_SERIAL_PORTS));
        elements.add(new StringStringPair("header.jsonData", EmportDwr.JSON_DATA));
        
        for (EmportDefinition def : ModuleRegistry.getDefinitions(EmportDefinition.class)) {
            elements.add(new StringStringPair(def.getDescriptionKey(), def.getElementId()));
        }
        return new ResponseEntity<>(elements, HttpStatus.OK);
    }
    
    @PreAuthorize("isAdmin()")
    @ApiOperation(
            value = "Export Configuration",
            notes = "",
            response=JsonValue.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<JsonValue> export(
            @ApiParam(value = "Elements To Export", required = true, allowMultiple = true)
            @RequestParam(name="exportElements", required=true)
            String[] exportElements,
            HttpServletRequest request) throws JsonException {
        //Do the Export
        Map<String,Object> data = createExportData(exportElements);
        JsonTypeWriter writer = new JsonTypeWriter(Common.JSON_CONTEXT);
        return new ResponseEntity<>(writer.writeObject(data), HttpStatus.OK);
    }
    
    /**
     * Status Provider for Imports
     * 
     * @author Terry Packer
     */
    public class ImportStatusProvider extends MangoRestTemporaryResource<ImportStatusProvider> implements ProgressiveTaskListener{

        private final ImportTask task;
        private final JsonConfigImportWebSocketHandler websocket;
        private final long expirationMs;
        
        //Our Status Parameters for the Model
        private final String username;
        private final Date start;
        private Date finish;
        private JsonConfigImportStateEnum state;
        private float progress;
        
        public ImportStatusProvider(MangoRestTemporaryResourceContainer<ImportStatusProvider> container, String resourceId, JsonConfigImportWebSocketHandler websocket, Long expirationMs, JsonObject root,  User user){
            super(resourceId, container);
            this.websocket = websocket;
            if(expirationMs == null)
                this.expirationMs = 300000L;
            else
                this.expirationMs = expirationMs;
            this.username = user.getUsername();
            this.start = new Date();
            this.state = JsonConfigImportStateEnum.RUNNING;
            this.progress = 0.0f;
            this.task = new ImportTask(root, Common.getTranslations(), user, this);
        }
        
        @JsonGetter
        public List<RestValidationMessage> getValidationMessages() {
            List<ProcessMessage> messages = this.task.getResponse().getMessages();
            List<RestValidationMessage> validation = new ArrayList<RestValidationMessage>();

            for (ProcessMessage message : messages) {
                if (!StringUtils.isEmpty(message.getContextKey())) {
                    switch (message.getLevel()) {
                        case info:
                            validation.add(new RestValidationMessage(message.getContextualMessage(),
                                    RestMessageLevel.INFORMATION, message.getContextKey()));
                            break;
                        case warning:
                            validation.add(new RestValidationMessage(message.getContextualMessage(),
                                    RestMessageLevel.WARNING, message.getContextKey()));
                            break;
                        case error:
                            validation.add(new RestValidationMessage(message.getContextualMessage(),
                                    RestMessageLevel.ERROR, message.getContextKey()));
                            break;
                    }
                }
            }
            return validation;
        }
        
        @JsonGetter
        public List<String> getGenericMessages() {
            List<ProcessMessage> messages = this.task.getResponse().getMessages();
            List<String> generic = new ArrayList<String>();
            Translations translations = Common.getTranslations();

            for (ProcessMessage message : messages) {
                if (StringUtils.isEmpty(message.getContextKey())) {
                    // Generic Message
                    generic.add(message.getGenericMessage().translate(translations));
                }
            }
            return generic;
        }
        
        @JsonGetter
        public Date getStart() {
            return start;
        }
        
        @JsonGetter
        public Date getFinish() {
            return finish;
        }
        
        @JsonGetter
        public JsonConfigImportStateEnum getState() {
            return state;
        }
        @JsonGetter
        public float getProgress() {
            return progress;
        }
        @JsonGetter
        public String getOwner() {
            return username;
        }
        
        public void cancel(){
            this.task.cancel();
        }
        
        @Override
        public void progressUpdate(float progress) {
            this.progress = progress;
            this.websocket.notify(this);
        }

        @Override
        public void taskCancelled() {
            this.progress = 0.0f;
            this.state = JsonConfigImportStateEnum.CANCELLED;
            this.websocket.notify(this);
            schedule(new Date(Common.timer.currentTimeMillis() + expirationMs));
        }

        @Override
        public void taskCompleted() {
            this.finish = new Date();
            this.progress = 100.0f;
            this.state = JsonConfigImportStateEnum.COMPLETED;
            this.websocket.notify(this);
            schedule(new Date(Common.timer.currentTimeMillis() + expirationMs));
        }

        /* (non-Javadoc)
         * @see com.serotonin.util.ProgressiveTaskListener#taskRejected(com.serotonin.timer.RejectedTaskReason)
         */
        @Override
        public void taskRejected(RejectedTaskReason reason) {
            this.finish = new Date();
            this.progress = 100.0f;
            this.state = JsonConfigImportStateEnum.REJECTED;
            this.websocket.notify(this);
            schedule(new Date(Common.timer.currentTimeMillis() + expirationMs));
        }       
    }
    
    /**
     * Cancel a given task if it exists
     * 
     * @param resourceId
     */
    public void cancelImport(String resourceId) {
        ImportStatusProvider provider = this.importStatusResources.get(resourceId);
        if(provider != null)
            provider.cancel();
    }
    
    /**
     * Get the export data
     * @param exportElements
     * @return
     */
    private Map<String, Object> createExportData(String[] exportElements){
         Map<String, Object> data = new LinkedHashMap<>();

            if (ArrayUtils.contains(exportElements, EmportDwr.DATA_SOURCES))
                data.put(EmportDwr.DATA_SOURCES, DataSourceDao.instance.getDataSources());
            if (ArrayUtils.contains(exportElements, EmportDwr.DATA_POINTS))
                data.put(EmportDwr.DATA_POINTS, DataPointDao.instance.getDataPoints(null, true));
            if (ArrayUtils.contains(exportElements, EmportDwr.USERS))
                data.put(EmportDwr.USERS, UserDao.instance.getUsers());
            if (ArrayUtils.contains(exportElements, EmportDwr.MAILING_LISTS))
                data.put(EmportDwr.MAILING_LISTS, MailingListDao.instance.getMailingLists());
            if (ArrayUtils.contains(exportElements, EmportDwr.PUBLISHERS))
                data.put(EmportDwr.PUBLISHERS, PublisherDao.instance.getPublishers());
            if (ArrayUtils.contains(exportElements, EmportDwr.EVENT_HANDLERS))
                data.put(EmportDwr.EVENT_HANDLERS, EventHandlerDao.instance.getEventHandlers());
            if (ArrayUtils.contains(exportElements, EmportDwr.POINT_HIERARCHY))
                data.put(EmportDwr.POINT_HIERARCHY, DataPointDao.instance.getPointHierarchy(true).getRoot().getSubfolders());
            if (ArrayUtils.contains(exportElements, EmportDwr.SYSTEM_SETTINGS))
                data.put(EmportDwr.SYSTEM_SETTINGS, SystemSettingsDao.instance.getAllSystemSettingsAsCodes());
            if (ArrayUtils.contains(exportElements, EmportDwr.TEMPLATES))
                data.put(EmportDwr.TEMPLATES, TemplateDao.instance.getAll());
            if (ArrayUtils.contains(exportElements, EmportDwr.VIRTUAL_SERIAL_PORTS))
                data.put(EmportDwr.VIRTUAL_SERIAL_PORTS, VirtualSerialPortConfigDao.instance.getAll());
            if (ArrayUtils.contains(exportElements, EmportDwr.JSON_DATA))
                data.put(EmportDwr.JSON_DATA, JsonDataDao.instance.getAll());
            
            
            for (EmportDefinition def : ModuleRegistry.getDefinitions(EmportDefinition.class)) {
                if (ArrayUtils.contains(exportElements, def.getElementId()))
                    data.put(def.getElementId(), def.getExportData());
            }
            
            return data;
    }
    
}
