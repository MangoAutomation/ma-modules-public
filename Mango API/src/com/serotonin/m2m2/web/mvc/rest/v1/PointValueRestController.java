/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * 
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.net.URI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.v2.model.RestValidationResult;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.RTException;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.IAnnotated;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.TimePeriodBucketCalculator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValuesRequestModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.RecentPointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.TimeRangePointValuesRequestModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueMapRollupCalculator;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeLatestPointFacadeStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeMapDatabaseStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics.StatisticsStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.serotonin.m2m2.web.taglib.Functions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * TODO Use Point Value Facade for recent data access
 * 
 * @author Terry Packer
 * 
 */
@Api(value = "Point Values", description = "Point Values")
@RestController
@RequestMapping("/point-values")
public class PointValueRestController extends MangoRestController {

    private static Log LOG = LogFactory.getLog(PointValueRestController.class);
    private PointValueDao dao = Common.databaseProxy.newPointValueDao();


    /**
     * Get the latest point values for a point
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values Directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. For the most efficient use of this endpoint "
                    + " the data point's default cache size should be the size that you will typically query the latest values of.")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/latest")
    public ResponseEntity<List<RecentPointValueTimeModel>> getLatestPointValues(
            HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "Limit results", allowMultiple = false,
                    defaultValue = "100") @RequestParam(value = "limit",
                            defaultValue = "100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false,
                    defaultValue = "true") @RequestParam(value = "useCache",
                            defaultValue = "true") boolean useCache) {

        RestProcessResult<List<RecentPointValueTimeModel>> result =
                new RestProcessResult<List<RecentPointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {

            DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                if (Permissions.hasDataPointReadPermission(user, vo)) {
                    // Check to see if we can convert (Must be a Numeric Value)
                    if (unitConversion
                            && (vo.getPointLocator().getDataTypeId() != DataTypes.NUMERIC)) {
                        result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                                "common.default", "Can't convert non-numeric types."));
                        return result.createResponseEntity();
                    }

                    // If we are an image type we should build the URLS
                    UriComponentsBuilder imageServletBuilder =
                            UriComponentsBuilder.fromPath("/imageValue/hst{ts}_{id}.jpg");

                    List<RecentPointValueTimeModel> models;
                    if (useCache) {
                        // In an effort not to expand the PointValueCache we avoid the
                        // PointValueFacade
                        DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getId());
                        if (rt != null) {
                            List<PointValueTime> cache = rt.getCacheCopy();
                            if (limit < cache.size()) {
                                List<PointValueTime> pvts = cache.subList(0, limit);
                                models = new ArrayList<>(limit);
                                for (PointValueTime pvt : pvts)
                                    models.add(createRecentPointValueTimeModel(vo, pvt,
                                            imageServletBuilder, useRendered, unitConversion,
                                            true));
                            } else {
                                // We need to merge 2 lists
                                List<PointValueTime> disk = Common.databaseProxy.newPointValueDao()
                                        .getLatestPointValues(vo.getId(), limit);
                                Set<RecentPointValueTimeModel> all =
                                        new HashSet<RecentPointValueTimeModel>(limit);
                                for (PointValueTime pvt : cache)
                                    all.add(createRecentPointValueTimeModel(vo, pvt,
                                            imageServletBuilder, useRendered, unitConversion,
                                            true));
                                for (PointValueTime pvt : disk) {
                                    if (all.size() >= limit)
                                        break;
                                    else
                                        all.add(createRecentPointValueTimeModel(vo, pvt,
                                                imageServletBuilder, useRendered, unitConversion,
                                                false));
                                }
                                models = new ArrayList<>(all);
                                // Override the comparison method
                                Collections.sort(models,
                                        new Comparator<RecentPointValueTimeModel>() {
                                            // Compare such that data sets are returned in time
                                            // descending order
                                            // which turns out is opposite of compare to method for
                                            // PointValueTime objects
                                            @Override
                                            public int compare(RecentPointValueTimeModel o1,
                                                    RecentPointValueTimeModel o2) {
                                                if (o1.getTimestamp() < o2.getTimestamp())
                                                    return 1;
                                                if (o1.getTimestamp() > o2.getTimestamp())
                                                    return -1;
                                                return 0;
                                            }
                                        });
                            }

                        } else {
                            List<PointValueTime> pvts = Common.databaseProxy.newPointValueDao()
                                    .getLatestPointValues(vo.getId(), limit);
                            models = new ArrayList<>(limit);
                            for (PointValueTime pvt : pvts)
                                models.add(createRecentPointValueTimeModel(vo, pvt,
                                        imageServletBuilder, useRendered, unitConversion, false));
                        }
                    } else {
                        models = new ArrayList<>(limit);
                        List<PointValueTime> pvts = Common.databaseProxy.newPointValueDao()
                                .getLatestPointValues(vo.getId(), limit);
                        for (PointValueTime pvt : pvts)
                            models.add(createRecentPointValueTimeModel(vo, pvt, imageServletBuilder,
                                    useRendered, unitConversion, false));
                    }
                    return result.createResponseEntity(models);

                } else {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    /**
     * @param vo
     * @param pvt
     * @param useRendered
     * @param unitConversion
     * @param b
     * @return
     */
    private RecentPointValueTimeModel createRecentPointValueTimeModel(DataPointVO vo,
            PointValueTime pvt, UriComponentsBuilder imageServletBuilder, boolean useRendered,
            boolean unitConversion, boolean cached) {
        RecentPointValueTimeModel model;
        if (useRendered) {
            // Render the values as Strings with the suffix and or units
            model = new RecentPointValueTimeModel(pvt, cached);
            model.setType(DataTypeEnum.convertTo(pvt.getValue().getDataType()));
            model.setValue(Functions.getRenderedText(vo, pvt));
            model.setTimestamp(pvt.getTime());
            if (pvt instanceof IAnnotated)
                model.setAnnotation(
                        ((IAnnotated) pvt).getAnnotation(Common.getTranslations()));
        } else if (unitConversion) {
            // Convert the numeric value using the unit and rendered unit
            model = new RecentPointValueTimeModel(pvt, cached);
            model.setType(DataTypeEnum.convertTo(pvt.getValue().getDataType()));
            model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit())
                    .convert(pvt.getValue().getDoubleValue()));
            model.setTimestamp(pvt.getTime());
            if (pvt instanceof IAnnotated)
                model.setAnnotation(
                        ((IAnnotated) pvt).getAnnotation(Common.getTranslations()));
        } else {
            model = new RecentPointValueTimeModel(pvt, cached);
        }

        if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
            model.setValue(
                    imageServletBuilder.buildAndExpand(model.getTimestamp(), vo.getId()).toUri());

        return model;
    }

    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for all points on a data source directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns as single time ordered array.")
    @RequestMapping(method = RequestMethod.GET,
            value = "/{dataSourceXid}/latest-data-source-single-array")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getLatestPointValuesForDataSourceAsSingleArray(
            HttpServletRequest request,

            @ApiParam(value = "Data source xid", required = true,
                    allowMultiple = false) @PathVariable String dataSourceXid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "Limit results", allowMultiple = false,
                    defaultValue = "100") @RequestParam(value = "limit",
                            defaultValue = "100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false,
                    defaultValue = "true") @RequestParam(value = "useCache",
                            defaultValue = "true") boolean useCache,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            
            @ApiParam(value = "Time zone of output, used if formatted times are returned", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone
            ) {
        RestProcessResult<QueryArrayStream<PointValueTimeModel>> result =
                new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            DataSourceVO<?> ds = DataSourceDao.getInstance().getByXid(dataSourceXid);
            if (ds == null)
                throw new NotFoundRestException();

            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            List<DataPointVO> points =
                    DataPointDao.getInstance().getDataPointsForDataSourceStart(ds.getId());

            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(points.size());
            for (DataPointVO vo : points) {
                if (Permissions.hasDataPointReadPermission(user, vo))
                    pointIdMap.put(vo.getId(), vo);
                else {
                    // Abort, invalid permissions
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }

            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                IdPointValueTimeLatestPointValueFacadeStream pvtDatabaseStream =
                        new IdPointValueTimeLatestPointValueFacadeStream(pointIdMap, useRendered, unitConversion,
                                limit, useCache, dateTimeFormat, timezone);
                return result.createResponseEntity(pvtDatabaseStream);
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for all points on a data source directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns data as map of xid to values.")
    @RequestMapping(method = RequestMethod.GET,
            value = "/{dataSourceXid}/latest-data-source-multiple-arrays")
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getLatestPointValuesForDataSourceAsMultipleArrays(
            HttpServletRequest request,

            @ApiParam(value = "Data source xid", required = true,
                    allowMultiple = true) @PathVariable String dataSourceXid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "Limit results", allowMultiple = false,
                    defaultValue = "100") @RequestParam(value = "limit",
                            defaultValue = "100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false,
                    defaultValue = "true") @RequestParam(value = "useCache",
                            defaultValue = "true") boolean useCache,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            
            @ApiParam(value = "Time zone of output, used if formatted times are returned", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone) {

        RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>> result =
                new RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>>(
                        HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            DataSourceVO<?> ds = DataSourceDao.getInstance().getByXid(dataSourceXid);
            if (ds == null)
                throw new NotFoundRestException();

            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            List<DataPointVO> points =
                    DataPointDao.getInstance().getDataPointsForDataSourceStart(ds.getId());
            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(points.size());

            for (DataPointVO vo : points) {
                if (Permissions.hasDataPointReadPermission(user, vo))
                    pointIdMap.put(vo.getId(), vo);
                else {
                    // Abort, invalid permissions
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            }

            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                XidPointValueTimeLatestPointFacadeStream pvtDatabaseStream =
                        new XidPointValueTimeLatestPointFacadeStream(pointIdMap, useRendered, unitConversion,
                                limit, useCache, dateTimeFormat, timezone);
                return result.createResponseEntity(pvtDatabaseStream);
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns as single time ordered array.")
    @RequestMapping(method = RequestMethod.GET,
            value = "/{xids}/latest-multiple-points-single-array")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getLatestPointValuesForMultiplePointsAsSingleArray(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "Limit results", allowMultiple = false,
                    defaultValue = "100") @RequestParam(value = "limit",
                            defaultValue = "100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false,
                    defaultValue = "true") @RequestParam(value = "useCache",
                            defaultValue = "true") boolean useCache,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            
            @ApiParam(value = "Time zone of output, used if formatted times are returned", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone) {
        return latestPointValuesForMultiplePointsAsSingleArray(request, xids, useRendered,
                unitConversion, limit, useCache, dateTimeFormat, timezone);
    }

    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns as single time ordered array.")
    @RequestMapping(method = RequestMethod.POST, value = "/latest-multiple-points-single-array")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getLatestPointValuesForMultiplePointsAsSingleArrayAsPost(
            HttpServletRequest request,

            @ApiParam(value = "Point Values Request model", required = true,
                    allowMultiple = true) @RequestBody PointValuesRequestModel model) {
        return latestPointValuesForMultiplePointsAsSingleArray(request, model.getXids(),
                model.isUseRendered(), model.isUnitConversion(), model.getLimit(),
                model.isUseCache(), model.getDateTimeFormat(), model.getTimezone());
    }



    private ResponseEntity<QueryArrayStream<PointValueTimeModel>> latestPointValuesForMultiplePointsAsSingleArray(
            HttpServletRequest request, String[] xids, boolean useRendered, boolean unitConversion,
            int limit, boolean useCache, String dateTimeFormat, String timezone) {
        RestProcessResult<QueryArrayStream<PointValueTimeModel>> result =
                new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {

            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
            DataPointVO vo;
            for (String xid : xids) {
                vo = DataPointDao.getInstance().getByXid(xid);
                if (vo != null) {
                    if (Permissions.hasDataPointReadPermission(user, vo))
                        pointIdMap.put(vo.getId(), vo);
                    else {
                        // Abort, invalid permissions
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }
            }

            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                IdPointValueTimeLatestPointValueFacadeStream pvtDatabaseStream =
                        new IdPointValueTimeLatestPointValueFacadeStream(pointIdMap, useRendered, unitConversion,
                                limit, useCache, dateTimeFormat, timezone);
                return result.createResponseEntity(pvtDatabaseStream);
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }


    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns data as map of xid to values.")
    @RequestMapping(method = RequestMethod.GET,
            value = "/{xids}/latest-multiple-points-multiple-arrays")
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getLatestPointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "Limit results", allowMultiple = false,
                    defaultValue = "100") @RequestParam(value = "limit",
                            defaultValue = "100") int limit,

            @ApiParam(value = "Return cached data?", allowMultiple = false,
                    defaultValue = "true") @RequestParam(value = "useCache",
                            defaultValue = "true") boolean useCache,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            
            @ApiParam(value = "Time zone of output, used if formatted times are returned", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone
            ) {
        return latestPointValuesForMultiplePointsAsMultipleArrays(request, xids, useRendered,
                unitConversion, limit, useCache, dateTimeFormat, timezone);
    }

    /**
     * Get the latest point values a set of points return as map of xid to array of values
     * 
     * @param xid
     * @param limit
     * @return
     */
    @ApiOperation(
            value = "Get Latest Point Values for multiple points directly from the Runtime Manager, this makes Cached and Intra-Interval data available.",
            notes = "Default limit 100, time descending order, Default to return cached data. Returns data as map of xid to values.")
    @RequestMapping(method = RequestMethod.POST, value = "/latest-multiple-points-multiple-arrays")
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getLatestPointValuesForMultiplePointsAsMultipleArraysAsPost(
            HttpServletRequest request,

            @ApiParam(value = "Point Values Request model", required = true,
                    allowMultiple = true) @RequestBody PointValuesRequestModel model) {
        return latestPointValuesForMultiplePointsAsMultipleArrays(request, model.getXids(),
                model.isUseRendered(), model.isUnitConversion(), model.getLimit(),
                model.isUseCache(), model.getDateTimeFormat(), model.getTimezone());
    }

    private ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> latestPointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request, String[] xids, boolean useRendered, boolean unitConversion,
            int limit, boolean useCache, String dateTimeFormat, String timezone) {
        RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>> result =
                new RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>>(
                        HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
            DataPointVO vo;
            for (String xid : xids) {
                vo = DataPointDao.getInstance().getByXid(xid);
                if (vo != null) {
                    if (Permissions.hasDataPointReadPermission(user, vo))
                        pointIdMap.put(vo.getId(), vo);
                    else {
                        // Abort, invalid permissions
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }
            }

            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                XidPointValueTimeLatestPointFacadeStream pvtDatabaseStream =
                        new XidPointValueTimeLatestPointFacadeStream(pointIdMap, useRendered, unitConversion,
                                limit, useCache, dateTimeFormat, timezone);
                return result.createResponseEntity(pvtDatabaseStream);
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }


    @ApiOperation(value = "First and last point values",
            notes = "Retrieves the first and last point values within a time range, used to read accumulators")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/first-last")
    public ResponseEntity<List<PointValueTimeModel>> firstAndLastPointValues(
            HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false, allowMultiple = false) @RequestParam(
                    value = "from",
                    required = false) @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) @RequestParam(
                    value = "to",
                    required = false) @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,
            
            @ApiParam(value = "Time zone of output, used if formatted times are returned", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone) {
        RestProcessResult<List<PointValueTimeModel>> result =
                new RestProcessResult<List<PointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                if (Permissions.hasDataPointReadPermission(user, vo)) {
                    long current = Common.timer.currentTimeMillis();
                    if (from == null)
                        from = new DateTime(current);
                    if (to == null)
                        to = new DateTime(current);
                    
                    // could also get the user's timezone if parameter was not supplied but probably
                    // better not to for RESTfulness
                    if (timezone != null) {
                        DateTimeZone zone = DateTimeZone.forID(timezone);
                        from = from.withZone(zone);
                        to = to.withZone(zone);
                    }
                    PointValueFacade pointValueFacade = new PointValueFacade(vo.getId(), false);
                    PointValueTime first = pointValueFacade.getPointValueAfter(from.getMillis());
                    PointValueTime last = pointValueFacade.getPointValueBefore(to.getMillis());

                    List<PointValueTimeModel> models = new ArrayList<PointValueTimeModel>(2);
                    if (useRendered) {
                        if (first != null) {
                            PointValueTimeModel model = new PointValueTimeModel();
                            model.setType(DataTypeEnum.convertTo(first.getValue().getDataType()));
                            model.setValue(Functions.getRenderedText(vo, first));
                            model.setTimestamp(first.getTime());
                            if (first instanceof IAnnotated)
                                model.setAnnotation(((IAnnotated) first)
                                        .getAnnotation(Common.getTranslations()));
                            models.add(model);
                        }
                        if (last != null) {
                            PointValueTimeModel model = new PointValueTimeModel();
                            model.setType(DataTypeEnum.convertTo(last.getValue().getDataType()));
                            model.setValue(Functions.getRenderedText(vo, last));
                            model.setTimestamp(last.getTime());
                            if (last instanceof IAnnotated)
                                model.setAnnotation(((IAnnotated) last)
                                        .getAnnotation(Common.getTranslations()));
                            models.add(model);
                        }
                    } else if (unitConversion) {
                        if (first != null) {
                            PointValueTimeModel model = new PointValueTimeModel();
                            model.setType(DataTypeEnum.convertTo(first.getValue().getDataType()));
                            model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit())
                                    .convert(first.getValue().getDoubleValue()));
                            model.setTimestamp(first.getTime());
                            if (first instanceof IAnnotated)
                                model.setAnnotation(((IAnnotated) first)
                                        .getAnnotation(Common.getTranslations()));
                            models.add(model);
                        }
                        if (last != null) {
                            PointValueTimeModel model = new PointValueTimeModel();
                            model.setType(DataTypeEnum.convertTo(last.getValue().getDataType()));
                            model.setValue(vo.getUnit().getConverterTo(vo.getRenderedUnit())
                                    .convert(last.getValue().getDoubleValue()));
                            model.setTimestamp(last.getTime());
                            if (last instanceof IAnnotated)
                                model.setAnnotation(((IAnnotated) last)
                                        .getAnnotation(Common.getTranslations()));
                            models.add(model);
                        }
                    } else {
                        models.add(first == null ? null : new PointValueTimeModel(first));
                        models.add(last == null ? null : new PointValueTimeModel(last));
                    }

                    if (vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE) {
                        // If we are an image type we should build the URLS
                        UriComponentsBuilder imageServletBuilder =
                                UriComponentsBuilder.fromPath("/imageValue/hst{ts}_{id}.jpg");

                        for (PointValueTimeModel model : models) {
                            model.setValue(imageServletBuilder
                                    .buildAndExpand(model.getTimestamp(), vo.getId()).toUri());
                        }
                    }

                    return result.createResponseEntity(models);
                } else {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Query Time Range for Multiple Points",
            notes = "From time inclusive, To time exclusive. Return in single array, use limit if provided",
            response = PointValueTimeModel.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = "/{xids}/multiple-points-single-array")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValuesForMultiplePointsAsSingleArray(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) @RequestParam(
                    value = "rollup", required = false) RollupEnum rollup,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriodType",
                            required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriods",
                            required = false) Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit", required = false, allowMultiple = false) @RequestParam(
                    value = "limit", required = false) Integer limit,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
            required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat) {
        return pointValuesForMultiplePointsAsSingleArray(request, xids, useRendered, unitConversion,
                from, to, rollup, timePeriodType, timePeriods, timezone, limit, dateTimeFormat);
    }

    @ApiOperation(value = "Query Time Range for Multiple Points",
            notes = "From time inclusive, To time exclusive. Return in single array, use limit if provided",
            response = PointValueTimeModel.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.POST, value = "/multiple-points-single-array")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValuesForMultiplePointsAsSingleArrayViaPost(
            HttpServletRequest request,

            @ApiParam(value = "Time Range Point Values Request model", required = true,
                    allowMultiple = true) @RequestBody TimeRangePointValuesRequestModel model) {
        return pointValuesForMultiplePointsAsSingleArray(request, model.getXids(),
                model.isUseRendered(), model.isUnitConversion(), model.getFromAsDateTime(),
                model.getToAsDateTime(), model.getRollup(), model.getTimePeriodType(),
                model.getTimePeriods(), model.getTimezone(), model.getLimit(),
                model.getDateTimeFormat());
    }

    private ResponseEntity<QueryArrayStream<PointValueTimeModel>> pointValuesForMultiplePointsAsSingleArray(
            HttpServletRequest request, String[] xids, boolean useRendered, boolean unitConversion,
            DateTime from, DateTime to, RollupEnum rollup, TimePeriodType timePeriodType,
            Integer timePeriods, String timezone, Integer limit, String dateTimeFormat) {
        RestProcessResult<QueryArrayStream<PointValueTimeModel>> result =
                new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            
            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
            DataPointVO vo;
            for (String xid : xids) {
                vo = DataPointDao.getInstance().getByXid(xid);
                if (vo != null) {
                    if (Permissions.hasDataPointReadPermission(user, vo))
                        pointIdMap.put(vo.getId(), vo);
                    else {
                        // Abort, invalid permissions
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                long current = Common.timer.currentTimeMillis();
                if (from == null)
                    from = new DateTime(current);
                if (to == null)
                    to = new DateTime(current);

                // could also get the user's timezone if parameter was not supplied but probably
                // better not to for RESTfulness
                if (timezone != null) {
                    DateTimeZone zone = DateTimeZone.forID(timezone);
                    from = from.withZone(zone);
                    to = to.withZone(zone);
                }

                // Are we using rollup
                if ((rollup != null) && (rollup != RollupEnum.NONE)) {
                    if (rollup == RollupEnum.FFT) {
                        // Special Rollup for FFT's with no time rollup action
                        // TODO Need a way to return frequency or period values
                        // IdPointValueFftCalculator calc = new
                        // IdPointValueFftCalculator(pointIdMap, from.getTime(), to.getTime(),
                        // true);
                        // return result.createResponseEntity(calc);
                    } else {
                        TimePeriod timePeriod = null;
                        if ((timePeriodType != null) && (timePeriods != null)) {
                            timePeriod = new TimePeriod(timePeriods, timePeriodType);
                        }
                        IdPointValueRollupCalculator calc = new IdPointValueRollupCalculator(pointIdMap,
                                useRendered, unitConversion, rollup, timePeriod, from, to, limit,
                                dateTimeFormat, timezone);
                        return result.createResponseEntity(calc);
                    }
                    return result.createResponseEntity();
                } else {
                    IdPointValueTimeDatabaseStream pvtDatabaseStream =
                            new IdPointValueTimeDatabaseStream(pointIdMap, useRendered,
                                    unitConversion, from.getMillis(), to.getMillis(), this.dao,
                                    limit, dateTimeFormat, timezone);
                    return result.createResponseEntity(pvtDatabaseStream);
                }


            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Query Time Range for Multiple Points",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to values with optionally limited value arrays",
            response = PointValueTimeModel.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = "/{xids}/multiple-points-multiple-arrays")
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getPointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request,

            @ApiParam(value = "Point xids", required = true,
                    allowMultiple = true) @PathVariable String[] xids,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) @RequestParam(
                    value = "rollup", required = false) RollupEnum rollup,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriodType",
                            required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriods",
                            required = false) Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit", required = false, allowMultiple = false) @RequestParam(
                    value = "limit", required = false) Integer limit,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat) {
        return pointValuesForMultiplePointsAsMultipleArrays(request, xids, useRendered,
                unitConversion, from, to, rollup, timePeriodType, timePeriods, timezone, limit,
                dateTimeFormat);
    }

    @ApiOperation(value = "Query Time Range for Multiple Points",
            notes = "From time inclusive, To time exclusive.  Returns a map of xid to values with optionally limited value arrays",
            response = PointValueTimeModel.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.POST, value = "/multiple-points-multiple-arrays")
    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> getPointValuesForMultiplePointsAsMultipleArraysAsPost(
            HttpServletRequest request,

            @ApiParam(value = "Time Range Point Values Request model", required = true,
                    allowMultiple = true) @RequestBody TimeRangePointValuesRequestModel model) {
        return pointValuesForMultiplePointsAsMultipleArrays(request, model.getXids(),
                model.isUseRendered(), model.isUnitConversion(), model.getFromAsDateTime(),
                model.getToAsDateTime(), model.getRollup(), model.getTimePeriodType(),
                model.getTimePeriods(), model.getTimezone(), model.getLimit(),
                model.getDateTimeFormat());
    }

    public ResponseEntity<ObjectStream<Map<String, List<PointValueTime>>>> pointValuesForMultiplePointsAsMultipleArrays(
            HttpServletRequest request, String[] xids, boolean useRendered, boolean unitConversion,
            DateTime from, DateTime to, RollupEnum rollup, TimePeriodType timePeriodType,
            Integer timePeriods, String timezone, Integer limit, String dateTimeFormat) {
        RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>> result =
                new RestProcessResult<ObjectStream<Map<String, List<PointValueTime>>>>(
                        HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            Map<Integer, DataPointVO> pointIdMap = new HashMap<Integer, DataPointVO>(xids.length);
            DataPointVO vo;
            for (String xid : xids) {
                vo = DataPointDao.getInstance().getByXid(xid);
                if (vo != null) {
                    if (Permissions.hasDataPointReadPermission(user, vo))
                        pointIdMap.put(vo.getId(), vo);
                    else {
                        // Abort, invalid permissions
                        result.addRestMessage(getUnauthorizedMessage());
                        return result.createResponseEntity();
                    }
                }
            }

            // Do we have any valid points?
            if (pointIdMap.size() == 0) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                long current = Common.timer.currentTimeMillis();
                if (from == null)
                    from = new DateTime(current);
                if (to == null)
                    to = new DateTime(current);

                // could also get the user's timezone if parameter was not supplied but probably
                // better not to for RESTfulness
                if (timezone != null) {
                    DateTimeZone zone = DateTimeZone.forID(timezone);
                    from = from.withZone(zone);
                    to = to.withZone(zone);
                }

                // Are we using rollup
                if ((rollup != null) && (rollup != RollupEnum.NONE)) {
                    if (rollup == RollupEnum.FFT) {
                        // Special Rollup for FFT's with no time rollup action
                        // TODO Need a way to return frequency or period values
                        // IdPointValueFftCalculator calc = new
                        // IdPointValueFftCalculator(pointIdMap, from.getTime(), to.getTime(),
                        // true);
                        // return result.createResponseEntity(calc);
                    } else {
                        TimePeriod timePeriod = null;
                        if ((timePeriodType != null) && (timePeriods != null)) {
                            timePeriod = new TimePeriod(timePeriods, timePeriodType);
                        }
                        XidPointValueMapRollupCalculator calc =
                                new XidPointValueMapRollupCalculator(pointIdMap, useRendered,
                                        unitConversion, rollup, timePeriod, from, to, limit,
                                        dateTimeFormat, timezone);
                        return result.createResponseEntity(calc);
                    }
                    return result.createResponseEntity();
                } else {
                    XidPointValueTimeMapDatabaseStream pvtDatabaseStream =
                            new XidPointValueTimeMapDatabaseStream(pointIdMap, useRendered,
                                    unitConversion, from.getMillis(), to.getMillis(), this.dao,
                                    limit, dateTimeFormat, timezone);
                    return result.createResponseEntity(pvtDatabaseStream);
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Query Time Range", notes = "From time inclusive, To time exclusive",
            response = PointValueTimeModel.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValues(
            HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) @RequestParam(
                    value = "rollup", required = false) RollupEnum rollup,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriodType",
                            required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriods",
                            required = false) Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,

            @ApiParam(value = "Limit", required = false, allowMultiple = false) @RequestParam(
                    value = "limit", required = false) Integer limit,

            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat) {

        RestProcessResult<QueryArrayStream<PointValueTimeModel>> result =
                new RestProcessResult<QueryArrayStream<PointValueTimeModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {

            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalid", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                if (Permissions.hasDataPointReadPermission(user, vo)) {
                    long current = Common.timer.currentTimeMillis();
                    if (from == null)
                        from = new DateTime(current);
                    if (to == null)
                        to = new DateTime(current);

                    // could also get the user's timezone if parameter was not supplied but probably
                    // better not to for RESTfulness
                    if (timezone != null) {
                        DateTimeZone zone = DateTimeZone.forID(timezone);
                        from = from.withZone(zone);
                        to = to.withZone(zone);
                    }

                    // Are we using rollup
                    if ((rollup != null) && (rollup != RollupEnum.NONE)) {
                        if (rollup == RollupEnum.FFT) {
                            // Special Rollup for FFT's with no time rollup action
                            // TODO Need a way to return frequency or period values
                            PointValueFftCalculator calc = new PointValueFftCalculator(vo,
                                    from.getMillis(), to.getMillis(), true);
                            return result.createResponseEntity(calc);
                        } else {
                            TimePeriod timePeriod = null;
                            if ((timePeriodType != null) && (timePeriods != null)) {
                                timePeriod = new TimePeriod(timePeriods, timePeriodType);
                            }
                            PointValueRollupCalculator calc = new PointValueRollupCalculator(vo,
                                    useRendered, unitConversion, rollup, timePeriod, from, to,
                                    limit, dateTimeFormat, timezone);
                            return result.createResponseEntity(calc);
                        }
                    } else {
                        PointValueTimeDatabaseStream pvtDatabaseStream =
                                new PointValueTimeDatabaseStream(vo, useRendered, unitConversion,
                                        from.getMillis(), to.getMillis(), this.dao, limit,
                                        dateTimeFormat, timezone);
                        return result.createResponseEntity(pvtDatabaseStream);
                    }

                } else {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Count point values in a Time Range",
            notes = "From time inclusive, To time exclusive", response = PointValueTimeModel.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/count")
    public ResponseEntity<Long> count(HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date from,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            // Not working yet@DateTimeFormat(pattern = "${rest.customDateInputFormat}") Date to,
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) @RequestParam(
                    value = "rollup", required = false) RollupEnum rollup,

            @ApiParam(value = "Time Period Type", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriodType",
                            required = false) TimePeriodType timePeriodType,

            @ApiParam(value = "Time Periods", required = false,
                    allowMultiple = false) @RequestParam(value = "timePeriods",
                            required = false) Integer timePeriods,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone) {

        RestProcessResult<Long> result = new RestProcessResult<Long>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }
            
            try {
                if (Permissions.hasDataPointReadPermission(user, vo)) {
                    long current = Common.timer.currentTimeMillis();
                    if (from == null)
                        from = new DateTime(current);
                    if (to == null)
                        to = new DateTime(current);

                    // could also get the user's timezone if parameter was not supplied but probably
                    // better not to for RESTfulness
                    if (timezone != null) {
                        DateTimeZone zone = DateTimeZone.forID(timezone);
                        from = from.withZone(zone);
                        to = to.withZone(zone);
                    }

                    // Are we using rollup
                    if ((rollup != null) && (rollup != RollupEnum.NONE)) {
                        // First check to see if there are any values in the range
                        long pointValueCount = Common.databaseProxy.newPointValueDao()
                                .dateRangeCount(vo.getId(), from.getMillis(), to.getMillis());
                        if (pointValueCount == 0)
                            return result.createResponseEntity(pointValueCount);
                        long count = new Long(0);
                        TimePeriodBucketCalculator calc = new TimePeriodBucketCalculator(from, to,
                                TimePeriodType.convertFrom(timePeriodType), timePeriods);
                        while (calc.getNextPeriodTo().isBefore(calc.getEndTime())) {
                            count++;
                        }
                        return result.createResponseEntity(count);
                    } else {
                        long count = Common.databaseProxy.newPointValueDao()
                                .dateRangeCount(vo.getId(), from.getMillis(), to.getMillis());
                        return result.createResponseEntity(count);
                    }

                } else {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Get Point Statistics", notes = "From time inclusive, To time exclusive"
    // TODO Implement a Statistics Model for the stream and put as response class here
    )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/statistics")
    public ResponseEntity<StatisticsStream> getPointStatistics(HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false) 
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false) 
            @DateTimeFormat(iso = ISO.DATE_TIME) DateTime to,
            @ApiParam(value = "Time zone", required = false, allowMultiple = false) @RequestParam(
                    value = "timezone", required = false) String timezone,
            
            @ApiParam(value = "Date Time format pattern for timestamps as strings, if not included epoch milli number is used",
                    required = false, allowMultiple = false) 
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat
            ) {

        RestProcessResult<StatisticsStream> result =
                new RestProcessResult<StatisticsStream>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            if(dateTimeFormat != null) {
                try {
                    DateTimeFormatter.ofPattern(dateTimeFormat);
                }catch(IllegalArgumentException e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "dateTimeFormat");
                    throw new ValidationFailedRestException(vr);
                }
            }
            if(timezone != null) {
                try{
                    ZoneId.of(timezone);
                }catch(Exception e) {
                    RestValidationResult vr = new RestValidationResult();
                    vr.addError("validate.invalidValue", "timezone");
                    throw new ValidationFailedRestException(vr);
                }
            }
            DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
            if (vo == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            }

            try {
                if (Permissions.hasDataPointReadPermission(user, vo)) {
                    long current = Common.timer.currentTimeMillis();
                    if (from == null)
                        from = new DateTime(current);
                    if (to == null)
                        to = new DateTime(current);
                    
                    // could also get the user's timezone if parameter was not supplied but probably
                    // better not to for RESTfulness
                    if (timezone != null) {
                        DateTimeZone zone = DateTimeZone.forID(timezone);
                        from = from.withZone(zone);
                        to = to.withZone(zone);
                    }
                    
                    StatisticsStream stream = new StatisticsStream(vo, useRendered, unitConversion,
                            from.getMillis(), to.getMillis(), dateTimeFormat, timezone);
                    return result.createResponseEntity(stream);
                } else {
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }
            } catch (PermissionException e) {
                LOG.error(e.getMessage(), e);
                result.addRestMessage(getUnauthorizedMessage());
                return result.createResponseEntity();
            }
        } else {
            return result.createResponseEntity();
        }
    }

    /**
     * Update a point value in the system
     * 
     * @param pvt
     * @param xid
     * @param builder
     * @return
     * @throws RestValidationFailedException
     */
    @ApiOperation(value = "Update an existing data point's value",
            notes = "Data point must exist and be enabled")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<PointValueTimeModel> putPointValue(HttpServletRequest request,
            @RequestBody(required = true) PointValueTimeModel model, @PathVariable String xid,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            UriComponentsBuilder builder) throws RestValidationFailedException {

        RestProcessResult<PointValueTimeModel> result =
                new RestProcessResult<PointValueTimeModel>(HttpStatus.OK);

        User user = this.checkUser(request, result);
        if (result.isOk()) {
            RestProcessResult<PointValueTimeModel> setResult =
                    setPointValue(user, xid, model, unitConversion, builder);
            if (setResult.getHighestStatus().value() == HttpStatus.CREATED.value())
                return setResult.createResponseEntity(model);
            else
                return setResult.createResponseEntity();
        } else {
            return result.createResponseEntity();
        }
    }

    @ApiOperation(value = "Update one or many data point's current value",
            notes = "Each data point must exist and be enabled")
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<List<XidPointValueTimeModel>> putPointsValues(HttpServletRequest request,
            @RequestBody(required = true) List<XidPointValueTimeModel> models,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion)
            throws RestValidationFailedException {

        RestProcessResult<List<XidPointValueTimeModel>> result =
                new RestProcessResult<List<XidPointValueTimeModel>>(HttpStatus.OK);
        List<XidPointValueTimeModel> setValues = new ArrayList<XidPointValueTimeModel>();

        User user = this.checkUser(request, result);
        if (result.isOk()) {

            for (XidPointValueTimeModel model : models) {
                RestProcessResult<PointValueTimeModel> pointResult =
                        setPointValue(user, model.getXid(), model, unitConversion,
                                ServletUriComponentsBuilder.fromContextPath(request));
                if (pointResult.getHighestStatus().value() == HttpStatus.CREATED.value()) {
                    // Save the model for later
                    setValues.add(model);
                }
                for (RestMessage message : pointResult.getRestMessages()) {
                    result.addRestMessage(message);
                }
            }
            if (setValues.size() > 0)
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

        RestProcessResult<PointValueTimeModel> result =
                new RestProcessResult<PointValueTimeModel>(HttpStatus.OK);

        DataPointVO existingDp = DataPointDao.getInstance().getByXid(xid);
        if (existingDp == null) {
            result.addRestMessage(getDoesNotExistMessage());
            return result;
        }

        try {
            if (Permissions.hasDataPointSetPermission(user, existingDp)) {

                // Set the time to now if it is not present
                if (model.getTimestamp() == 0) {
                    model.setTimestamp(Common.timer.currentTimeMillis());
                }

                // Validate the model's data type for compatibility
                if (DataTypeEnum.convertFrom(model.getType()) != existingDp.getPointLocator()
                        .getDataTypeId()) {
                    result.addRestMessage(HttpStatus.NOT_ACCEPTABLE,
                            new TranslatableMessage("event.ds.dataType"));
                    return result;
                }

                // Validate the timestamp for future dated
                if (model.getTimestamp() > Common.timer.currentTimeMillis()
                        + SystemSettingsDao.instance.getFutureDateLimit()) {
                    result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                            "common.default", "Future dated points not acceptable."));
                    return result;
                }

                // TODO Backdate validation?
                // boolean backdated = pointValue != null && newValue.getTime() <
                // pointValue.getTime();

                // Are we converting from the rendered Unit?
                if (unitConversion) {
                    if ((model.getType() == DataTypeEnum.NUMERIC)
                            && (model.getValue() instanceof Number)) {
                        double value;
                        if (model.getValue() instanceof Integer) {
                            value = (double) ((Integer) model.getValue());
                        } else {
                            value = (double) ((Double) model.getValue());
                        }
                        model.setValue(existingDp.getRenderedUnit()
                                .getConverterTo(existingDp.getUnit()).convert(value));
                    } else {
                        result.addRestMessage(HttpStatus.NOT_ACCEPTABLE,
                                new TranslatableMessage("common.default", "[" + xid
                                        + "]Cannot perform unit conversion on Non Numeric data types."));
                        return result;
                    }
                }

                // If we are a multistate point and our value is in string format then we should try
                // to convert it
                if ((model.getType() == DataTypeEnum.MULTISTATE)
                        && (model.getValue() instanceof String)) {
                    try {
                        DataValue value =
                                existingDp.getTextRenderer().parseText((String) model.getValue(),
                                        existingDp.getPointLocator().getDataTypeId());
                        model.setValue(value.getObjectValue());
                    } catch (Exception e) {
                        // Lots can go wrong here so let the user know
                        result.addRestMessage(HttpStatus.NOT_ACCEPTABLE,
                                new TranslatableMessage("common.default", "[" + xid
                                        + "]Unable to convert Multistate String representation to any known value."));
                    }
                }



                final PointValueTime pvt;
                try {
                    pvt = model.getData();
                } catch (Exception e) {
                    result.addRestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                            "common.default", "[" + xid + "]Invalid Format"));
                    return result;
                }

                // one last check to ensure we are inserting the correct data type
                if (DataTypes.getDataType(pvt.getValue()) != existingDp.getPointLocator()
                        .getDataTypeId()) {
                    result.addRestMessage(HttpStatus.NOT_ACCEPTABLE,
                            new TranslatableMessage("event.ds.dataType"));
                    return result;
                }

                final int dataSourceId = existingDp.getDataSourceId();
                SetPointSource source = null;
                if (model.getAnnotation() != null) {
                    source = new SetPointSource() {

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
                            return ((AnnotatedPointValueTime) pvt).getSourceMessage();
                        }

                        @Override
                        public void raiseRecursionFailureEvent() {
                            LOG.error("Recursive failure while setting point via REST");
                        }

                    };
                }
                try {
                    Common.runtimeManager.setDataPointValue(existingDp.getId(), pvt, source);
                    // This URI may not always be accurate if the Data Source doesn't use the
                    // provided time...
                    URI location = builder.path("/point-values/{xid}/{time}")
                            .buildAndExpand(xid, pvt.getTime()).toUri();
                    result.addRestMessage(getResourceCreatedMessage(location));
                    return result;

                } catch (RTException e) {
                    // Ok its probably not enabled or settable
                    result.addRestMessage(
                            new RestMessage(HttpStatus.NOT_ACCEPTABLE, new TranslatableMessage(
                                    "common.default", "[" + xid + "]" + e.getMessage())));
                    return result;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                    return result;
                }
            } else {
                result.addRestMessage(getUnauthorizedMessage());
                return result;
            }
        } catch (PermissionException e) {
            LOG.error(e.getMessage(), e);
            result.addRestMessage(getUnauthorizedMessage());
            return result;
        }
    }
}
