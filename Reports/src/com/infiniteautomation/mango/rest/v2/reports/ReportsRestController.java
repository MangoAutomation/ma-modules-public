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

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVOQueryWithTotal;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportModel;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
@Api(value="Reports")
@RestController
@RequestMapping("/v2/reports")
public class ReportsRestController {

    private final ReportDao dao;

    @Autowired
    private ReportsRestController(ReportDao dao) {
        this.dao = dao;
    }

    @ApiOperation("Get by XID")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public ReportModel getByXid(
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {

        ReportVO item = this.dao.getByXid(xid);
        if (item == null) {
            throw new NotFoundRestException();
        }

        // TODO ensure permission
        user.ensureHasAdminPermission();

        return new ReportModel(item);
    }

    @ApiOperation("Get by ID")
    @RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
    public ReportModel getById(
            @PathVariable int id,
            @AuthenticationPrincipal User user) {

        ReportVO item = this.dao.get(id);
        if (item == null) {
            throw new NotFoundRestException();
        }

        // TODO ensure permission
        user.ensureHasAdminPermission();

        return new ReportModel(item);
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

        // TODO ensure permission
        user.ensureHasAdminPermission();

        // TODO check permission
        return new StreamedVOQueryWithTotal<>(this.dao, rqlQuery, item -> user.hasAdminPermission(), item -> new ReportModel(item));
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

        // TODO ensure permission
        user.ensureHasAdminPermission();

        ASTNode rqlQuery = RQLUtils.parseRQLtoAST(request.getQueryString());

        // TODO check permission
        return new StreamedVOQueryWithTotal<>(this.dao, rqlQuery, item -> user.hasAdminPermission(), item -> new ReportModel(item));
    }

    @ApiOperation(value = "Update an existing item")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<ReportModel> updateItem(
            @PathVariable String xid,

            @ApiParam(value = "Updated item model", required = true)
            @RequestBody ReportModel model,

            @AuthenticationPrincipal User user,

            UriComponentsBuilder builder) {

        ReportVO existing = this.dao.getByXid(xid);
        if (existing == null) {
            throw new NotFoundRestException();
        }

        // TODO ensure permission
        user.ensureHasAdminPermission();

        ReportVO item = model.getData();
        item.setId(existing.getId());
        item.ensureValid();

        this.dao.save(item);

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

        // TODO ensure permission
        user.ensureHasAdminPermission();

        ReportVO item = model.getData();
        item.ensureValid();

        this.dao.save(item);

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

        ReportVO item = this.dao.getByXid(xid);
        if (item == null) {
            throw new NotFoundRestException();
        }

        // TODO ensure permission
        user.ensureHasAdminPermission();

        this.dao.delete(item.getId());

        return new ReportModel(item);
    }
}
