/**
 * Copyright (C) 2017  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.ConditionSortLimitWithTagKeys;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Data Points", description="Data points")
@RestController(value="DataPointRestControllerV2")
@RequestMapping("/v2/data-points")
public class DataPointRestController {

	private static Log LOG = LogFactory.getLog(DataPointRestController.class);
	
	public DataPointRestController() {
		LOG.info("Creating Data Point v2 Rest Controller.");
	}

	@ApiOperation(
			value = "Get data point by XID",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public DataPointModel getDataPoint(
    		@ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
    		@PathVariable String xid,
    		@AuthenticationPrincipal User user) {
	    
	    DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
	    DataPointDao.instance.loadPartialRelationalData(dataPoint);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        
        checkDataPointReadPermission(user, dataPoint);
        return new DataPointModel(dataPoint);
    }

	@ApiOperation(
			value = "Get data point by ID",
			notes = "Returned as CSV or JSON, only points that user has read permission to are returned"
			)
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
    public DataPointModel getDataPointById(
    		@ApiParam(value = "Valid Data Point ID", required = true, allowMultiple = false)
    		@PathVariable int id,
    		@AuthenticationPrincipal User user) {

        DataPointVO dataPoint = DataPointDao.instance.get(id);
        DataPointDao.instance.loadPartialRelationalData(dataPoint);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }

        checkDataPointReadPermission(user, dataPoint);
        return new DataPointModel(dataPoint);
    }

    @ApiOperation(value = "Enable/disable/restart a data point")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public ResponseEntity<Void> enableDisable(
            @AuthenticationPrincipal User user,
            
            @PathVariable String xid,
            
            @ApiParam(value = "Enable or disable the data point", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,
            
            @ApiParam(value = "Restart the data point, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart) {

        DataPointVO dataPoint = DataPointDao.instance.getByXid(xid);
        if (dataPoint == null) {
            throw new NotFoundRestException();
        }
        
        checkDataPointEditPermission(user, dataPoint);

        if (enabled && restart) {
            Common.runtimeManager.restartDataPoint(dataPoint);
        } else {
            Common.runtimeManager.enableDataPoint(dataPoint, enabled);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@ApiOperation(
			value = "Query Data Points",
			notes = "",
			response=DataPointModel.class,
			responseContainer="Array"
			)
	@RequestMapping(method = RequestMethod.POST, value = "/query")
    public StreamedArrayWithTotal query(
    		@ApiParam(value="Query", required = true)
    		@RequestBody(required=true) ASTNode rql,
            @AuthenticationPrincipal User user) {

	    return doQuery(rql, user);
	}

	@ApiOperation(
			value = "Query Data Points",
			notes = "Use RQL formatted query",
			response=DataPointModel.class,
			responseContainer="List"
			)
	@RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request, 
            @AuthenticationPrincipal User user) {

        ASTNode rql = BaseMangoRestController.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
	}

    private static StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        if (user.isAdmin()) {
            return new StreamedVOQueryWithTotal<>(DataPointDao.instance, rql, item -> {
                DataPointDao.instance.loadPartialRelationalData(item);
                return new DataPointModel(item);
            });
        } else {
            // Add some conditions to restrict based on user permissions
            ConditionSortLimitWithTagKeys conditions = DataPointDao.instance.rqlToCondition(rql);
            conditions.addCondition(DataPointDao.instance.userHasPermission(user));

            DataPointFilter dataPointFilter = new DataPointFilter(user);
            
            return new StreamedVOQueryWithTotal<>(DataPointDao.instance, conditions, item -> {
                boolean oldFilterMatches = dataPointFilter.hasDataPointReadPermission(item);

                // this is just a double check, permissions should be accounted for via SQL restrictions added by DataPointDao.userHasPermission()
                if (!oldFilterMatches) {
                    throw new RuntimeException("Data point does not match old permission filter");
                }
                
                return true;
            }, item -> {
                DataPointDao.instance.loadPartialRelationalData(item);
                return new DataPointModel(item);
            });
        }
    }

    private static void checkDataPointReadPermission(User user, DataPointVO point) {
        boolean hasPermission;
        try {
            hasPermission = Permissions.hasDataPointReadPermission(user, point);
        } catch (PermissionException e) {
            hasPermission = false;
        }
        
        if (!hasPermission) {
            TranslatableMessage msg = new TranslatableMessage("rest.error.noReadPermissionForPoint", user.getUsername());
            throw new AccessDeniedException(msg);
        }
    }

    private static void checkDataPointEditPermission(User user, DataPointVO point) {
        try {
            Permissions.ensureDataSourcePermission(user, point.getDataSourceId());
        } catch (PermissionException e) {
            TranslatableMessage msg = new TranslatableMessage("rest.error.noEditPermissionForPoint", user.getUsername());
            throw new AccessDeniedException(msg);
        }
    }

}
