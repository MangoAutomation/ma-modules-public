/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.pointLinks;

import java.net.URI;
import java.util.function.Function;

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

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.spring.service.PointLinkService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Point Links Rest Controller")
@RestController()
@RequestMapping("/point-links")
public class PointLinkRestController {

    @Autowired
    private PointLinkService service;
    
    @ApiOperation(
            value = "Query Point Links",
            notes = "Admin Only",
            responseContainer="List",
            response=PointLinkModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return service.doQuery(rql, user, transform);
    }
    
    @ApiOperation(
            value = "Get a Point Link",
            notes = "Admin Only",
            response=PointLinkModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")

    public ResponseEntity<PointLinkModel> get(
            @ApiParam(value = "XID of Global Script to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(new PointLinkModel(service.get(xid, user)));
    }
    
    @ApiOperation(
            value = "Create a Point Link",
            notes = "Admin Only",
            response=PointLinkModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<PointLinkModel> create(
            @RequestBody PointLinkModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        PointLinkVO vo = service.insert(model.toVO(), user);
        URI location = builder.path("/point-links/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new PointLinkModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Update a Global Script",
            notes = "Admin Only",
            response=PointLinkModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<PointLinkModel> update(
            @ApiParam(value = "XID of Point Link to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Point link of update", required = true, allowMultiple = false)
            @RequestBody PointLinkModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        PointLinkVO vo = service.update(xid, model.toVO(), user);
        URI location = builder.path("/point-links/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new PointLinkModel(vo), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Delete a Global Script",
            notes = "Admin Only",
            response=PointLinkModel.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<PointLinkModel> delete(
            @ApiParam(value = "XID of Global Script to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(new PointLinkModel(service.delete(xid, user)));
    }
    
    final Function<PointLinkVO, Object> transform = item -> {
        return new PointLinkModel(item);
    };
    
}
