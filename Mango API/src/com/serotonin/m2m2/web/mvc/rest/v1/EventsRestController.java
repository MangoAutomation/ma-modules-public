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

import com.infiniteautomation.mango.db.query.QueryComparison;
import com.infiniteautomation.mango.db.query.QueryModel;
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
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * 
 * 
 * @author Terry Packer
 *
 */
@Api(value="Events", description="Operations on Events")
@RestController()
@RequestMapping("/v1/events")
public class EventsRestController extends MangoVoRestController<EventInstanceVO, EventInstanceModel>{
	
	private static Log LOG = LogFactory.getLog(EventsRestController.class);
	
	public EventsRestController(){ 
		super(EventInstanceDao.instance);
	}

	
	@ApiOperation(
			value = "Get all events",
			notes = "",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=EventInstanceModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value="/list")
    public ResponseEntity<QueryArrayStream<EventInstanceVO>> getAll(HttpServletRequest request, 
    		@RequestParam(value="limit", required=false, defaultValue="100")Integer limit) {

        RestProcessResult<QueryArrayStream<EventInstanceVO>> result = new RestProcessResult<QueryArrayStream<EventInstanceVO>>(HttpStatus.OK);
        
        this.checkUser(request, result);
    	
        if(result.isOk()){
        	QueryModel query = new QueryModel();
        	query.setLimit(limit);
    		return result.createResponseEntity(getStream(query));
    	}
        return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get existing event by ID",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"}, value = "/{id}")
    public ResponseEntity<EventInstanceModel> getById(
    		@ApiParam(value = "Valid Event ID", required = true, allowMultiple = false)
    		@PathVariable Integer id, HttpServletRequest request) {

		RestProcessResult<EventInstanceModel> result = new RestProcessResult<EventInstanceModel>(HttpStatus.OK);

		this.checkUser(request, result);
        if(result.isOk()){
	        EventInstanceVO vo =EventInstanceDao.instance.get(id);
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
			notes = "",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=EventInstanceModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json"}, produces={"application/json"}, value = "/query")
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> query(
    		
    		@ApiParam(value="Query", required=true)
    		@RequestBody(required=true) QueryModel query, 
    		   		
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);
    	this.checkUser(request, result);
    	if(result.isOk()){
    		return result.createResponseEntity(getPageStream(query));
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Query Events",
			notes = "",
			response=EventInstanceModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=EventInstanceModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<QueryDataPageStream<EventInstanceVO>> queryRQL(HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<EventInstanceVO>> result = new RestProcessResult<QueryDataPageStream<EventInstanceVO>>(HttpStatus.OK);
    	
		this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			//Parse the RQL Query
	    		QueryModel query = this.parseRQL(request);
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
	        dao.ackEvent(id, System.currentTimeMillis(), user.getId(), tlm);
	        
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
            
            int total = EventInstanceDao.instance.countUnsilencedEvents(user.getId(),AlarmLevels.LIFE_SAFETY);
            EventInstanceVO event = EventInstanceDao.instance.getHighestUnsilencedEvent(user.getId(), AlarmLevels.LIFE_SAFETY);
            EventInstanceModel model;
            if(event != null)
            	 model = new EventInstanceModel(event);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.LIFE_SAFETY), total, model));

            total = EventInstanceDao.instance.countUnsilencedEvents(user.getId(), AlarmLevels.CRITICAL);
            event = EventInstanceDao.instance.getHighestUnsilencedEvent(user.getId(), AlarmLevels.CRITICAL);
            if(event != null)
            	model = new EventInstanceModel(event);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.CRITICAL), total, model));

            total = EventInstanceDao.instance.countUnsilencedEvents(user.getId(), AlarmLevels.URGENT);
            event = EventInstanceDao.instance.getHighestUnsilencedEvent(user.getId(), AlarmLevels.URGENT);
            if(event != null)
            	model = new EventInstanceModel(event);
            else
            	model = null;            
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.URGENT), total, model));
            
            total = EventInstanceDao.instance.countUnsilencedEvents(user.getId(), AlarmLevels.INFORMATION);
            event = EventInstanceDao.instance.getHighestUnsilencedEvent(user.getId(), AlarmLevels.INFORMATION);
            if(event != null)
            	model = new EventInstanceModel(event);
            else
            	model = null;           
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.INFORMATION), total, model));
            
            total = EventInstanceDao.instance.countUnsilencedEvents(user.getId(), AlarmLevels.NONE);
            event = EventInstanceDao.instance.getHighestUnsilencedEvent(user.getId(), AlarmLevels.NONE);
            if(event != null)
            	model = new EventInstanceModel(event);
            else
            	model = null;
            list.add(new EventLevelSummaryModel(AlarmLevels.CODES.getCode(AlarmLevels.NONE), total, model));
             
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


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#mapComparisons(java.util.List)
	 */
	@Override
	public void mapComparisons(List<QueryComparison> list) {
		//Check for the attribute commentType
		for(QueryComparison param : list){
			if(param.getAttribute().equalsIgnoreCase("alarmLevel")){
				param.setCondition(Integer.toString(AlarmLevels.CODES.getId(param.getCondition())));
			}
		}

	}


}
