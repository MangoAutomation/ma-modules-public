/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.maintenanceEvents;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
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

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.BaseMangoRestController;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value="Maintenance Events API")
@RestController()
@RequestMapping("/v2/maintenance-events")
public class MaintenanceEventRestController extends BaseMangoRestController {

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

        MaintenanceEventDao.instance.saveFull(vo);

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

        MaintenanceEventDao.instance.saveFull(vo);

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MaintenanceEventModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Create new maintenance event")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MaintenanceEventModel> create(
            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) MaintenanceEventModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        MaintenanceEventVO vo = model.toVO();
        ensureEditPermission(user, vo);
        if(StringUtils.isEmpty(vo.getXid()))
            vo.setXid(MaintenanceEventDao.instance.generateUniqueXid());
        
        vo.ensureValid();

        MaintenanceEventDao.instance.saveFull(vo);

        URI location = builder.path("/v2/maintenance-events/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new MaintenanceEventModel(vo), headers, HttpStatus.OK);
    }
    
    /**
     * @param user
     * @param vo
     */
    private void ensureEditPermission(User user, MaintenanceEventVO vo) {
        // TODO Auto-generated method stub
        
    }
}
