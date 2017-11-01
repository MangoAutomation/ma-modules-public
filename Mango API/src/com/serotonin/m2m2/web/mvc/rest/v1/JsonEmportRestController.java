/*
   Copyright (C) 2016 Infinite Automation Systems Inc. All rights reserved.
   @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.io.serial.virtual.VirtualSerialPortConfigDao;
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
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessageLevel;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestValidationMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonConfigImportStateEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonConfigImportStatusModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.config.JsonConfigImportWebSocketHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.util.MangoRestTemporaryResource;
import com.serotonin.m2m2.web.mvc.rest.v1.util.MangoRestTemporaryResourceContainer;
import com.serotonin.timer.RejectedTaskReason;
import com.serotonin.util.ProgressiveTaskListener;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="JSON Emport", description="Import/Export JSON Configurations")
@RestController
@RequestMapping("/v1/json-emport")
public class JsonEmportRestController extends MangoRestController{
	
	private final MangoRestTemporaryResourceContainer<ImportStatusProvider> importStatusResources;
	private final JsonConfigImportWebSocketHandler websocket;
	
	
	public JsonEmportRestController(){
		this.websocket = (JsonConfigImportWebSocketHandler)ModuleRegistry.getWebSocketHandlerDefinition(JsonConfigImportWebSocketDefinition.TYPE_NAME).getHandlerInstance();
		this.websocket.setController(this);
		this.importStatusResources = new MangoRestTemporaryResourceContainer<ImportStatusProvider>("IMPORT_");
	}
	
	@ApiOperation(
			value = "Get Status For Import",
			notes = "",
			response=JsonConfigImportStatusModel.class
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/import/{id}")
    public ResponseEntity<JsonConfigImportStatusModel> getImportStatus(
    		@ApiParam(value = "Valid Resource ID", required = true, allowMultiple = false)
    		@PathVariable String id, HttpServletRequest request) {
		
		RestProcessResult<JsonConfigImportStatusModel> result = new RestProcessResult<JsonConfigImportStatusModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
        	ImportStatusProvider provider = this.importStatusResources.get(id);
        	if(provider == null){
        		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
        	}else{
        		return result.createResponseEntity((JsonConfigImportStatusModel) provider.getModel());
        	}
        }
		return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Update an Import in Progress",
			notes = "Currently only cancel action is supported"
			)
	@RequestMapping(method = RequestMethod.PUT, value = "/import/{resourceId}", produces={"application/json"}, consumes={"application/json"})
    public ResponseEntity<Void> updateImport(
    		HttpServletRequest request, 
    		@RequestBody(required=true) JsonEmportControlModel model, 
    		@ApiParam(value="Resource id", required=true, allowMultiple=false)
    		@PathVariable String resourceId, 
    		UriComponentsBuilder builder) throws RestValidationFailedException {
		
		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
		
		User user = this.checkUser(request, result);
		if(result.isOk()){
			
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
        	ImportStatusProvider provider = this.importStatusResources.get(resourceId);
        	if(provider == null){
        		result.addRestMessage(getDoesNotExistMessage());
        	}else{
        		//Currently our only action
        		if(model.isCancel()){
        			provider.cancel();
        			result.addRestMessage(HttpStatus.ACCEPTED, new TranslatableMessage("emport.importCancelled"));
        		}
            	URI location = builder.path("/v1/json-emport/import/{id}").buildAndExpand(resourceId).toUri();
            	result.addHeader("Location", location.toString());
        	}
        	return result.createResponseEntity();
		}else{
			return result.createResponseEntity();
		}
    }

	@ApiOperation(value = "Upload a configuration json file", notes = "Files should only contain the json object to be imported")
//	@ApiResponses({
//			@ApiResponse(code = 201, message = "Configuration Uploaded", response=Map.class),
//			@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class)
//			}) //boundary=----------1111111111 useful in testing through swagger
	@RequestMapping(method = RequestMethod.POST, value = "/upload-file", consumes={"multipart/form-data", "multipart/form-data;boundary=-----SWAG_BOUND"}, produces={"application/json"})
    public ResponseEntity<Void> uploadConfigurationFile(
    		MultipartHttpServletRequest multipartRequest,
    		UriComponentsBuilder builder,
    		HttpServletRequest request,
    		@ApiParam(value = "Optional Date for Status Resource to Expire, defaults to 5 minutes", required = false, allowMultiple = false)
    		@RequestParam(value="expiration", required=false) @DateTimeFormat(iso=ISO.DATE_TIME) DateTime expiration,
    		
    		@ApiParam(value = "Time zone", required = false, allowMultiple = false)
            @RequestParam(value="timezone", required=false)
            String timezone) throws RestValidationFailedException {

		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.ACCEPTED);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		Iterator<String> itr =  multipartRequest.getFileNames();
    		while(itr.hasNext()){
    			
	            MultipartFile file = multipartRequest.getFile(itr.next());
	    		if (!file.isEmpty()) {
	                try {
	                	JsonReader jr = new JsonReader(Common.JSON_CONTEXT, new String(file.getBytes()));
	                	JsonObject jo = jr.read(JsonObject.class);
	                	
	                	if(expiration == null)
	                		expiration = new DateTime(System.currentTimeMillis() + 300000);
	                    if (timezone != null) {
	                        DateTimeZone zone = DateTimeZone.forID(timezone);
	                        expiration = expiration.withZone(zone);
	                    }
	                    
	                    //Setup the Temporary Resource
	                	String resourceId = importStatusResources.generateResourceId();
	                	this.importStatusResources.put(resourceId, new ImportStatusProvider(jo, resourceId, user, websocket), expiration.toDate());
	                	URI location = builder.path("/v1/json-emport/import/{id}").buildAndExpand(resourceId).toUri();
	                	result.addHeader("Location", location.toString());
	                } catch (Exception e) {
	                    result.addRestMessage(this.getInternalServerErrorMessage(e.getMessage()));
	                }
	            } else {
	                result.addRestMessage(this.getInternalServerErrorMessage("No file provided"));
	            }
    		}
    		return result.createResponseEntity();
    	}
    	return result.createResponseEntity();
	}
	
	@ApiOperation(value = "Import Configuration", notes="Submit the request and get a URL for the results")
	@RequestMapping(
			method = {RequestMethod.POST},
			produces = {"application/json"},
			consumes = {"application/json", "application/sero-json"}
	)
	public ResponseEntity<Void> importConfiguration(
			HttpServletRequest request,
			UriComponentsBuilder builder,
    		@ApiParam(value = "Optional Date for Status Resource to Expire, defaults to 5 minutes", required = false, allowMultiple = false)
    		@RequestParam(value="expiration", required=false) @DateTimeFormat(iso=ISO.DATE_TIME) DateTime expiration,
    		
    		@ApiParam(value = "Time zone", required = false, allowMultiple = false)
            @RequestParam(value="timezone", required=false)
            String timezone,
            
			@RequestBody(required=true) JsonValue config){
		
		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.ACCEPTED);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
            if (config instanceof JsonObject) {
            	if(expiration == null)
            		expiration = new DateTime(System.currentTimeMillis() + 300000);
            	// could also get the user's timezone if parameter was not supplied but probably
                // better not to for RESTfulness
                if (timezone != null) {
                    DateTimeZone zone = DateTimeZone.forID(timezone);
                    expiration = expiration.withZone(zone);
                }
                //Setup the Temporary Resource
            	String resourceId = importStatusResources.generateResourceId();
            	this.importStatusResources.put(resourceId, new ImportStatusProvider(config.toJsonObject(), resourceId, user, websocket), expiration.toDate());
            	URI location = builder.path("/v1/json-emport/import/{id}").buildAndExpand(resourceId).toUri();
            	result.addHeader("Location", location.toString());
            }
            else {
            	result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.invalidImportData")));
            }
		}
        return result.createResponseEntity();
	}


	@ApiOperation(
			value = "List Exportable Elements",
			notes = "Provided as parameters to choose what to export",
			response=String.class,
			responseContainer="List"
			)
	@RequestMapping(method = RequestMethod.GET, value = "/list", produces={"application/json"})
    public ResponseEntity<List<StringStringPair>> listExportElements(HttpServletRequest request) {
		RestProcessResult<List<StringStringPair>> result = new RestProcessResult<List<StringStringPair>>(HttpStatus.OK);
		
		User user = this.checkUser(request, result);
        if(result.isOk()){
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}else{
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
				return result.createResponseEntity(elements);
			}
        }
		
		return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Export Configuration",
			notes = "",
			response=JsonValue.class
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<JsonValue> export(
    		@ApiParam(value = "Elements To Export", required = true, allowMultiple = true)
    		@RequestParam(name="exportElements", required=true)
    		String[] exportElements,
    		
    		HttpServletRequest request) {
		
		RestProcessResult<JsonValue> result = new RestProcessResult<JsonValue>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
        	//Do the Export
			Map<String,Object> data = createExportData(exportElements);
			JsonTypeWriter writer = new JsonTypeWriter(Common.JSON_CONTEXT);
			try{
				return result.createResponseEntity(writer.writeObject(data));
			}catch(JsonException e){
				result.addRestMessage(this.getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
			}
        }
		return result.createResponseEntity();
	}
	
	/**
	 * Status Provider for Imports
	 * 
	 * @author Terry Packer
	 */
	public class ImportStatusProvider extends MangoRestTemporaryResource implements ProgressiveTaskListener{

		private final ImportTask task;
		private final JsonConfigImportWebSocketHandler websocket;
		//private final JsonConfigImportStatusModel model;
		
		//Our Status Parameters for the Model
		private final String username;
		private final String resourceId;
		private final Date start;
		private Date finish;
		private JsonConfigImportStateEnum state;
		private float progress;
		
		public ImportStatusProvider(JsonObject root, String resourceId, User user, JsonConfigImportWebSocketHandler websocket){
			super(resourceId);
			this.websocket = websocket;
			this.username = user.getUsername();
			this.resourceId = resourceId;
			this.start = new Date();
			this.state = JsonConfigImportStateEnum.RUNNING;
			this.progress = 0.0f;
			this.task = new ImportTask(root, Common.getTranslations(), user, this);
		}
		
		/**
		 * @return
		 */
		@Override
		public JsonConfigImportStatusModel createModel() {

			List<ProcessMessage> messages = this.task.getResponse().getMessages();
			
			List<RestValidationMessage> validation = new ArrayList<RestValidationMessage>();
			List<String> generic = new ArrayList<String>();
			Translations translations = Common.getTranslations();
			
			for(ProcessMessage message : messages){
				if(StringUtils.isEmpty(message.getContextKey())){
					//Generic Message
					generic.add(message.getGenericMessage().translate(translations));
				}else{
					switch(message.getLevel()){
					case info:
						validation.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.INFORMATION, message.getContextKey()));
						break;
					case warning:
						validation.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.WARNING, message.getContextKey()));
						break;
					case error:
						validation.add(new RestValidationMessage(message.getContextualMessage(), RestMessageLevel.ERROR, message.getContextKey()));
						break;
					}
				}
			}
			
			
			return new JsonConfigImportStatusModel(
					resourceId, username, start, finish, 
					progress, state, validation, generic);
		}

		public void cancel(){
			this.task.cancel();
		}
		
		@Override
		public void progressUpdate(float progress) {
			this.progress = progress;
			this.websocket.notify(createModel());
		}

		@Override
		public void taskCancelled() {
			this.progress = 0.0f;
			this.state = JsonConfigImportStateEnum.CANCELLED;
			this.websocket.notify(createModel());
		}

		@Override
		public void taskCompleted() {
			this.finish = new Date();
			this.progress = 100.0f;
			this.state = JsonConfigImportStateEnum.COMPLETED;
			this.websocket.notify(createModel());
		}

		/* (non-Javadoc)
		 * @see com.serotonin.util.ProgressiveTaskListener#taskRejected(com.serotonin.timer.RejectedTaskReason)
		 */
		@Override
		public void taskRejected(RejectedTaskReason reason) {
			this.finish = new Date();
			this.progress = 100.0f;
			this.state = JsonConfigImportStateEnum.REJECTED;
			this.websocket.notify(createModel());
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
