/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.maintenanceEvents;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventRT;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.RTMDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Api(value="Maintenance Events API")
@RestController()
@RequestMapping("/v2/maintenance-events")
public class MaintenanceEventsRestController extends BaseMangoRestController {

    @ApiOperation(
            value = "Get maintenance event by XID",
            notes = "Only events that user has permission to are returned"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public MaintenanceEventModel get(
            @ApiParam(value = "Valid XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        MaintenanceEventVO vo = MaintenanceEventDao.instance.getFullByXid(xid);
        if (vo == null) {
            throw new NotFoundRestException();
        }
        ensureReadPermission(user, vo);
        return new MaintenanceEventModel(vo);
    }
    
    @ApiOperation(value = "Partially update an existing maintenance event")
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<MaintenanceEventModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MaintenanceEventModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO existing = MaintenanceEventDao.instance.getFullByXid(xid);
        if (existing == null)
            throw new NotFoundRestException();
        int id = existing.getId();

        MaintenanceEventModel existingModel = new MaintenanceEventModel(existing);
        existingModel.patch(model);
        MaintenanceEventVO vo = existingModel.toVO();
        vo.setId(id);
        ensureEditPermission(user, vo);
        
        vo.ensureValid();

        RTMDefinition.instance.saveMaintenanceEvent(vo);

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MaintenanceEventModel(vo), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Update an existing maintenance event")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<MaintenanceEventModel> update(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MaintenanceEventModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO existing = MaintenanceEventDao.instance.getFullByXid(xid);
        if (existing == null)
            throw new NotFoundRestException();
        int id = existing.getId();

        MaintenanceEventVO vo = model.toVO();
        vo.setId(id);
        ensureEditPermission(user, vo);
        
        vo.ensureValid();

        RTMDefinition.instance.saveMaintenanceEvent(vo);

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MaintenanceEventModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Create new maintenance event", notes="User must have global data source permission")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MaintenanceEventModel> create(
            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MaintenanceEventModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO vo = model.toVO();
        //User needs data source permission to create these
        Permissions.ensureDataSourcePermission(user);
        
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(MaintenanceEventDao.instance.generateUniqueXid());
        
        vo.ensureValid();

        RTMDefinition.instance.saveMaintenanceEvent(vo);

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MaintenanceEventModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Delete a maintenance event")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public MaintenanceEventModel delete(
            @ApiParam(value = "Valid maintenance event XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        MaintenanceEventVO vo = MaintenanceEventDao.instance.getByXid(xid);
        if (vo == null)
            throw new NotFoundRestException();

        ensureEditPermission(user, vo);
        RTMDefinition.instance.deleteMaintenanceEvent(vo.getId());
        return new MaintenanceEventModel(vo);
    }
    
    @ApiOperation(value = "Toggle the state of a maintenance event", notes="returns boolean state of event")
    @RequestMapping(method = RequestMethod.PUT, value = "/toggle/{xid}")
    public ResponseEntity<Boolean> toggle(
            @PathVariable String xid,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO existing = MaintenanceEventDao.instance.getFullByXid(xid);
        if (existing == null)
            throw new NotFoundRestException();
        
        ensureTogglePermission(user, existing);
        
        int id = existing.getId();

        MaintenanceEventRT rt = RTMDefinition.instance.getRunningMaintenanceEvent(id);
        boolean activated = false;
        if (rt == null)
            throw new BadRequestException(new TranslatableMessage("maintenanceEvents.toggle.disabled"));
        else
            activated = rt.toggle();

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(xid).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(activated, headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Query Maintenance Events",
            notes = "Use RQL formatted query",
            response=MaintenanceEventModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        ASTNode rql = parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }
    
    @ApiOperation(
            value = "Find Maintenance Events linked to data points",
            notes = "Returns a map of point ids to a list of events",
            response=Map.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/query/get-for-points/{pointIds}")
    public Map<Integer, List<MaintenanceEventModel>> getForPoints(
            @PathVariable(required = true) List<Integer> pointIds,
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        Map<Integer, List<MaintenanceEventModel>> map = new HashMap<>();
        for(Integer id: pointIds) {
            List<MaintenanceEventModel> models = new ArrayList<>();
            map.put(id, models);
            MaintenanceEventDao.instance.getForDataPoint(id, new MappedRowCallback<MaintenanceEventVO>() {

                @Override
                public void row(MaintenanceEventVO vo, int index) {
                    models.add(new MaintenanceEventModel(vo));
                }
                
            });
        }
        return map;
    }
    
    protected static StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        final Function<MaintenanceEventVO, Object> transformVisit = item -> {
            return new MaintenanceEventModel(item);
        };
        
        //If we are admin or have overall data source permission we can view all
        if (user.isAdmin() || Permissions.hasDataSourcePermission(user)) {
            return new StreamedVOQueryWithTotal<>(MaintenanceEventDao.instance, rql, transformVisit);
        } else {
            return new StreamedVOQueryWithTotal<>(MaintenanceEventDao.instance, rql, item -> {
                if(item.getDataPoints().size() > 0) {
                    DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                    MaintenanceEventDao.instance.getPoints(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                if(item.getDataSources().size() > 0) {
                    DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                    MaintenanceEventDao.instance.getDataSources(item.getId(), callback);
                    if(!callback.hasPermission.booleanValue())
                        return false;
                }
                return true;
            }, transformVisit);
        }
    }
    
    /**
     * Ensure the user has permission to toggle this event
     * @param user
     * @param vo
     */
    private void ensureTogglePermission(User user, MaintenanceEventVO vo) {
        if(user.isAdmin())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else if(!Permissions.hasPermission(user, vo.getTogglePermission()));
            throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToToggleEvent"), user);
    }
    
    /**
     * Ensure this user can read this VO
     * @param user
     * @param vo
     */
    private void ensureReadPermission(User user, MaintenanceEventVO vo) {
        if(user.isAdmin())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, true);
                MaintenanceEventDao.instance.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToReadPoints"), user);
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                MaintenanceEventDao.instance.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToEditSources"), user);
            }
        }
    }
    /**
     * Ensure the user can edit this VO
     * @param user
     * @param vo
     */
    private void ensureEditPermission(User user, MaintenanceEventVO vo) {
        if(user.isAdmin())
            return;
        else if(Permissions.hasDataSourcePermission(user))
            //TODO Review how this permission works
            return;
        else {
            if(vo.getDataPoints().size() > 0) {
                DataPointPermissionsCheckCallback callback = new DataPointPermissionsCheckCallback(user, false);
                MaintenanceEventDao.instance.getPoints(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToReadPoints"), user);
            }
            
            if(vo.getDataSources().size() > 0) {
                DataSourcePermissionsCheckCallback callback = new DataSourcePermissionsCheckCallback(user);
                MaintenanceEventDao.instance.getDataSources(vo.getId(), callback);
                if(!callback.hasPermission.booleanValue())
                    throw new PermissionException(new TranslatableMessage("maintenanceEvents.permission.unableToEditSources"), user);
            }
        }
    }
    
    /**
     * Check the permission on the data point and if the user does not have it
     * then cache and check the permission on the data source
     *
     * @author Terry Packer
     */
    static class DataPointPermissionsCheckCallback implements MappedRowCallback<DataPointVO> {

        Map<Integer, DataSourceVO<?>> sources = new HashMap<>();
        MutableBoolean hasPermission = new MutableBoolean(true);
        boolean read;
        User user;
        
        /**
         * 
         * @param read = true to check read permission, false = check edit permission
         */
        public DataPointPermissionsCheckCallback(User user, boolean read) {
            this.user = user;
            this.read = read;
        }
        
        /* (non-Javadoc)
         * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
         */
        @Override
        public void row(DataPointVO point, int index) {
            
            if(!hasPermission.getValue()) {
                //short circuit the logic if we already failed
                return;
            }else {
                if(read) {
                    if(!Permissions.hasDataPointReadPermission(user, point)) {
                        DataSourceVO<?> source = sources.computeIfAbsent(point.getDataSourceId(), k -> {
                            DataSourceVO<?> newDs = DataSourceDao.instance.get(k);
                            return newDs;
                        });
                        if(!Permissions.hasDataSourcePermission(user, source))
                            hasPermission.setFalse();
                    }
                }else {
                    DataSourceVO<?> source = sources.computeIfAbsent(point.getDataSourceId(), k -> {
                        DataSourceVO<?> newDs = DataSourceDao.instance.get(k);
                        return newDs;
                    });
                    if(!Permissions.hasDataSourcePermission(user, source))
                        hasPermission.setFalse();
                }
            }
        }
    }
    
    /**
     * Does the user have edit permission for all data sources
     *
     * @author Terry Packer
     */
    static class DataSourcePermissionsCheckCallback implements MappedRowCallback<DataSourceVO<?>> {

        MutableBoolean hasPermission = new MutableBoolean(true);
        User user;
        
        /**
         * 
         * @param read = true to check read permission, false = check edit permission
         */
        public DataSourcePermissionsCheckCallback(User user) {
            this.user = user;
        }
        
        /* (non-Javadoc)
         * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
         */
        @Override
        public void row(DataSourceVO<?> source, int index) {
            
            if(!hasPermission.getValue()) {
                //short circuit the logic if we already failed
                return;
            }else {
                if(!Permissions.hasDataSourcePermission(user, source))
                    hasPermission.setFalse();
            }
        }
    }
}
