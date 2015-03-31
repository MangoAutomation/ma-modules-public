/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.DataTypeEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueRollupCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeDatabaseStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.serotonin.m2m2.web.taglib.Functions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * TODO Use Point Value Facade for recent data access
 * 
 * @author Terry Packer
 * 
 */
@Api(value="Point Values", description="Operations on Point Values")
@RestController
@RequestMapping("/v1/point-values")
public class PointValueRestController extends MangoRestController{

	private static Log LOG = LogFactory.getLog(PointValueRestController.class);
	private PointValueDao dao = Common.databaseProxy.newPointValueDao();

	
	/**
	 * Get the latest point values for a point
	 * @param xid
	 * @param limit
	 * @return
	 */
	@ApiOperation(
			value = "Get Latest Point Values Directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
			notes = "Default limit 100, time descending order, Default to return cached data"
			)
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/latest", produces={"application/json", "text/csv"})
    public ResponseEntity<List<PointValueTimeModel>> getLatestPointValues(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xid", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		
    		@ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean useRendered,
    		
    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion,
    		
    		@ApiParam(value = "Limit results", allowMultiple = false, defaultValue="100")
    		@RequestParam(value="limit", defaultValue="100") int limit,

    		@ApiParam(value = "Return cached data?", allowMultiple = false, defaultValue="true")
    		@RequestParam(value="useCache", defaultValue="true") boolean useCache
    		){
        
    	RestProcessResult<List<PointValueTimeModel>> result = new RestProcessResult<List<PointValueTimeModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    	
	    	DataPointVO vo = DataPointDao.instance.getByXid(xid);
	    	if(vo == null){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{
	    		if(Permissions.hasDataPointReadPermission(user, vo)){
	    			PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), useCache);
	    			
	    			List<PointValueTime> pvts = pointValueFacade.getLatestPointValues(limit);
	    			List<PointValueTimeModel> models = new ArrayList<PointValueTimeModel>(pvts.size());
	    			if(useRendered){
	    				//Render the values as Strings with the suffix and or units
	    				for(PointValueTime pvt : pvts){
	    					PointValueTimeModel model = new PointValueTimeModel();
	    					model.setType(DataTypeEnum.convertTo(pvt.getValue().getDataType()));
	    					model.setValue(Functions.getRenderedText(vo, pvt));
	    					model.setTimestamp(pvt.getTime());
	    					if(pvt.isAnnotated())
	    						model.setAnnotation(((AnnotatedPointValueTime) pvt).getAnnotation(Common.getTranslations()));
		    				models.add(model);
	    				}
	    			}else if(unitConversion){
	    				//Check to see if we can convert (Must be a Numeric Value)
	    				if (vo.getPointLocator().getDataTypeId() != DataTypes.NUMERIC){
	    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Can't convert non-numeric types."));
	    					return result.createResponseEntity();
	    				}
	    				//Convert the numeric value using the unit and rendered unit
	    				for(PointValueTime pvt : pvts){
	    					PointValueTimeModel model = new PointValueTimeModel();
	    					model.setType(DataTypeEnum.convertTo(pvt.getValue().getDataType()));
	    					model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue()));
	    					model.setTimestamp(pvt.getTime());
	    					if(pvt.isAnnotated())
	    						model.setAnnotation(((AnnotatedPointValueTime) pvt).getAnnotation(Common.getTranslations()));
		    				models.add(model);
		    			}
		    		}else{
		    			for(PointValueTime pvt : pvts){
		    				models.add(new PointValueTimeModel(pvt));
		    			}
		    		}
	    			return result.createResponseEntity(models);
	    		}else{
	    	 		result.addRestMessage(getUnauthorizedMessage());
	    	 		return result.createResponseEntity();
		    	}
	    	}catch(PermissionException e){
	    		LOG.error(e.getMessage(), e);
	    		result.addRestMessage(getUnauthorizedMessage());
	    		return result.createResponseEntity();
	    	}
    	}else{
    		return result.createResponseEntity();
    	}
    }

	@ApiOperation(
	        value = "First and last point values",
	        notes = "Retrieves the first and last point values within a time range, used to read accumulators"
	        )
	@ApiResponses({
	    @ApiResponse(code = 200, message = "Query Successful", response=PointValueTimeModel.class),
	    @ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class)
	})
	@RequestMapping(method = RequestMethod.GET, value="/{xid}/first-last", produces={"application/json", "text/csv"})
	public ResponseEntity<List<PointValueTimeModel>> firstAndLastPointValues(
	        HttpServletRequest request,

	        @ApiParam(value = "Point xid", required = true, allowMultiple = false)
	        @PathVariable String xid,

    		@ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean useRendered,
    		
    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion,
	        
	        @ApiParam(value = "From time", required = false, allowMultiple = false)
	        @RequestParam(value="from", required=false, defaultValue="2014-08-10T00:00:00.000-10:00")
	        @DateTimeFormat(iso=ISO.DATE_TIME) Date from,

	        @ApiParam(value = "To time", required = false, allowMultiple = false)
	        @RequestParam(value="to", required=false, defaultValue="2014-08-11T23:59:59.999-10:00")
	        @DateTimeFormat(iso=ISO.DATE_TIME) Date to
	        ){
	    RestProcessResult<List<PointValueTimeModel>> result = new RestProcessResult<List<PointValueTimeModel>>(HttpStatus.OK);
	    User user = this.checkUser(request, result);
	    if(result.isOk()){
	        DataPointVO vo = DataPointDao.instance.getByXid(xid);
	        if(vo == null){
	            result.addRestMessage(getDoesNotExistMessage());
	            return result.createResponseEntity();
	        }

	        try{
	            if(Permissions.hasDataPointReadPermission(user, vo)){
	                PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), false);
                    PointValueTime first = pointValueFacade.getPointValueAfter(from.getTime());
                    PointValueTime last = pointValueFacade.getPointValueBefore(to.getTime());
                    
                    List<PointValueTimeModel> models = new ArrayList<PointValueTimeModel>(2);
                    if(useRendered){
                    	if(first != null){
                    		PointValueTimeModel model = new PointValueTimeModel();
	    					model.setType(DataTypeEnum.convertTo(first.getValue().getDataType()));
	    					model.setValue(Functions.getRenderedText(vo, first));
	    					model.setTimestamp(first.getTime());
	    					if(first.isAnnotated())
	    						model.setAnnotation(((AnnotatedPointValueTime) first).getAnnotation(Common.getTranslations()));
		    				models.add(model);
                    	}
                    	if(last != null){
                      		PointValueTimeModel model = new PointValueTimeModel();
    	    					model.setType(DataTypeEnum.convertTo(last.getValue().getDataType()));
    	    					model.setValue(Functions.getRenderedText(vo, last));
    	    					model.setTimestamp(last.getTime());
    	    					if(last.isAnnotated())
    	    						model.setAnnotation(((AnnotatedPointValueTime) last).getAnnotation(Common.getTranslations()));
    		    				models.add(model);
                    	}
                    }else if(unitConversion){
                    	if(first != null){
                    		PointValueTimeModel model = new PointValueTimeModel();
	    					model.setType(DataTypeEnum.convertTo(first.getValue().getDataType()));
	    					model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(first.getValue().getDoubleValue()));
	    					model.setTimestamp(first.getTime());
	    					if(first.isAnnotated())
	    						model.setAnnotation(((AnnotatedPointValueTime) first).getAnnotation(Common.getTranslations()));
		    				models.add(model);
                    	}
                    	if(last != null){
                    		PointValueTimeModel model = new PointValueTimeModel();
	    					model.setType(DataTypeEnum.convertTo(last.getValue().getDataType()));
	    					model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(last.getValue().getDoubleValue()));
	    					model.setTimestamp(last.getTime());
	    					if(last.isAnnotated())
	    						model.setAnnotation(((AnnotatedPointValueTime) last).getAnnotation(Common.getTranslations()));
		    				models.add(model);
                    	}
                    }else{
	                    models.add(first == null ? null : new PointValueTimeModel(first));
	                    models.add(last == null ? null : new PointValueTimeModel(last));
                    }
	                return result.createResponseEntity(models);
	            }else{
	                result.addRestMessage(getUnauthorizedMessage());
	                return result.createResponseEntity();
	            }
	        }catch(PermissionException e){
	            LOG.error(e.getMessage(), e);
	            result.addRestMessage(getUnauthorizedMessage());
	            return result.createResponseEntity();
	        }
	    }else{
	        return result.createResponseEntity();
	    }
	}

	@ApiOperation(
			value = "Query Time Range",
			notes = "From time inclusive, To time exclusive",
			response=PointValueTimeModel.class,
			responseContainer="List"
			)
	@ApiResponses({
		@ApiResponse(code = 200, message = "Query Successful", response=PointValueTimeModel.class),
		@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class)
		})
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValues(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xid", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		
    		@ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean useRendered,
    		
    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion,

    		@ApiParam(value = "From time", required = false, allowMultiple = false)
    		@RequestParam(value="from", required=false, defaultValue="2014-08-10T00:00:00.000-10:00")
    		//Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
    		@DateTimeFormat(iso=ISO.DATE_TIME) Date from,
    		
    		@ApiParam(value = "To time", required = false, allowMultiple = false)
			@RequestParam(value="to", required=false, defaultValue="2014-08-11T23:59:59.999-10:00")
    		//Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
    		@DateTimeFormat(iso=ISO.DATE_TIME) Date to,
    		
    		@ApiParam(value = "Rollup type", required = false, allowMultiple = false)
			@RequestParam(value="rollup", required=false)
    		RollupEnum rollup,

    		@ApiParam(value = "Time Period Type", required = false, allowMultiple = false)
			@RequestParam(value="timePeriodType", required=false)
    		TimePeriodType timePeriodType,
    		
    		@ApiParam(value = "Time Periods", required = false, allowMultiple = false)
			@RequestParam(value="timePeriods", required=false)
    		Integer timePeriods    		
    		){
        
    	RestProcessResult<QueryArrayStream<PointValueTimeModel>> result = new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    	
	    	DataPointVO vo = DataPointDao.instance.getByXid(xid);
	    	if(vo == null){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{
	    		if(Permissions.hasDataPointReadPermission(user, vo)){
	    			//Are we using rollup
	    			if(rollup != null){
	    				TimePeriod timePeriod = null;
	    				if((timePeriodType != null)&&(timePeriods != null)){
	    					timePeriod = new TimePeriod(timePeriods, timePeriodType);
	    				}
	    				PointValueRollupCalculator calc = new PointValueRollupCalculator(vo, useRendered, unitConversion, rollup, timePeriod, from.getTime(), to.getTime());
	    				return result.createResponseEntity(calc);
	    			}else{
	    				PointValueTimeDatabaseStream pvtDatabaseStream = new PointValueTimeDatabaseStream(vo, useRendered, unitConversion, from.getTime(), to.getTime(), this.dao);
		    			return result.createResponseEntity(pvtDatabaseStream);
	    			}
	    			
	    		}else{
	    	 		result.addRestMessage(getUnauthorizedMessage());
		    		return result.createResponseEntity();
		    		}
	    	}catch(PermissionException e){
	    		LOG.error(e.getMessage(), e);
	    		result.addRestMessage(getUnauthorizedMessage());
	    		return result.createResponseEntity();
	    	}
    	}else{
    		return result.createResponseEntity();
    	}
    }
	
	@ApiOperation(
			value = "Get Point Statistics",
			notes = "From time inclusive, To time exclusive"
			//TODO Implement a Statistics Model for the stream and put as response class here
			)
	@ApiResponses({
		@ApiResponse(code = 200, message = "Query Successful", response=StatisticsStream.class),
		@ApiResponse(code = 401, message = "Unauthorized Access", response=ResponseEntity.class)
		})
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/statistics")
    public ResponseEntity<StatisticsStream> getPointStatistics(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xid", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		
    		@ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean useRendered,

    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion,

    		@ApiParam(value = "From time", required = false, allowMultiple = false)
    		@RequestParam(value="from", required=false, defaultValue="2014-08-10T00:00:00.000-10:00") //Not working yet: defaultValue="2014-08-01 00:00:00.000 -1000" )
    		//Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
    		@DateTimeFormat(iso=ISO.DATE_TIME) Date from,
    		
    		@ApiParam(value = "To time", required = false, allowMultiple = false)
			@RequestParam(value="to", required=false, defaultValue="2014-08-11T23:59:59.999-10:00")//Not working yet defaultValue="2014-08-11 23:59:59.999 -1000")
    		//Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
    		@DateTimeFormat(iso=ISO.DATE_TIME) Date to    		
    		){
        
    	RestProcessResult<StatisticsStream> result = new RestProcessResult<StatisticsStream>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    	
	    	DataPointVO vo = DataPointDao.instance.getByXid(xid);
	    	if(vo == null){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{
	    		if(Permissions.hasDataPointReadPermission(user, vo)){
	    			StatisticsStream stream = new StatisticsStream(vo, useRendered, unitConversion, from.getTime(), to.getTime());
	    			return result.createResponseEntity(stream);
	    		}else{
	    	 		result.addRestMessage(getUnauthorizedMessage());
		    		return result.createResponseEntity();
		    		}
	    	}catch(PermissionException e){
	    		LOG.error(e.getMessage(), e);
	    		result.addRestMessage(getUnauthorizedMessage());
	    		return result.createResponseEntity();
	    	}
    	}else{
    		return result.createResponseEntity();
    	}
    }
    
    /**
     * Update a point value in the system
     * @param pvt
     * @param xid
     * @param builder
     * @return
     * @throws RestValidationFailedException 
     */
	@ApiOperation(
			value = "Updatae an existing data point's value",
			notes = "Data point must exist and be enabled"
			)
	@RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<PointValueTimeModel> putPointValue(
    		HttpServletRequest request, 
    		@RequestBody PointValueTimeModel model, 
    		@PathVariable String xid, 
    		
    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion,

    		UriComponentsBuilder builder) throws RestValidationFailedException {
		
		RestProcessResult<PointValueTimeModel> result = new RestProcessResult<PointValueTimeModel>(HttpStatus.OK);
		
		User user = this.checkUser(request, result);
		if(result.isOk()){
		
	        DataPointVO existingDp = DataPointDao.instance.getByXid(xid);
	        if (existingDp == null) {
	        	result.addRestMessage(getDoesNotExistMessage());
	        	return result.createResponseEntity();
	    	}
	        
	    	try{
	    		if(Permissions.hasDataPointReadPermission(user, existingDp)){
	    			
	    			//Set the time to now if it is not present
	    			if(model.getTimestamp() == 0){
	    				model.setTimestamp(System.currentTimeMillis());
	    			}
	    			
	    			//Validate the model's data type for compatibility
	    			if(DataTypeEnum.convertFrom(model.getType()) != existingDp.getPointLocator().getDataTypeId()){
	    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("event.ds.dataType"));
	    				return result.createResponseEntity();
	    			}
	    			
	    			//Validate the timestamp for future dated
	    			if (model.getTimestamp() > System.currentTimeMillis() + SystemSettingsDao.getFutureDateLimit()) {
	    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Future dated points not acceptable."));
	    				return result.createResponseEntity();
	    		    }

	    			//TODO Backdate validation?
	    			//boolean backdated = pointValue != null && newValue.getTime() < pointValue.getTime();
    		        
	    			//Are we converting from the rendered Unit?
	    			if(unitConversion){
	    				if((model.getType() == DataTypeEnum.NUMERIC)&&(model.getValue() instanceof Number)){
	    					double value;
	    					if(model.getValue() instanceof Integer){
	    						value = (double)((Integer)model.getValue());
	    					}else{
	    						value = (double)((Double)model.getValue());
	    					}
	    					model.setValue(existingDp.getRenderedUnit().getConverterTo(existingDp.getUnit()).convert(value));
	    				}else{
	    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Cannot perform unit conversion on Non Numeric data types."));
	    					return result.createResponseEntity();
	    				}
	    			}
	    			
	    			//If we are a multistate point and our value is in string format then we should try to convert it
	    			if((model.getType() == DataTypeEnum.MULTISTATE)&&(model.getValue() instanceof String)){
	    				try{
		    				DataValue value = existingDp.getTextRenderer().parseText((String)model.getValue(), existingDp.getPointLocator().getDataTypeId());
		    				model.setValue(value.getObjectValue());
	    				}catch(Exception e){
	    					//Lots can go wrong here so let the user know
	    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Unable to convert Multistate String representation to any known value."));
	    				}
	    			}
	    			
	    			
	    			
    		        final PointValueTime pvt;
    				try{
    					pvt = model.getData(); 
    				}catch(Exception e){
    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Invalid Format"));
    					return result.createResponseEntity();
    				}
    				
    				//one last check to ensure we are inserting the correct data type
	    			if(DataTypes.getDataType(pvt.getValue()) != existingDp.getPointLocator().getDataTypeId()){
	    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("event.ds.dataType"));
	    				return result.createResponseEntity();
	    			}
    				
	    	        final int dataSourceId = existingDp.getDataSourceId();
	    	        SetPointSource source = null;
	    	        if(model.getAnnotation() != null){
	    	        	source = new SetPointSource(){
	
	    					@Override
	    					public String getSetPointSourceType() {
	    						return "REST";
	    					}
	
	    					@Override
	    					public int getSetPointSourceId() {
	    						return dataSourceId;
	    					}
	
	    					@Override
	    					public TranslatableMessage getSetPointSourceMessage() {
	    						return ((AnnotatedPointValueTime)pvt).getSourceMessage();
	    					}
	
	    					@Override
	    					public void raiseRecursionFailureEvent() {
	    						LOG.error("Recursive failure while setting point via REST");
	    					}
	    	        		
	    	        	};
	    	        }
	    	        try{
	    	        	Common.runtimeManager.setDataPointValue(existingDp.getId(), pvt, source);
	    	        	//This URI may not always be accurate if the Data Source doesn't use the provided time...
	    	        	URI location = builder.path("/v1/pointValue/{xid}/{time}").buildAndExpand(xid, pvt.getTime()).toUri();
	    		    	result.addRestMessage(getResourceCreatedMessage(location));
	    		        return result.createResponseEntity(new PointValueTimeModel(pvt));
	
	    	        }catch(Exception e){
	    	        	LOG.error(e.getMessage(), e);
	    	        	result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
	    	        	return result.createResponseEntity();
	    	        }
	    		}else{
		    		result.addRestMessage(getUnauthorizedMessage());
		    		return result.createResponseEntity();
	    		}
	    	}catch(PermissionException e){
	    		LOG.error(e.getMessage(), e);
	    		result.addRestMessage(getUnauthorizedMessage());
	    		return result.createResponseEntity();
	    	}
		}else{
			return result.createResponseEntity();
		}
    }
    
	

	
	
}
