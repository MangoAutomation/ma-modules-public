/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueImportResult;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.SinglePointValueTimeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.pointValue.rollup.SingleNumericPointValueTimeRollupStream;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.XidPointValueTimeModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value = "Point Values", description = "Point Values")
@RestController("PointValueV2RestController")
@RequestMapping("/v2/point-values")
public class PointValueRestController extends AbstractMangoRestV2Controller{

    private final PointValueDao dao = Common.databaseProxy.newPointValueDao();
    
    @ApiOperation(
            value = "Query Time Range For 1 Data Point", 
            notes = "From time inclusive, To time exclusive",
            response = PointValueTimeModel.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ResponseEntity<QueryArrayStream<PointValueTimeModel>> getPointValues(
            HttpServletRequest request,

            @ApiParam(value = "Point xid", required = true,
                    allowMultiple = false) @PathVariable String xid,

            @ApiParam(value = "Return rendered value as String", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean useRendered,
            
            @ApiParam(value = "Return rendered value and raw value", required = false,
            defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                    defaultValue = "false") boolean bothRenderedAndRaw,

            @ApiParam(value = "Return converted value using displayed unit", required = false,
                    defaultValue = "false", allowMultiple = false) @RequestParam(required = false,
                            defaultValue = "false") boolean unitConversion,

            @ApiParam(value = "From time", required = false,
                    allowMultiple = false) @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime from,

            @ApiParam(value = "To time", required = false,
                    allowMultiple = false) @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime to,

            @ApiParam(value = "Rollup type", required = false, allowMultiple = false) @RequestParam(
                    value = "rollup", required = false, defaultValue="NONE") RollupEnum rollup,

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
            @RequestParam(value = "dateTimeFormat", required = false) String dateTimeFormat,
            @AuthenticationPrincipal User user) {

        TimePeriod timePeriod = null;
        if ((timePeriodType != null) && (timePeriods != null)) {
            timePeriod = new TimePeriod(timePeriods, timePeriodType);
        }
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(request.getServerName(), 
                request.getServerPort(), 
                from, to, dateTimeFormat, timezone, rollup, timePeriod, limit, 
                useRendered, unitConversion, bothRenderedAndRaw, false);
        
        DataPointVO vo = DataPointDao.instance.getByXid(xid);
        if (vo == null) {
            throw new NotFoundRestException();
        }else {
            if(!Permissions.hasDataPointReadPermission(user, vo))
                throw new AccessDeniedException();
        }
        
        Map<Integer, DataPointVO> voMap = new HashMap<Integer, DataPointVO>();
        voMap.put(vo.getId(), vo);
        // Are we using rollup
        if (rollup != RollupEnum.NONE) {
            return ResponseEntity.ok(new SingleNumericPointValueTimeRollupStream(info, voMap, this.dao));
        } else {
            return ResponseEntity.ok(new SinglePointValueTimeDatabaseStream(info, voMap, this.dao));
        }
    }
    
    @ApiOperation(
            value = "Import Point Values for one or many Data Points",
            notes = "Data Point must exist and user must have write access"
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Collection<PointValueImportResult>> savePointsValues(HttpServletRequest request,
            @RequestBody(required = true) List<XidPointValueTimeModel> models,
            @AuthenticationPrincipal User user
            ) {

        //Map of XIDs to results
        Map<String, PointValueImportResult> results = new HashMap<String, PointValueImportResult>();
        
        for(XidPointValueTimeModel model : models) {
            PointValueImportResult result = results.get(model.getXid());
            if(result == null) {
                result = new PointValueImportResult(model.getXid(), dao, user);
                results.put(model.getXid(), result);
            }
            //Attempt to save it
            result.saveValue(model);
        }
        
        return ResponseEntity.ok(results.values());
    }
    
}
