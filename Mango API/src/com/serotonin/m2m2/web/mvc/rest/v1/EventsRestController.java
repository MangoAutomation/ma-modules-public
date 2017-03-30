/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.db.query.ComparisonEnum;
import com.infiniteautomation.mango.db.query.SQLQueryColumn;
import com.infiniteautomation.mango.db.query.appender.ExportCodeColumnQueryAppender;
import com.infiniteautomation.mango.db.query.appender.GenericSQLColumnQueryAppender;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.EventInstanceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.TranslatableMessageModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventLevelSummaryModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * 
 * 
 * @author Terry Packer
 *
 */
@Api(value="Events", description="Events")
@RestController()
@RequestMapping("/v1/events")
public class EventsRestController extends MangoVoRestController<EventInstanceVO, EventInstanceModel, EventInstanceDao>{
	
	private static Log LOG = LogFactory.getLog(EventsRestController.class);
	
	public EventsRestController(){ 
		super(EventInstanceDao.instance);
		
		//Add in our mappings
		this.modelMap.put("eventType", "typeName");
		this.modelMap.put("referenceId1", "typeRef1");
		this.modelMap.put("referenceId2", "typeRef2");
		this.modelMap.put("dataPointId", "typeRef1");
		this.modelMap.put("active", "rtnTs");
		this.modelMap.put("acknowledged", "ackTs");
		
		this.appenders.put("alarmLevel", new ExportCodeColumnQueryAppender(AlarmLevels.CODES));
		
		//If we query on the member active
		this.appenders.put("active", new GenericSQLColumnQueryAppender(){

			@Override
			public void appendSQL(SQLQueryColumn column,
					StringBuilder selectSql, StringBuilder countSql,
					List<Object> selectArgs, List<Object> columnArgs,
					ComparisonEnum comparison) {
				
				if(columnArgs.size() == 0)
					return;
				
				//Hack to allow still querying on rtnTs as number
				if(!(columnArgs.get(0) instanceof Boolean))
					return;
				
				Boolean condition = (Boolean)columnArgs.get(0);
				if(condition){
					appendSQL(column.getName(), " IS ? ", selectSql, countSql);
					selectArgs.add(null);
				}else{
					appendSQL(column.getName(), " IS NOT ? ", selectSql, countSql);
					selectArgs.add(null);
				}
				
				appendSQL("AND evt.rtnApplicable", EQUAL_TO_SQL, selectSql, countSql);
				selectArgs.add("Y");
			}
		
		});	
		//If we query on the member acknowledged
		this.appenders.put("acknowledged", new GenericSQLColumnQueryAppender(){

			@Override
			public void appendSQL(SQLQueryColumn column,
					StringBuilder selectSql, StringBuilder countSql,
					List<Object> selectArgs, List<Object> columnArgs,
					ComparisonEnum comparison) {
				
				if(columnArgs.size() == 0)
					return;
				
				//Hack to allow querying on ackTs as number
				if(!(columnArgs.get(0) instanceof Boolean))
					return;
				
				Boolean condition = (Boolean)columnArgs.get(0);
				if(condition){
					appendSQL(column.getName(), " IS NOT ? ", selectSql, countSql);
					selectArgs.add(null);
				}else{
					appendSQL(column.getName(), " IS ? ", selectSql, countSql);
					selectArgs.add(null);
				}
			}
		
		});	
		
		//Ensure we are querying on Type of Data Point for dataPointId queries
		this.appenders.put("dataPointId", new GenericSQLColumnQueryAppender(){

			@Override
			public void appendSQL(SQLQueryColumn column,
					StringBuilder selectSql, StringBuilder countSql,
					List<Object> selectArgs, List<Object> columnArgs,
					ComparisonEnum comparison) {
				
				selectSql.append(" typeName = 'DATA_POINT' AND typeRef1 = ? ");
				countSql.append(" typeName = 'DATA_POINT' AND typeRef1 = ? ");
				selectArgs.add(columnArgs.get(0));
			}
		});
	}

	
	@ApiOperation(
			value = "Get all events",
			notes = "",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="/list")
    public ResponseEntity<QueryArrayStream<EventInstanceVO>> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")Integer limit) {

        RestProcessResult<QueryArrayStream<EventInstanceVO>> result = new RestProcessResult<QueryArrayStream<EventInstanceVO>>(HttpStatus.OK);
        
        User user = this.checkUser(request, result);
        if(result.isOk()){
    		ASTNode root = new ASTNode("and", new ASTNode("eq", "userId", user.getId()), new ASTNode("limit", limit));
        	return result.createResponseEntity(getPageStream(root));
    	}
        return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get event by ID",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/{id}")
    public ResponseEntity<EventInstanceModel> getById(
    		@ApiParam(value = "Valid Event ID", required = true, allowMultiple = false)
    		@PathVariable Integer id, HttpServletRequest request) {

		RestProcessResult<EventInstanceModel> result = new RestProcessResult<EventInstanceModel>(HttpStatus.OK);

		this.checkUser(request, result);
        if(result.isOk()){
	        EventInstanceVO vo = EventInstanceDao.instance.get(id);
	        if (vo == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        
	        EventInstanceModel model = new EventInstanceModel(vo);
	        return result.createResponseEntity(model);
        }
        return result.createResponseEntity();
    }
	
	
	@ApiOperation(
			value = "Query Events",
			notes = "Query by posting AST Model",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/query")
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> query(
    		
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) ASTNode query, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		query = addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
  			return result.createResponseEntity(getPageStream(query));
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query Events",
			notes = "Query via rql in url",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);
    	
		User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			//Parse the RQL Query
	    		ASTNode query = this.parseRQLtoAST(request);
	    		query = addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
	    		return result.createResponseEntity(getPageStream(query));
    		}catch(UnsupportedEncodingException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	/**
	 * Update an event
	 * @param vo
	 * @param xid
	 * @param builder
	 * @param request
	 * @return
	 */
	@ApiOperation(
			value = "Acknowledge an existing event",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json"}, produces={"application/json"}, value = "/acknowledge/{id}")
    public ResponseEntity<EventInstanceModel> acknowledgeEvent(
    		@PathVariable Integer id,
    		@RequestBody(required=false) TranslatableMessageModel message, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<EventInstanceModel> result = new RestProcessResult<EventInstanceModel>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){

        	EventDao dao = new EventDao();
	        EventInstance existingEvent = dao.get(id);
	        if (existingEvent == null) {
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	        }
	        
	        if(existingEvent.isAcknowledged()){
	        	//TODO should create a message that says Already Acknowleged..
	        	//result.addRestMessage(get);
	    		return result.createResponseEntity();
	        }

	        EventInstanceModel model = new EventInstanceModel(existingEvent);
	        
	        TranslatableMessage tlm = null;
	        if(message != null)
	        	tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());

	        Common.eventManager.acknowledgeEvent(existingEvent, System.currentTimeMillis(), user.getId(), tlm);
	        
	        model.setAcknowledged(true);
	        
	        //Put a link to the updated data in the header?
	    	URI location = builder.path("/v1/events/{id}").buildAndExpand(id).toUri();
	    	
	    	result.addRestMessage(getResourceUpdatedMessage(location));
	    	//TODO Could get the model from the DB and return it...
	        return result.createResponseEntity(model);
        }
        //Not logged in
        return result.createResponseEntity();
    }
	
	
	@ApiOperation(
			value = "Acknowledge many existing events",
			notes = ""
			)
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/acknowledge")
    public ResponseEntity<EventAcknowledgeQueryStream> acknowledgeManyEvents(
    		@RequestBody(required=false) TranslatableMessageModel message, 
    		UriComponentsBuilder builder, HttpServletRequest request) {

		RestProcessResult<EventAcknowledgeQueryStream> result = new RestProcessResult<EventAcknowledgeQueryStream>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){

        	//Parse the RQL Query
    		ASTNode query;
			try {
				query = this.parseRQLtoAST(request);
				query = addAndRestriction(query, new ASTNode("eq", "userId", user.getId()));
	    		
		        TranslatableMessage tlm = null;
		        if(message != null)
		        	tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());

		        //Perform the query and stream through acknowledger
		        EventAcknowledgeQueryStreamCallback callback = new EventAcknowledgeQueryStreamCallback(user, tlm);
		        EventAcknowledgeQueryStream stream = new EventAcknowledgeQueryStream(dao, this, query, callback);
				//Ensure its ready
				stream.setupQuery();

				return result.createResponseEntity(stream);
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
			}
    		
        }
        //Not logged in
        return result.createResponseEntity();
    }
	
	@ApiOperation(
			value = "Get the active events summary",
			notes = "List of counts for all active events by type and the most recent active alarm for each."
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/active-summary")
    public ResponseEntity<List<EventLevelSummaryModel>> getActiveSummary(HttpServletRequest request) {

		RestProcessResult<List<EventLevelSummaryModel>> result = new RestProcessResult<List<EventLevelSummaryModel>>(HttpStatus.OK);

		User user = this.checkUser(request, result);
        if(result.isOk()){
        	List<EventLevelSummaryModel> list = new ArrayList<EventLevelSummaryModel>();
            
            List<EventInstance> events = Common.eventManager.getAllActiveUserEvents(user.getId());
            int lifeSafetyTotal = 0;
            EventInstance lifeSafetyEvent = null;
            int criticalTotal = 0;
            EventInstance criticalEvent = null;
            int urgentTotal = 0;
            EventInstance urgentEvent = null;
            int warningTotal = 0;
            EventInstance warningEvent = null;
            int importantTotal = 0;
            EventInstance importantEvent = null;
            int informationTotal = 0;
            EventInstance informationEvent = null;
            int noneTotal = 0;
            EventInstance noneEvent = null;
            int doNotLogTotal = 0;
            EventInstance doNotLogEvent = null;
            
            for(EventInstance event : events){
            	switch(event.getAlarmLevel()){
            	case AlarmLevels.LIFE_SAFETY:
            		lifeSafetyTotal++;
            		lifeSafetyEvent = event;
            		break;
            	case AlarmLevels.CRITICAL:
            		criticalTotal++;
            		criticalEvent = event;
            		break;
            	case AlarmLevels.URGENT:
            		urgentTotal++;
            		urgentEvent = event;
            		break;
            	case AlarmLevels.WARNING:
            		warningTotal++;
            		warningEvent = event;
            		break;
            	case AlarmLevels.IMPORTANT:
            		importantTotal++;
            		importantEvent = event;
            		break;
            	case AlarmLevels.INFORMATION:
            		informationTotal++;
            		informationEvent = event;
            		break;
            	case AlarmLevels.NONE:
            		noneTotal++;
            		noneEvent = event;
            		break;
            	case AlarmLevels.DO_NOT_LOG:
            		doNotLogTotal++;
            		doNotLogEvent = event;
            		break;
            	}
            }
            EventInstanceModel model;
            //Life Safety
            if(lifeSafetyEvent != null)
            	model = new EventInstanceModel(lifeSafetyEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.LIFE_SAFETY), lifeSafetyTotal, model));
            //Critical Events
            if(criticalEvent != null)
            	model = new EventInstanceModel(criticalEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.CRITICAL), criticalTotal, model));
            //Urgent Events
            if(urgentEvent != null)
            	model = new EventInstanceModel(urgentEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.URGENT), urgentTotal, model));
            //Warning Events
            if(warningEvent != null)
            	model = new EventInstanceModel(warningEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.WARNING), warningTotal, model));
            //Important Events
            if(importantEvent != null)
            	model = new EventInstanceModel(importantEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.IMPORTANT), importantTotal, model));
            //Information Events
            if(informationEvent != null)
            	model = new EventInstanceModel(informationEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.INFORMATION), informationTotal, model));
            //None Events
            if(noneEvent != null)
            	model = new EventInstanceModel(noneEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.NONE), noneTotal, model));
            //Do Not Log Events
            if(doNotLogEvent != null)
            	model = new EventInstanceModel(doNotLogEvent);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.DO_NOT_LOG), doNotLogTotal, model));
	            
	        return result.createResponseEntity(list);
        }
        return result.createResponseEntity();
    }


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	public EventInstanceModel createModel(EventInstanceVO vo) {
		return new EventInstanceModel(vo);
	}

	
	class EventAcknowledgeQueryStream extends QueryObjectStream<EventInstanceVO, EventInstanceModel, EventInstanceDao>{

		/**
		 * @param dao
		 * @param controller
		 * @param root
		 * @param queryCallback
		 */
		public EventAcknowledgeQueryStream(EventInstanceDao dao,
				MangoVoRestController<EventInstanceVO, EventInstanceModel, EventInstanceDao> controller, ASTNode root,
				QueryStreamCallback<EventInstanceVO> queryCallback) {
			super(dao, controller, root, queryCallback);
		}
		
		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.JsonArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
			this.queryCallback.setJsonGenerator(jgen);
			this.results.query();
			((EventAcknowledgeQueryStreamCallback)this.queryCallback).finish();
		}
		
	}
	
	class EventAcknowledgeQueryStreamCallback extends QueryStreamCallback<EventInstanceVO>{
		
		private int count;
		private User user;
		private TranslatableMessage message;
		private long ackTimestamp;
		
		
		/**
		 * @param user2
		 * @param tlm
		 */
		public EventAcknowledgeQueryStreamCallback(User user, TranslatableMessage message) {
			this.user = user;
			this.message = message;
			this.ackTimestamp = System.currentTimeMillis();
		}

		/* (non-Javadoc)
		 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
		 */
		@Override
		public void row(EventInstanceVO vo, int index) {
			Common.eventManager.acknowledgeEvent(createEventInstance(vo), ackTimestamp, user.getId(), message);
			this.count++;
		}
		
		private EventInstance createEventInstance(EventInstanceVO vo){
			//TODO This is a hack until we redo the Events Page to better work 
			// with the Events Manager
			EventInstance evt = new EventInstance(vo.getEventType(), 
					vo.getActiveTimestamp(),
					vo.isRtnApplicable(),
					vo.getAlarmLevel(),
					vo.getMessage(),
					vo.getContext());
			evt.setId(vo.getId());
			return evt;
		}
		
		public void finish() throws IOException{
			this.jgen.writeNumberField("count", this.count);
		}
	}

}
