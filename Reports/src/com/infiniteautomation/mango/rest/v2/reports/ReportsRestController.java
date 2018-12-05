/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import java.net.URI;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.reports.ReportsService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 * @author Terry Packer
 */
@Api(value="Reports")
@RestController
@RequestMapping("/reports")
public class ReportsRestController {

    private final ReportDao dao;
    private final ReportsService service;

    @Autowired
    private ReportsRestController(ReportsService service, ReportDao dao) {
        this.service = service;
        this.dao = dao;
    }

    @ApiOperation("Get by XID")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ReportModel getByXid(
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return new ReportModel(service.get(xid, user));
    }

    @ApiOperation("Get by ID")
    @RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
    public ReportModel getById(
            @PathVariable int id,
            @AuthenticationPrincipal User user) {
        return new ReportModel(service.get(id, user));
    }

    @ApiOperation(
            value = "Query items using RQL post",
            notes = "",
            response=ReportModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.POST, value = "/query")
    public StreamedArrayWithTotal query(
            @RequestBody ASTNode rqlQuery,
            @AuthenticationPrincipal User user) {
        return new StreamedVOQueryWithTotal<>(this.dao, rqlQuery, item -> service.hasReadPermission(user, item), item -> new ReportModel(item));
    }

    @ApiOperation(
            value = "Query items using RQL query parameters",
            notes = "",
            response=ReportModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {
        ASTNode rqlQuery = RQLUtils.parseRQLtoAST(request.getQueryString());
        return new StreamedVOQueryWithTotal<>(this.dao, rqlQuery, item -> service.hasReadPermission(user, item), item -> new ReportModel(item));
    }

    @ApiOperation(value = "Update an existing item")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<ReportModel> updateItem(
            @PathVariable String xid,

            @ApiParam(value = "Updated item model", required = true)
            @RequestBody ReportModel model,

            @AuthenticationPrincipal User user,

            UriComponentsBuilder builder) {

        ReportVO item = service.update(xid, model.toVO(), user);

        URI location = builder.path("/v2/reports/{xid}").buildAndExpand(item.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new ReportModel(item), headers, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Patch an existing item")
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<ReportModel> patchItem(
            @PathVariable String xid,

            @ApiParam(value = "Updated item model", required = true)
            @PatchVORequestBody(
                    service=ReportsService.class,
                    modelClass=ReportModel.class)
            ReportModel model,

            @AuthenticationPrincipal User user,

            UriComponentsBuilder builder) {

        ReportVO item = service.update(xid, model.toVO(), user);

        URI location = builder.path("/v2/reports/{xid}").buildAndExpand(item.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new ReportModel(item), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Create a new item")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReportModel> createItem(
            @ApiParam(value = "New item model", required = true)
            @RequestBody ReportModel model,

            @AuthenticationPrincipal User user,

            UriComponentsBuilder builder) {
        ReportVO item = service.insert(model.toVO(), user);
        
        URI location = builder.path("/v2/reports/{xid}").buildAndExpand(item.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(new ReportModel(item), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete an item")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{xid}")
    public ReportModel deleteItem(
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return new ReportModel(service.delete(xid, user));
    }
}
