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
import com.infiniteautomation.mango.service.maintenanceEvents.MaintenanceEventsService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventRT;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.maintenanceEvents.RTMDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;

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
public class MaintenanceEventsRestController {

    private MaintenanceEventsService service;
    
    public MaintenanceEventsRestController() {
        this.service = new MaintenanceEventsService();
    }
    
    @ApiOperation(
            value = "Get maintenance event by XID",
            notes = "Only events that user has permission to are returned"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public MaintenanceEventModel get(
            @ApiParam(value = "Valid XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return new MaintenanceEventModel(service.getFullByXid(xid, user));
    }
    
    @ApiOperation(value = "Partially update an existing maintenance event")
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<MaintenanceEventModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MaintenanceEventModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO existing = service.getFullByXid(xid, user);
        MaintenanceEventModel existingModel = new MaintenanceEventModel(existing);
        existingModel.patch(model);
        MaintenanceEventVO vo = existingModel.toVO();
        service.update(existing, vo, user);

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
        MaintenanceEventVO vo = service.update(xid, model.toVO(), user);
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

        MaintenanceEventVO vo = service.insert(model.toVO(), user);
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
        return new MaintenanceEventModel(service.delete(xid, user));
    }
    
    @ApiOperation(value = "Toggle the state of a maintenance event", notes="returns boolean state of event")
    @RequestMapping(method = RequestMethod.PUT, value = "/toggle/{xid}")
    public ResponseEntity<Boolean> toggle(
            @PathVariable String xid,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        boolean activated = service.toggle(xid, user);
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

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return service.doQuery(rql, user, transformVisit);
    }
    
    @ApiOperation(
            value = "Find Maintenance Events linked to data points by point IDs",
            notes = "Returns a map of point ids to a list of events",
            response=Map.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/query/get-for-points-by-ids/{pointIds}")
    public Map<Integer, List<MaintenanceEventModel>> getForPointsByIds(
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
                    MaintenanceEventModel model = new MaintenanceEventModel(vo);
                    fillDataPoints(model);
                    fillDataSources(model);
                    models.add(model);
                }
                
            });
        }
        return map;
    }
    
    @ApiOperation(
            value = "Find Maintenance Events linked to data points by point XIDs",
            notes = "Returns a map of point ids to a list of events",
            response=Map.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/query/get-for-points-by-xids/{pointXids}")
    public Map<String, List<MaintenanceEventModel>> getForPointsByXid(
            @PathVariable(required = true) List<String> pointXids,
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        Map<String, List<MaintenanceEventModel>> map = new HashMap<>();
        for(String xid: pointXids) {
            List<MaintenanceEventModel> models = new ArrayList<>();
            map.put(xid, models);
            Integer id = DataPointDao.instance.getIdByXid(xid);
            if(id != null) {
                MaintenanceEventDao.instance.getForDataPoint(id, new MappedRowCallback<MaintenanceEventVO>() {
    
                    @Override
                    public void row(MaintenanceEventVO vo, int index) {
                        MaintenanceEventModel model = new MaintenanceEventModel(vo);
                        fillDataPoints(model);
                        fillDataSources(model);
                        models.add(model);
                    }
                    
                });
            }
        }
        return map;
    }
    
    //Helpers for Queries
    
    final Function<MaintenanceEventVO, Object> transformVisit = item -> {
        MaintenanceEventModel model = new MaintenanceEventModel(item);
        fillDataPoints(model);
        fillDataSources(model);
        return model;
    };
    
    /**
     * Set the data point XIDs if there are any, id must be set in model
     * @param model
     */
    private static void fillDataPoints(MaintenanceEventModel model) {
        List<String> xids = new ArrayList<String>();
        MaintenanceEventDao.instance.getPointXids(model.getId(), new MappedRowCallback<String>() {
            @Override
            public void row(String item, int index) {
                xids.add(item);
            }
        });
        model.setDataPoints(xids.size() == 0 ? null : xids);
    }
    
    /**
     * Set the data source XIDs if there are any, id must be set in model
     * @param model
     */
    private static void fillDataSources(MaintenanceEventModel model) {
        List<String> dsXids = new ArrayList<String>();
        MaintenanceEventDao.instance.getSourceXids(model.getId(), new MappedRowCallback<String>() {
            @Override
            public void row(String item, int index) {
                dsXids.add(item);
            }
        });
        model.setDataSources(dsXids.size() == 0 ? null : dsXids);
    }
}
