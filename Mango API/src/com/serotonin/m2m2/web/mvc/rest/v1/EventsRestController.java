/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.jazdw.rql.parser.ASTNode;

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
import com.serotonin.m2m2.web.mvc.rest.v1.model.TranslatableMessageModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventLevelSummaryModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
		this.modelMap.put("dataSourceId", "typeRef2");
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
        	return result.createResponseEntity(getPageStream(restrictQuery(new ASTNode("limit", limit), user)));
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
  			return result.createResponseEntity(getPageStream(restrictQuery(query, user)));
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
	    		return result.createResponseEntity(getPageStream(restrictQuery(query, user)));
    		}catch(UnsupportedEncodingException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	/**
	 * Restrict results based on userId
	 * @param query
	 * @return
	 */
	private ASTNode restrictQuery(ASTNode query, User user){
		if(query == null){
			return new ASTNode("eq", "userId", user.getId());
		}else{
			return new ASTNode("and",  new ASTNode("eq", "userId", user.getId()), query);
		}
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
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.NONE), doNotLogTotal, model));
	            
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


}
