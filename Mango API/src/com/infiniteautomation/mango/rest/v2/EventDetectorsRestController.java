/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.detectors.AbstractEventDetectorModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.EventDetectorsService;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Event Detectors, full REST v2 implementation")
@RestController()
@RequestMapping("/full-event-detectors")
public class EventDetectorsRestController<T extends AbstractEventDetectorVO<T>> {

    private final EventDetectorsService<T> service;
    private final BiFunction<AbstractEventDetectorVO<T>, User, AbstractEventDetectorModel<?>> map;

    @Autowired
    public EventDetectorsRestController(EventDetectorsService<T> service, RestModelMapper modelMapper){
        this.service = service;
        this.map = (vo, user) -> {
            AbstractEventDetectorModel<?> model = modelMapper.map(vo, AbstractEventDetectorModel.class, user);
            return model;
        };
    }
    
    @ApiOperation(
            value = "Query Event Detectors",
            notes = "Use RQL formatted query, filtered by data point read permissions",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }
    
    @ApiOperation(
            value = "Get an Event Detector by xid",
            notes = "User must have read permission for the data point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractEventDetectorModel<?> getByXid(
            @ApiParam(value = "XID of Event detector", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.getFull(xid, user), user);
    }
    
    @ApiOperation(
            value = "Get an Event Detector by id",
            notes = "User must have read permission for the data point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractEventDetectorModel<?> getById(
            @ApiParam(value = "ID of Event detector", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.getFull(id, user), user);
    }
    
    @ApiOperation(
            value = "Create an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventDetectorModel<?>> create(
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @RequestBody AbstractEventDetectorModel<T> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        T vo = service.insertFull(model.toVO(), user, restart);
        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }
    
    @ApiOperation(
            value = "Update an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> update(
            @ApiParam(value = "XID of Event Handler to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Event Handler of update", required = true, allowMultiple = false)
            @RequestBody AbstractEventDetectorModel<T> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        T vo = service.updateFull(xid, model.toVO(), user, restart);
        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> partialUpdate(
            @PathVariable String xid,
            @ApiParam(value = "Event detector to patch", required = true)
            @PatchVORequestBody(
                    service=EventHandlerService.class,
                    modelClass=AbstractEventDetectorModel.class)
            AbstractEventDetectorModel<T> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        T vo = service.updateFull(xid, model.toVO(), user, restart);
        
        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Delete an Event Detector",
            notes = "User must have data source edit permission for source of the point, data point will restart",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> delete(
            @ApiParam(value = "XID to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(map.apply(service.delete(xid, user), user));
    }
    
    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, vo -> map.apply(vo, user), true);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, vo -> map.apply(vo, user), true);
        }
    }
}
