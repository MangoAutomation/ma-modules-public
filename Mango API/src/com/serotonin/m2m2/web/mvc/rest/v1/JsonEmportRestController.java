/*
   Copyright (C) 2016 Infinite Automation Systems Inc. All rights reserved.
   @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mangoApi.websocket.JsonConfigImportWebSocketDefinition;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonTypeReader;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
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
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.config.JsonConfigImportWebSocketHandler;
import com.serotonin.util.ProgressiveTask;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="JSON Emport", description="Import/Export JSON Configurations")
@RestController
@RequestMapping("/v1/json-emport")
public class JsonEmportRestController extends MangoRestController{
	
	private static Log LOG = LogFactory.getLog(JsonEmportRestController.class);
	
	//Lock to ensure only 1 import at a time can run
	private Object importLock = new Object();
	private ImportBackgroundTask task;
	private final JsonConfigImportWebSocketHandler websocket;

	public JsonEmportRestController(){
		this.websocket = (JsonConfigImportWebSocketHandler)ModuleRegistry.getWebSocketHandlerDefinition(JsonConfigImportWebSocketDefinition.TYPE_NAME).getHandlerInstance();
	}
	
	@ApiOperation(value = "Import Configuration", notes="Submit the request and get a URL for the results")
	@RequestMapping(
			method = {RequestMethod.POST},
			produces = {"application/json"},
			consumes = {"application/json"}
	)
	public ResponseEntity<Void> importConfiguration(
			HttpServletRequest request,
			@RequestBody(required=true) String config){
		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.ACCEPTED);
		User user = this.checkUser(request, result);
		if(result.isOk()) {
			if(!user.isAdmin()){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
			
			synchronized(importLock){
				if(task != null){
					result.addRestMessage(HttpStatus.CONFLICT, new TranslatableMessage("emport.importProgress"));
					return result.createResponseEntity();
				}
				JsonTypeReader reader = new JsonTypeReader(config);
		        try {
		            JsonValue value = reader.read();
		            if (value instanceof JsonObject) {
		                JsonObject root = value.toJsonObject();
		                task = new ImportBackgroundTask(root, Common.getTranslations(), user, websocket);
		            }
		            else {
		            	result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("emport.invalidImportData")));
		            }
		        }
		        catch (ClassCastException e) {
		        	LOG.error(e.getMessage(), e);
	            	result.addRestMessage(new RestMessage(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("emport.parseError", e.getMessage())));
		        }
		        catch (TranslatableJsonException e) {
		        	LOG.error(e.getMessage(), e);
	            	result.addRestMessage(new RestMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMsg()));
		        }
		        catch (IOException | JsonException e) {
		        	LOG.error(e.getMessage(), e);
	            	result.addRestMessage(new RestMessage(HttpStatus.INTERNAL_SERVER_ERROR, new TranslatableMessage("emport.parseError", e.getMessage())));
		        }
			}
		}
        return result.createResponseEntity();
	}

	/**
	 * Class to import a JSON config in the background and 
	 * publish the results to a websocket
	 * 
	 * @author Terry Packer
	 */
	public class ImportBackgroundTask extends ProgressiveTask {

		private final ImportContext importContext;
	    private final User user;

	    private final List<Importer> importers = new ArrayList<Importer>();
	    private final List<ImportItem> importItems = new ArrayList<ImportItem>();
	    private final JsonConfigImportWebSocketHandler websocket;
	    private float progress = 0f;
	    //Chunk of progress for each importer
	    private float progressChunk;
	    
	    public ImportBackgroundTask(JsonObject root, Translations translations, User user, JsonConfigImportWebSocketHandler websocket) {
	    	super(websocket);
	    	
	    	this.websocket = websocket;
	    	this.websocket.setTask(this);
	    	
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

	    @Override
	    protected void runImpl() {
	        try {
	        	declareProgress(this.progress + 0.1f);
	            BackgroundContext.set(user);
	            if (!importers.isEmpty()) {
	            	
	                if (importerIndex >= importers.size()) {
	                    // A run through the importers has been completed.
	                    if (importerSuccess) {
	                        // If there were successes with the importers and there are still more to do, run through 
	                        // them again.
	                        importerIndex = 0;
	                        importerSuccess = false;
	                    }
	                    else {
	                        // There are importers left in the list, but there were no successful imports in the last run
	                        // of the set. So, all that is left is stuff that will always fail. Copy the validation 
	                        // messages to the context for each.
	                        for (Importer importer : importers){
	                            importer.copyMessages();
	                        }
	                        importers.clear();
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
	            //Our progress is 100 - chunk*importersLeft
	            int importItemsLeft = 0;
	            for(ImportItem item : importItems)
	            	if(!item.isComplete())
	            		importItemsLeft++;
	            declareProgress(100f - progressChunk*((float)importers.size() + (float)importItemsLeft));
	            synchronized(importLock){
	            	task = null;
	            	this.websocket.setTask(null);
	            }
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
}
