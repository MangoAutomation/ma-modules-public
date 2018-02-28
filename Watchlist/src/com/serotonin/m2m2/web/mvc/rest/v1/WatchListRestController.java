/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.watchlist.WatchListDao;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredPageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListDataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.WatchListSummaryModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Watch Lists", description="")
@RestController
@RequestMapping("/v1/watch-lists")
public class WatchListRestController extends MangoVoRestController<WatchListVO, WatchListSummaryModel, WatchListDao>{

	private static Log LOG = LogFactory.getLog(WatchListRestController.class);

	public WatchListRestController(){
		super(WatchListDao.instance);
	}

	@ApiOperation(
			value = "Query WatchLists",
			notes = "",
			response=WatchListSummaryModel.class,
			responseContainer="Array"
			)
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Ok", response=WatchListSummaryModel.class),
			@ApiResponse(code = 403, message = "User does not have access", response=ResponseEntity.class)
		})
	@RequestMapping(method = RequestMethod.GET, produces={"application/json"})
    public ResponseEntity<QueryDataPageStream<WatchListVO>> queryRQL(
    		HttpServletRequest request) {
		
		RestProcessResult<QueryDataPageStream<WatchListVO>> result = new RestProcessResult<QueryDataPageStream<WatchListVO>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		try{
    			ASTNode query = parseRQLtoAST(request.getQueryString());
    			if(!user.isAdmin()){
    				//We are going to filter the results, so we need to strip out the limit(limit,offset) or limit(limit) clause.
    				WatchListStreamCallback callback = new WatchListStreamCallback(this, user);
    				FilteredPageQueryStream<WatchListVO, WatchListSummaryModel, WatchListDao> stream  = 
    						new FilteredPageQueryStream<WatchListVO, WatchListSummaryModel, WatchListDao>(WatchListDao.instance,
    								this, query, callback);
    				stream.setupQuery();
    				return result.createResponseEntity(stream);
	    		}else
	    			return result.createResponseEntity(getPageStream(query));
    		}catch(InvalidRQLRestException e){
    			LOG.error(e.getMessage(), e);
    			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
				return result.createResponseEntity();
    		}
    	}
    	
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Create New WatchList",
			notes = "",
			response=WatchListModel.class
			)
	@ApiResponses({
			@ApiResponse(code = 201, message = "User Created", response=WatchListModel.class),
			@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class),
			@ApiResponse(code = 409, message = "WatchList Already Exists")
			})
	@RequestMapping(method = RequestMethod.POST, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"})
    public ResponseEntity<WatchListModel> createNew(
    		@ApiParam( value = "Watchlist to save", required = true )
    		@RequestBody
    		WatchListModel model,
    		UriComponentsBuilder builder,
    		HttpServletRequest request) throws RestValidationFailedException {

		RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.CREATED);
    	User user = this.checkUser(request, result);
    	if (!result.isOk()) {
    	    return result.createResponseEntity();
    	}
		
		WatchListVO wl = model.getData();		
		
		//Check XID if blank and generate one
		if(StringUtils.isBlank(wl.getXid())){
			wl.setXid(this.dao.generateUniqueXid());
		}
		//Add the user
		wl.setUserId(user.getId());
		
		//Setup the Points
		if(model.getPoints() != null)
			for(WatchListDataPointModel pm : model.getPoints())
				wl.getPointList().add(pm.getDataPointVO());
		
		//Ready to validate and then save
		if(!model.validate()){
		    result.addRestMessage(this.getValidationFailedError());
		    return result.createResponseEntity(model);
		}
		
		try {
            String initiatorId = request.getHeader("initiatorId");
            this.dao.save(wl, initiatorId);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }
        return result.createResponseEntity(new WatchListModel(wl, this.dao.getPointSummaries(wl.getId())));
	}
	
	@ApiOperation(
			value = "Get a Watchlist",
			notes = "",
			response=WatchListModel.class
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<WatchListModel> get(
    		@PathVariable String xid,
    		HttpServletRequest request) throws RestValidationFailedException {
		RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.OK);
		try{
	    	User user = this.checkUser(request, result);
	    	if(result.isOk()){
	    		WatchListVO wl = this.dao.getByXid(xid);
	    		if(wl == null){
	    			result.addRestMessage(getDoesNotExistMessage());
	    			return result.createResponseEntity();
	    		}
	    		if(hasReadPermission(user, wl)){
	    			List<WatchListDataPointModel> points = this.dao.getPointSummaries(wl.getId());
	    			//Filter them on read permission
	    			ListIterator<WatchListDataPointModel> it = points.listIterator();
	    			while(it.hasNext()){
	    				if(!Permissions.hasPermission(user, it.next().getReadPermission()))
	    					it.remove();
	    			}
	    			return result.createResponseEntity(new WatchListModel(wl, points));
	    		}else{
	    			result.addRestMessage(getUnauthorizedMessage());
	    		}
	    	}
		}catch(Exception e){
			LOG.warn(e.getMessage(), e);
			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
		}
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Update a WatchList",
			notes = "",
			response=WatchListModel.class
			)
	@RequestMapping(method = RequestMethod.PUT, consumes={"application/json", "text/csv"}, produces={"application/json", "text/csv"}, value = "/{xid}")
    public ResponseEntity<WatchListModel> update(
    		@PathVariable String xid,
    		@RequestBody WatchListModel model,
    		HttpServletRequest request) throws RestValidationFailedException {
		RestProcessResult<WatchListModel> result = new RestProcessResult<WatchListModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if (!result.isOk()) {
            return result.createResponseEntity();
        }
    	WatchListVO wl = this.dao.getByXid(xid);
		
		if(wl == null){
			result.addRestMessage(getDoesNotExistMessage());
			return result.createResponseEntity();
		}
		if(!hasEditPermission(user, wl)){
		    result.addRestMessage(getUnauthorizedMessage());
		    return result.createResponseEntity();
		}
		
		WatchListVO update = model.getData();
		//Set the id
		update.setId(wl.getId());
		//Add the user
		update.setUserId(wl.getUserId());
		
		//Setup the Points
		if(model.getPoints() != null)
			for(WatchListDataPointModel pm : model.getPoints())
				update.getPointList().add(pm.getDataPointVO());
		
		if (!model.validate()){
		    result.addRestMessage(this.getValidationFailedError());
            return result.createResponseEntity(model);
		}
		
		
		try {
            String initiatorId = request.getHeader("initiatorId");
            this.dao.save(update, initiatorId);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
        }
		
		return result.createResponseEntity(model);
	}
	
	@ApiOperation(
			value = "Delete a WatchList ",
			notes = "Only the owner or an admin can delete",
			response=WatchListModel.class
			)
	@RequestMapping(method = RequestMethod.DELETE, consumes={"application/*"}, produces={"*/*"}, value = "/{xid}")
    public ResponseEntity<Void> delete(
    		@PathVariable String xid,
    		HttpServletRequest request) throws RestValidationFailedException {
		RestProcessResult<Void> result = new RestProcessResult<Void>(HttpStatus.OK);
		try{
	    	User user = this.checkUser(request, result);
	    	if(result.isOk()){
	    		WatchListVO wl = this.dao.getByXid(xid);
	    		if(wl == null){
	    			result.addRestMessage(getDoesNotExistMessage());
	    			return result.createResponseEntity();
	    		}
	    		if(isOwner(user, wl)){
	                String initiatorId = request.getHeader("initiatorId");
	    			this.dao.delete(wl.getId(), initiatorId);
	    			result.addRestMessage(HttpStatus.NO_CONTENT, new TranslatableMessage("common.deleted"));
	    			return result.createResponseEntity();
	    		}else{
	    			result.addRestMessage(this.getUnauthorizedMessage());
    				return result.createResponseEntity();
	    		}
	    	}
		}catch(Exception e){
			LOG.warn(e.getMessage(), e);
			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
		}
    	return result.createResponseEntity();
	}
	
	@ApiOperation(
			value = "Get Data Points for a Watchlist",
			notes = "",
			response=WatchListPointsQueryDataPageStream.class
			)
	@RequestMapping(method = RequestMethod.GET, produces={"application/json", "text/csv"}, value = "/{xid}/data-points")
    public ResponseEntity<WatchListPointsQueryDataPageStream> getDataPoints(
    		@PathVariable String xid,
    		HttpServletRequest request) throws RestValidationFailedException {
		RestProcessResult<WatchListPointsQueryDataPageStream> result = new RestProcessResult<WatchListPointsQueryDataPageStream>(HttpStatus.OK);
		try{
	    	User user = this.checkUser(request, result);
	    	if(result.isOk()){
	    		WatchListVO wl = this.dao.getByXid(xid);
	    		if(wl == null){
	    			result.addRestMessage(getDoesNotExistMessage());
	    			return result.createResponseEntity();
	    		}
	    		if(hasReadPermission(user, wl)){
	    			WatchListPointsQueryDataPageStream stream = new WatchListPointsQueryDataPageStream(wl.getId(), user);
	    			return result.createResponseEntity(stream);
	    		}else{
	    			result.addRestMessage(getUnauthorizedMessage());
	    		}
	    	}
		}catch(Exception e){
			LOG.warn(e.getMessage(), e);
			result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
		}
    	return result.createResponseEntity();
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(java.lang.Object)
	 */
	@Override
	public WatchListSummaryModel createModel(WatchListVO vo) {
        return new WatchListSummaryModel(vo);
	}
	
    public static boolean isOwner(User user, WatchListVO vo) throws RuntimeException {
        if (user == null)
            return false;
        if (vo == null)
            return false;
        if (Permissions.hasAdmin(user))
            return true;
        if(vo.getUserId() == user.getId())
        	return true; //Owner
        else
        	return false;
    }
	
    public static boolean hasEditPermission(User user, WatchListVO vo) throws RuntimeException {
        if (user == null)
            return false;
        if (vo == null)
            return false;
        if (Permissions.hasAdmin(user))
            return true;
        if(vo.getUserId() == user.getId())
        	return true; //Owner
        return Permissions.hasPermission(user, vo.getEditPermission());
    }
    
	public static boolean hasReadPermission(User user, WatchListVO vo) {
		if (user == null)
            return false;
		else if (vo == null)
            return false;
		else if (Permissions.hasAdmin(user))
            return true;
		else if(vo.getUserId() == user.getId())
        	return true; //Owner
		else if(Permissions.hasPermission(user, vo.getEditPermission()))
        	return true;
		else if(Permissions.hasPermission(user, vo.getReadPermission()))
        	return true;
		else
			return false;
	}

	
	

	
	/**
	 * Class to stream data points and restrict based on permissions
	 * @author Terry Packer
	 *
	 */
	class WatchListPointsQueryDataPageStream implements QueryDataPageStream<DataPointModel>{

		private int watchlistId;
		private long pointCount = 0;
		private User user;
		
		public WatchListPointsQueryDataPageStream(int wlId, User user){
			this.watchlistId = wlId;
			this.user = user;
		}
		
		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(final JsonGenerator jgen) throws IOException {
			WatchListDao.instance.getPoints(watchlistId, new MappedRowCallback<DataPointVO>(){

				@Override
				public void row(DataPointVO dp, int index) {
					if(Permissions.hasDataPointReadPermission(user, dp)){
                        DataPointDao.instance.loadPartialRelationalData(dp);
                        
						try {
							jgen.writeObject(new DataPointModel(dp));
							pointCount++;
						} catch (IOException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			});
			
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
		 */
		@Override
		public void streamData(CSVPojoWriter<DataPointModel> writer) throws IOException {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream#streamCount(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamCount(JsonGenerator jgen) throws IOException {
			jgen.writeNumber(pointCount);
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream#streamCount(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
		 */
		@Override
		public void streamCount(CSVPojoWriter<Long> writer) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
