/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Event Handlers Rest Controller")
@RestController("EventHandlersRestControllerV2")
@RequestMapping("/v2/event-handlers")
public class EventHandlersRestController {

    private final EventHandlerService service;
    private final RestModelMapper modelMapper;
    
    @Autowired
    public EventHandlersRestController(EventHandlerService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @ApiOperation(
            value = "Query Event Handlers",
            notes = "",
            responseContainer="List",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Get an Event Handler",
            notes = "",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<AbstractEventHandlerModel> get(
            @ApiParam(value = "XID of Mailing List to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(modelMapper.map(service.getFull(xid, user), AbstractEventHandlerModel.class, user));
    }

    @ApiOperation(
            value = "Create an Event Handler",
            notes = "Requires global Event Handler privileges",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventHandlerModel> create(
            @RequestBody AbstractEventHandlerModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventHandlerVO<?> vo = service.insertFull(model.toVO(), user);
        URI location = builder.path("/v2/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(modelMapper.map(vo, AbstractEventHandlerModel.class, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update an Event Handler",
            notes = "Requires edit permission",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<AbstractEventHandlerModel> update(
            @ApiParam(value = "XID of Event Handler to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Event Handler of update", required = true, allowMultiple = false)
            @RequestBody AbstractEventHandlerModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventHandlerVO<?> vo = service.updateFull(xid, model.toVO(), user);
        URI location = builder.path("/v2/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(modelMapper.map(vo, AbstractEventHandlerModel.class, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update an Event Handler",
            notes = "Requires edit permission",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractEventHandlerModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @RequestBody(required=true) AbstractEventHandlerModel model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        AbstractEventHandlerVO<?> existing = service.getFull(xid, user);
        AbstractEventHandlerModel existingModel = modelMapper.map(existing, AbstractEventHandlerModel.class, user);
        existingModel.patch(model);
        AbstractEventHandlerVO<?> vo = existingModel.toVO();
        vo = service.updateFull(existing, vo, user);

        URI location = builder.path("/v2/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(modelMapper.map(vo, AbstractEventHandlerModel.class, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete an EventHandler",
            notes = "",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<AbstractEventHandlerModel> delete(
            @ApiParam(value = "XID of EventHandler to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(modelMapper.map(service.delete(xid, user), AbstractEventHandlerModel.class, user));
    }

    @ApiOperation(
            value = "Validate an Event Handler without saving it",
            notes = "Admin Only",
            response=Void.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST, value="/validate")
    public void validate(
            @RequestBody AbstractEventHandlerModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        service.ensureValid(model.toVO(), user);
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, vo -> modelMapper.map(vo, AbstractEventHandlerModel.class, user), true);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, vo -> modelMapper.map(vo, AbstractEventHandlerModel.class, user), true);
        }
    }
}
