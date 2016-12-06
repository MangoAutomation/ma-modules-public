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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.type.JsonArray;
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
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.util.BackgroundContext;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.EmportDwr;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;
import com.serotonin.m2m2.web.dwr.emport.ImportItem;
import com.serotonin.m2m2.web.dwr.emport.Importer;
import com.serotonin.m2m2.web.dwr.emport.importers.DataPointImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.DataSourceImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.EventHandlerImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.JsonDataImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.MailingListImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.PointHierarchyImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.PublisherImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.SystemSettingsImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.TemplateImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.UserImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.VirtualSerialPortImporter;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonConfigImportStateEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonConfigImportStatusModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.emport.JsonEmportControlModel;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.config.JsonConfigImportWebSocketHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.util.MangoRestTemporaryResource;
import com.serotonin.m2m2.web.mvc.rest.v1.util.MangoRestTemporaryResourceContainer;
import com.serotonin.util.ProgressiveTask;
import com.serotonin.util.ProgressiveTaskListener;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
//import com.wordnik.swagger.annotations.ApiResponse;
//import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @author Terry Packer
 *
 */
@Api(value="JSON Emport", description="Import/Export JSON Configurations")
@RestController
@RequestMapping("/v1/json-emport")
public class JsonEmportRestController extends MangoRestController{
	
	private static Log LOG = LogFactory.getLog(JsonEmportRestController.class);
	
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
	@RequestMapping(method = RequestMethod.PUT, value = "/import/{id}", produces={"application/json"}, consumes={"application/json"})
    public ResponseEntity<Void> putPointValue(
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
    		@RequestParam(value="expiration", required=false) DateTime expiration,
    		
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
			consumes = {"application/json"}
	)
	public ResponseEntity<Void> importConfiguration(
			HttpServletRequest request,
			UriComponentsBuilder builder,
    		@ApiParam(value = "Optional Date for Status Resource to Expire, defaults to 5 minutes", required = false, allowMultiple = false)
    		@RequestParam(value="expiration", required=false) DateTime expiration,
    		
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
    public ResponseEntity<List<String>> listExportElements(HttpServletRequest request) {
		RestProcessResult<List<String>> result = new RestProcessResult<List<String>>(HttpStatus.OK);
		
		User user = this.checkUser(request, result);
        if(result.isOk()){
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}else{
				List<String> elements = new ArrayList<String>();
				elements.add(EmportDwr.DATA_SOURCES);
				elements.add(EmportDwr.DATA_POINTS);
				elements.add(EmportDwr.USERS);
				elements.add(EmportDwr.MAILING_LISTS);
				elements.add(EmportDwr.PUBLISHERS);
				elements.add(EmportDwr.EVENT_HANDLERS);
				elements.add(EmportDwr.POINT_HIERARCHY);
				elements.add(EmportDwr.SYSTEM_SETTINGS);
				elements.add(EmportDwr.TEMPLATES);
				elements.add(EmportDwr.VIRTUAL_SERIAL_PORTS);
				elements.add(EmportDwr.JSON_DATA);
				
				for (EmportDefinition def : ModuleRegistry.getDefinitions(EmportDefinition.class)) {
		            elements.add(def.getElementId());
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

		private final ImportBackgroundTask task;
		private final JsonConfigImportWebSocketHandler websocket;
		private final JsonConfigImportStatusModel model;
		
		public ImportStatusProvider(JsonObject root, String resourceId, User user, JsonConfigImportWebSocketHandler websocket){
			super(resourceId);
			this.websocket = websocket;
			this.model = new JsonConfigImportStatusModel(resourceId, user.getUsername(), new Date());
			this.task = new ImportBackgroundTask(root, Common.getTranslations(), user, this);
		}
		
		/**
		 * @return
		 */
		@Override
		public JsonConfigImportStatusModel createModel() {
			return model;
		}

		public void cancel(){
			this.task.cancel();
		}
		
		@Override
		public void progressUpdate(float progress) {
			this.model.setProgress(progress);
			this.websocket.notify(this.model);
		}

		@Override
		public void taskCancelled() {
			this.model.setProgress(0.0f);
			this.model.setState(JsonConfigImportStateEnum.CANCELLED);
			this.websocket.notify(this.model);
		}

		@Override
		public void taskCompleted() {
			this.model.setFinish(new Date());
			this.model.setProgress(100.0f);
			this.model.setState(JsonConfigImportStateEnum.COMPLETED);
			this.model.updateMessages(this.task.getResponse());
			this.websocket.notify(this.model);
		}
	}
	
	/**
	 * Class to import a JSON config in the background and 
	 * publish the results to a websocket
	 * 
	 * @author Terry Packer
	 */
	public class ImportBackgroundTask extends ProgressiveTask{

		private final ImportContext importContext;
	    private final User user;

	    private final List<Importer> importers = new ArrayList<Importer>();
	    private final List<ImportItem> importItems = new ArrayList<ImportItem>();
	    
	    private float progress = 0f;
	    //Chunk of progress for each importer
	    private float progressChunk;
	    
	    public ImportBackgroundTask(JsonObject root, Translations translations, User user, ProgressiveTaskListener listener) {
	    	super(listener);
	    	
	        JsonReader reader = new JsonReader(Common.JSON_CONTEXT, root);
	        this.importContext = new ImportContext(reader, new ProcessResult(), translations);
	        this.user = user;

	        for (JsonValue jv : nonNullList(root, EmportDwr.USERS))
	            addImporter(new UserImporter(jv.toJsonObject()));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.DATA_SOURCES))
	            addImporter(new DataSourceImporter(jv.toJsonObject()));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.DATA_POINTS))
	            addImporter(new DataPointImporter(jv.toJsonObject()));
	        
	        JsonArray phJson = root.getJsonArray(EmportDwr.POINT_HIERARCHY);
	        if(phJson != null)
	        	addImporter(new PointHierarchyImporter(phJson));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.MAILING_LISTS))
	            addImporter(new MailingListImporter(jv.toJsonObject()));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.PUBLISHERS))
	            addImporter(new PublisherImporter(jv.toJsonObject()));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.EVENT_HANDLERS))
	            addImporter(new EventHandlerImporter(jv.toJsonObject()));
	        
	        JsonObject obj = root.getJsonObject(EmportDwr.SYSTEM_SETTINGS);
	        if(obj != null)
	            addImporter(new SystemSettingsImporter(obj));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.TEMPLATES))
	            addImporter(new TemplateImporter(jv.toJsonObject()));
	        
	        for (JsonValue jv : nonNullList(root, EmportDwr.VIRTUAL_SERIAL_PORTS))
	            addImporter(new VirtualSerialPortImporter(jv.toJsonObject()));
	        
	        for(JsonValue jv : nonNullList(root, EmportDwr.JSON_DATA))
	        	addImporter(new JsonDataImporter(jv.toJsonObject()));
	        
	        for (EmportDefinition def : ModuleRegistry.getDefinitions(EmportDefinition.class)) {
	            ImportItem importItem = new ImportItem(def, root.get(def.getElementId()));
	            importItems.add(importItem);
	        }
	        
	        this.progressChunk = 100f/((float)importers.size() + (float)importItems.size());
	        
	        Common.timer.execute(this);
	    }

	    private List<JsonValue> nonNullList(JsonObject root, String key) {
	        JsonArray arr = root.getJsonArray(key);
	        if (arr == null)
	            arr = new JsonArray();
	        return arr;
	    }

	    private void addImporter(Importer importer) {
	        importer.setImportContext(importContext);
	        importer.setImporters(importers);
	        importers.add(importer);
	    }

	    public ProcessResult getResponse() {
	        return importContext.getResult();
	    }

	    private int importerIndex;
	    private boolean importerSuccess;
	    private boolean importedItems;

	    @Override
	    protected void runImpl() {
	        try {
	            BackgroundContext.set(user);
	            if (!importers.isEmpty()) {
	            	
	                if (importerIndex >= importers.size()) {
	                    // A run through the importers has been completed.
	                    if (importerSuccess) {
	                        // If there were successes with the importers and there are still more to do, run through 
	                        // them again.
	                        importerIndex = 0;
	                        importerSuccess = false;
	                    } else if(!importedItems) {
	        	            try {
	                            for (ImportItem importItem : importItems) {
	                                if (!importItem.isComplete()) {
	                                    importItem.importNext(importContext);
	                                    return;
	                                }
	                            }
	                            importedItems = true;   // We may have imported a dependency in a module
	                            importerIndex = 0;
	                        }
	                        catch (Exception e) {
	                            addException(e);
	                        }
	                    } else {
	                        // There are importers left in the list, but there were no successful imports in the last run
	                        // of the set. So, all that is left is stuff that will always fail. Copy the validation 
	                        // messages to the context for each.
	                        for (Importer importer : importers){
	                            importer.copyMessages();
	                        }
	                        importers.clear();
	                        completed = true;
	                        return;
	                    }
	                }

	                // Run the next importer
	                Importer importer = importers.get(importerIndex);
	                try {
	                    importer.doImport();
	                    if (importer.success()) {
	                        // The import was successful. Note the success and remove the importer from the list.
	                        importerSuccess = true;
	                        importers.remove(importerIndex);
	                    }
	                    else{
	                        // The import failed. Leave it in the list since the run of another importer
	                        // may resolved the problem.
	                        importerIndex++;
	                    }
	                }
	                catch (Exception e) {
	                    // Uh oh...
	                	LOG.error(e.getMessage(),e);
	                    addException(e);
	                    importers.remove(importerIndex);
	                }

	                return;
	            }

	            // Run the import items.
	            try {
	                for (ImportItem importItem : importItems) {
	                    if (!importItem.isComplete()) {
	                        importItem.importNext(importContext);
	                        return;
	                    }
	                }

	                completed = true;
	            }
	            catch (Exception e) {
	                addException(e);
	            }
	        }
	        finally {
	            BackgroundContext.remove();
	            //Compute progress, but only declare if we are < 100 since we will declare 100 when done
	            //Our progress is 100 - chunk*importersLeft
	            int importItemsLeft = 0;
	            for(ImportItem item : importItems)
	            	if(!item.isComplete())
	            		importItemsLeft++;
	            this.progress = 100f - progressChunk*((float)importers.size() + (float)importItemsLeft);
	            if(progress < 100f)
	            	declareProgress(this.progress);
	        }
	    }

	    private void addException(Exception e) {
	        String msg = e.getMessage();
	        Throwable t = e;
	        while ((t = t.getCause()) != null)
	            msg += ", " + importContext.getTranslations().translate("emport.causedBy") + " '" + t.getMessage() + "'";
	        //We were missing NPE and others without a msg
	        if(msg == null)
	        	msg = e.getClass().getCanonicalName();
	        importContext.getResult().addGenericMessage("common.default", msg);
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
	            data.put(EmportDwr.DATA_SOURCES, new DataSourceDao().getDataSources());
	        if (ArrayUtils.contains(exportElements, EmportDwr.DATA_POINTS))
	            data.put(EmportDwr.DATA_POINTS, new DataPointDao().getDataPoints(null, true));
	        if (ArrayUtils.contains(exportElements, EmportDwr.USERS))
	            data.put(EmportDwr.USERS, new UserDao().getUsers());
	        if (ArrayUtils.contains(exportElements, EmportDwr.MAILING_LISTS))
	            data.put(EmportDwr.MAILING_LISTS, new MailingListDao().getMailingLists());
	        if (ArrayUtils.contains(exportElements, EmportDwr.PUBLISHERS))
	            data.put(EmportDwr.PUBLISHERS, PublisherDao.instance.getPublishers());
	        if (ArrayUtils.contains(exportElements, EmportDwr.EVENT_HANDLERS))
	            data.put(EmportDwr.EVENT_HANDLERS, EventHandlerDao.instance.getEventHandlers());
	        if (ArrayUtils.contains(exportElements, EmportDwr.POINT_HIERARCHY))
	            data.put(EmportDwr.POINT_HIERARCHY, new DataPointDao().getPointHierarchy(true).getRoot().getSubfolders());
	        if (ArrayUtils.contains(exportElements, EmportDwr.SYSTEM_SETTINGS))
	            data.put(EmportDwr.SYSTEM_SETTINGS, new SystemSettingsDao().getSystemSettingsForExport());
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
