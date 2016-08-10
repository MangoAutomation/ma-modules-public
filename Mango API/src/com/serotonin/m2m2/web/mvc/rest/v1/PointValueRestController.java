/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RTException;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.TimePeriodBucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.exception.RestValidationFailedException;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestMessage;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.DataTypeEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.IdPointValueRollupCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.IdPointValueTimeDatabaseStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.IdPointValueTimeLatestPointValueFacadeStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueFftCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueRollupCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeDatabaseStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueMapRollupCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeLatestPointFacadeStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeMapDatabaseStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.serotonin.m2m2.web.taglib.Functions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * TODO Use Point Value Facade for recent data access
 * 
 * @author Terry Packer
 * 
 */
@Api(value="Point Values", description="Point Values")
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

	    			if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE){
		    			//If we are an image type we should build the URLS
		    			UriComponentsBuilder imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/{ts}_{id}.jpg");
		    			imageServletBuilder.scheme(request.getScheme());
		    			imageServletBuilder.host(request.getServerName());
		    			imageServletBuilder.port(request.getLocalPort());
		    			
	    				for(PointValueTimeModel model : models){
	    					model.setValue(imageServletBuilder.buildAndExpand(model.getTimestamp(), vo.getId()).toUri());
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
	
	/**
     * Get the latest point values a set of points
     * return as map of xid to array of values
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns as single time ordered array."
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xids}/latest-multiple-points-single-array", produces={"application/json", "text/csv"})
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getLatestPointValuesForMultiplePointsAsSingleArray(
            HttpServletRequest request, 
            
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,
            
            @ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean useRendered,
            
            @ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean unitConversion,
            
            @ApiParam(value = "Limit results", allowMultiple = false, defaultValue="100")
            @RequestParam(value="limit", defaultValue="100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false, defaultValue="true")
            @RequestParam(value="useCache", defaultValue="true") boolean useCache
            ){
    	RestProcessResult<QueryArrayStream<PointValueTimeModel>> result = new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
    		DataPointVO vo;
    		for(String xid : xids){
    			 vo = DataPointDao.instance.getByXid(xid);
    			 if(vo != null){
    				 if(Permissions.hasDataPointReadPermission(user, vo))
    					 pointIdMap.put(vo.getId(), vo);
    				 else{
    					 //Abort, invalid permissions
    					 result.addRestMessage(getUnauthorizedMessage());
    					 return result.createResponseEntity();
    				 }
    			 }
    		}
    		
    		//Do we have any valid points?
	    	if(pointIdMap.size() == 0){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{
	    		IdPointValueTimeLatestPointValueFacadeStream pvtDatabaseStream = new IdPointValueTimeLatestPointValueFacadeStream(request, pointIdMap, useRendered, unitConversion, limit, useCache);
    			return result.createResponseEntity(pvtDatabaseStream);
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
     * Get the latest point values a set of points
     * return as map of xid to array of values
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns data as map of xid to values."
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xids}/latest-multiple-points-multiple-arrays", produces={"application/json", "text/csv"})
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getLatestPointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request, 
            
            @ApiParam(value = "Point xids", required = true, allowMultiple = true)
            @PathVariable String[] xids,
            
            @ApiParam(value = "Return rendered value as String", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean useRendered,
            
            @ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean unitConversion,
            
            @ApiParam(value = "Limit results", allowMultiple = false, defaultValue="100")
            @RequestParam(value="limit", defaultValue="100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false, defaultValue="true")
            @RequestParam(value="useCache", defaultValue="true") boolean useCache
            ){
        
    	RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>> result = new RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
    		Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
    		DataPointVO vo;
    		for(String xid : xids){
    			 vo = DataPointDao.instance.getByXid(xid);
    			 if(vo != null){
    				 if(Permissions.hasDataPointReadPermission(user, vo))
    					 pointIdMap.put(vo.getId(), vo);
    				 else{
    					 //Abort, invalid permissions
    					 result.addRestMessage(getUnauthorizedMessage());
    					 return result.createResponseEntity();
    				 }
    			 }
    		}
    		
    		//Do we have any valid points?
	    	if(pointIdMap.size() == 0){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}
        	
	    	try{
				XidPointValueTimeLatestPointFacadeStream pvtDatabaseStream = new XidPointValueTimeLatestPointFacadeStream(request, pointIdMap, useRendered, unitConversion, limit, useCache);
    			return result.createResponseEntity(pvtDatabaseStream);
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

	    			if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE){
		    			//If we are an image type we should build the URLS
		    			UriComponentsBuilder imageServletBuilder = UriComponentsBuilder.fromPath("/imageValue/{ts}_{id}.jpg");
		    			imageServletBuilder.scheme(request.getScheme());
		    			imageServletBuilder.host(request.getServerName());
		    			imageServletBuilder.port(request.getLocalPort());
		    			
	    				for(PointValueTimeModel model : models){
	    					model.setValue(imageServletBuilder.buildAndExpand(model.getTimestamp(), vo.getId()).toUri());
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
			value = "Query Time Range for Multiple Points",
			notes = "From time inclusive, To time exclusive. Return in single array",
			response=PointValueTimeModel.class,
			responseContainer="List"
			)
    @RequestMapping(method = RequestMethod.GET, value="/{xids}/multiple-points-single-array", produces={"application/json","text/csv"})
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValuesForMultiplePointsAsSingleArray(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xids", required = true, allowMultiple = true)
    		@PathVariable String[] xids,
    		
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
    		
    		Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
    		DataPointVO vo;
    		for(String xid : xids){
    			 vo = DataPointDao.instance.getByXid(xid);
    			 if(vo != null){
    				 if(Permissions.hasDataPointReadPermission(user, vo))
    					 pointIdMap.put(vo.getId(), vo);
    				 else{
    					 //Abort, invalid permissions
    					 result.addRestMessage(getUnauthorizedMessage());
    					 return result.createResponseEntity();
    				 }
    			 }
    		}
    		
    		//Do we have any valid points?
	    	if(pointIdMap.size() == 0){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{

    			//Are we using rollup
    			if((rollup != null)&&(rollup != RollupEnum.NONE)){
    				if(rollup == RollupEnum.FFT){
    					//Special Rollup for FFT's with no time rollup action
    					//TODO Need a way to return frequency or period values
//    					IdPointValueFftCalculator calc = new IdPointValueFftCalculator(pointIdMap, from.getTime(), to.getTime(), true);
//    					return result.createResponseEntity(calc);
    				}else{
	    				TimePeriod timePeriod = null;
	    				if((timePeriodType != null)&&(timePeriods != null)){
	    					timePeriod = new TimePeriod(timePeriods, timePeriodType);
	    				}
	    				IdPointValueRollupCalculator calc = new IdPointValueRollupCalculator(pointIdMap, useRendered, unitConversion, rollup, timePeriod, from.getTime(), to.getTime());
	    				return result.createResponseEntity(calc);
    				}
    				return result.createResponseEntity();
    			}else{
    				IdPointValueTimeDatabaseStream pvtDatabaseStream = new IdPointValueTimeDatabaseStream(request, pointIdMap, useRendered, unitConversion, from.getTime(), to.getTime(), this.dao);
	    			return result.createResponseEntity(pvtDatabaseStream);
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
			value = "Query Time Range for Multiple Points",
			notes = "From time inclusive, To time exclusive.  Returns a map of xid to values",
			response=PointValueTimeModel.class,
			responseContainer="List"
			)
    @RequestMapping(method = RequestMethod.GET, value="/{xids}/multiple-points-multiple-arrays", produces={"application/json","text/csv"})
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getPointValuesForMultiplePointsAsMultipleArrays(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xids", required = true, allowMultiple = true)
    		@PathVariable String[] xids,
    		
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
        
    	RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>> result = new RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
    		Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
    		DataPointVO vo;
    		for(String xid : xids){
    			 vo = DataPointDao.instance.getByXid(xid);
    			 if(vo != null){
    				 if(Permissions.hasDataPointReadPermission(user, vo))
    					 pointIdMap.put(vo.getId(), vo);
    				 else{
    					 //Abort, invalid permissions
    					 result.addRestMessage(getUnauthorizedMessage());
    					 return result.createResponseEntity();
    				 }
    			 }
    		}
    		
    		//Do we have any valid points?
	    	if(pointIdMap.size() == 0){
	    		result.addRestMessage(getDoesNotExistMessage());
	    		return result.createResponseEntity();
	    	}

	    	try{
    			//Are we using rollup
    			if((rollup != null)&&(rollup != RollupEnum.NONE)){
    				if(rollup == RollupEnum.FFT){
    					//Special Rollup for FFT's with no time rollup action
    					//TODO Need a way to return frequency or period values
//    					IdPointValueFftCalculator calc = new IdPointValueFftCalculator(pointIdMap, from.getTime(), to.getTime(), true);
//    					return result.createResponseEntity(calc);
    				}else{
	    				TimePeriod timePeriod = null;
	    				if((timePeriodType != null)&&(timePeriods != null)){
	    					timePeriod = new TimePeriod(timePeriods, timePeriodType);
	    				}
	    				XidPointValueMapRollupCalculator calc = new XidPointValueMapRollupCalculator(pointIdMap, useRendered, unitConversion, rollup, timePeriod, from.getTime(), to.getTime());
	    				return result.createResponseEntity(calc);
    				}
    				return result.createResponseEntity();
    			}else{
    				XidPointValueTimeMapDatabaseStream pvtDatabaseStream = new XidPointValueTimeMapDatabaseStream(request, pointIdMap, useRendered, unitConversion, from.getTime(), to.getTime(), this.dao);
	    			return result.createResponseEntity(pvtDatabaseStream);
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
    @RequestMapping(method = RequestMethod.GET, value="/{xid}", produces={"application/json","text/csv"})
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
	    			if((rollup != null)&&(rollup != RollupEnum.NONE)){
	    				if(rollup == RollupEnum.FFT){
	    					//Special Rollup for FFT's with no time rollup action
	    					//TODO Need a way to return frequency or period values
	    					PointValueFftCalculator calc = new PointValueFftCalculator(vo, from.getTime(), to.getTime(), true);
	    					return result.createResponseEntity(calc);
	    				}else{
		    				TimePeriod timePeriod = null;
		    				if((timePeriodType != null)&&(timePeriods != null)){
		    					timePeriod = new TimePeriod(timePeriods, timePeriodType);
		    				}
		    				PointValueRollupCalculator calc = new PointValueRollupCalculator(vo, useRendered, unitConversion, rollup, timePeriod, from.getTime(), to.getTime());
		    				return result.createResponseEntity(calc);
	    				}
	    			}else{
	    				PointValueTimeDatabaseStream pvtDatabaseStream = new PointValueTimeDatabaseStream(request, vo, useRendered, unitConversion, from.getTime(), to.getTime(), this.dao);
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
			value = "Count point values in a Time Range",
			notes = "From time inclusive, To time exclusive",
			response=PointValueTimeModel.class,
			responseContainer="List"
			)
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/count", produces={"application/json"})
    public ResponseEntity<Long> count(
    		HttpServletRequest request, 
    		
    		@ApiParam(value = "Point xid", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		
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
        
    	RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
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
	    			if((rollup != null)&&(rollup != RollupEnum.NONE)){
	    				//First check to see if there are any values in the range
	    				long pointValueCount = Common.databaseProxy.newPointValueDao().dateRangeCount(vo.getId(), from.getTime(), to.getTime());
	    				if(pointValueCount == 0)
	    					return result.createResponseEntity(pointValueCount);
	    				long count = new Long(0);
	    				TimePeriodBucketCalculator calc = new TimePeriodBucketCalculator(new DateTime(from), new DateTime(to), TimePeriodType.convertFrom(timePeriodType), timePeriods);
	    				while(calc.getNextPeriodTo().isBefore(calc.getEndTime())){
	    					count++;
	    				}
	    				return result.createResponseEntity(count);
	    			}else{
		    			long count = Common.databaseProxy.newPointValueDao().dateRangeCount(vo.getId(), from.getTime(), to.getTime());
		    			return result.createResponseEntity(count);
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
    @RequestMapping(method = RequestMethod.GET, value="/{xid}/statistics", produces={"application/json"})
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
			value = "Update an existing data point's value",
			notes = "Data point must exist and be enabled"
			)
	@RequestMapping(method = RequestMethod.PUT, value = "/{xid}", produces={"application/json"}, consumes={"application/json"})
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
			RestProcessResult<PointValueTimeModel> setResult = setPointValue(user, xid, model, unitConversion, builder);
			if(setResult.getHighestStatus().value() == HttpStatus.CREATED.value())
				return setResult.createResponseEntity(model);
			else
				return setResult.createResponseEntity();
		}else{
			return result.createResponseEntity();
		}
    }
	
	@ApiOperation(
			value = "Update one or many data point's current value",
			notes = "Each data point must exist and be enabled"
			)
	@RequestMapping(method = RequestMethod.PUT, produces={"application/json"}, consumes={"application/json"})
    public ResponseEntity<List<XidPointValueTimeModel>> putPointsValues(
    		HttpServletRequest request, 
    		@RequestBody List<XidPointValueTimeModel> models, 

    		@ApiParam(value = "Return converted value using displayed unit", required = false, defaultValue="false", allowMultiple = false)
    		@RequestParam(required=false, defaultValue="false") boolean unitConversion) throws RestValidationFailedException {
		
		RestProcessResult<List<XidPointValueTimeModel>> result = new RestProcessResult<List<XidPointValueTimeModel>>(HttpStatus.OK);
		List<XidPointValueTimeModel> setValues = new ArrayList<XidPointValueTimeModel>();
		
		User user = this.checkUser(request, result);
		if(result.isOk()){
		
			for(XidPointValueTimeModel model : models){
				RestProcessResult<PointValueTimeModel> pointResult = setPointValue(user, model.getXid(), model, unitConversion, ServletUriComponentsBuilder.fromContextPath(request));
				if(pointResult.getHighestStatus().value() == HttpStatus.CREATED.value()){
					//Save the model for later
					setValues.add(model);
				}
				for(RestMessage message : pointResult.getRestMessages()){
					result.addRestMessage(message);
				}
			}
			if(setValues.size() > 0)
				return result.createResponseEntity(setValues);
		}
		return result.createResponseEntity();
    }

	/**
	 * 
	 * Helper method for setting a point value
	 * 
	 * @param xid
	 * @param data
	 * @param unitConversion
	 * @return
	 */
	private RestProcessResult<PointValueTimeModel> setPointValue(User user, String xid,
			PointValueTimeModel model, boolean unitConversion, UriComponentsBuilder builder) {

		RestProcessResult<PointValueTimeModel> result = new RestProcessResult<PointValueTimeModel>(HttpStatus.OK);
		
        DataPointVO existingDp = DataPointDao.instance.getByXid(xid);
        if (existingDp == null) {
        	result.addRestMessage(getDoesNotExistMessage());
        	return result;
    	}
        
    	try{
    		if(Permissions.hasDataPointSetPermission(user, existingDp)){
    			
    			//Set the time to now if it is not present
    			if(model.getTimestamp() == 0){
    				model.setTimestamp(System.currentTimeMillis());
    			}
    			
    			//Validate the model's data type for compatibility
    			if(DataTypeEnum.convertFrom(model.getType()) != existingDp.getPointLocator().getDataTypeId()){
    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("event.ds.dataType"));
    				return result;
    			}
    			
    			//Validate the timestamp for future dated
    			if (model.getTimestamp() > System.currentTimeMillis() + SystemSettingsDao.getFutureDateLimit()) {
    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "Future dated points not acceptable."));
    				return result;
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
    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "[" + xid +"]Cannot perform unit conversion on Non Numeric data types."));
    					return result;
    				}
    			}
    			
    			//If we are a multistate point and our value is in string format then we should try to convert it
    			if((model.getType() == DataTypeEnum.MULTISTATE)&&(model.getValue() instanceof String)){
    				try{
	    				DataValue value = existingDp.getTextRenderer().parseText((String)model.getValue(), existingDp.getPointLocator().getDataTypeId());
	    				model.setValue(value.getObjectValue());
    				}catch(Exception e){
    					//Lots can go wrong here so let the user know
    					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "[" + xid +"]Unable to convert Multistate String representation to any known value."));
    				}
    			}
    			
    			
    			
		        final PointValueTime pvt;
				try{
					pvt = model.getData(); 
				}catch(Exception e){
					result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "[" + xid +"]Invalid Format"));
					return result;
				}
				
				//one last check to ensure we are inserting the correct data type
    			if(DataTypes.getDataType(pvt.getValue()) != existingDp.getPointLocator().getDataTypeId()){
    				result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("event.ds.dataType"));
    				return result;
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
    	        	URI location = builder.path("/v1/point-values/{xid}/{time}").buildAndExpand(xid, pvt.getTime()).toUri();
    		    	result.addRestMessage(getResourceCreatedMessage(location));
    		        return result;

    	        }catch(RTException e){
    	        	//Ok its probably not enabled or settable
    	        	result.addRestMessage(new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage("common.default", "[" + xid +"]" + e.getMessage())));
    	        	return result;
    	        }catch(Exception e){
    	        	LOG.error(e.getMessage(), e);
    	        	result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
    	        	return result;
    	        }
    		}else{
	    		result.addRestMessage(getUnauthorizedMessage());
	    		return result;
    		}
    	}catch(PermissionException e){
    		LOG.error(e.getMessage(), e);
    		result.addRestMessage(getUnauthorizedMessage());
    		return result;
    	}
	}
}
