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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * 
 * @author Terry Packer
 *
 */
@Api(value="Data source controller")
@RestController
@RequestMapping("/data-sources")
public class DataSourcesRestController {
    
    private final DataSourceService service;
    private final BiFunction<DataSourceVO<?>, User, AbstractDataSourceModel<?>> map;
    
    @Autowired
    public DataSourcesRestController(final DataSourceService service, final RestModelMapper modelMapper) {
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, AbstractDataSourceModel.class, user);
        };
    }
    
    @ApiOperation(
            value = "Query Data Sources",
            notes = "RQL Formatted Query",
            responseContainer="List"
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

    @ApiOperation(value = "Save data source")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractDataSourceModel<?>> save(
            @RequestBody(required=true) AbstractDataSourceModel<?> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {
        
        DataSourceVO<?> vo = this.service.insertFull(model.toVO(), user);
        URI location = builder.path("/data-sources/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Update data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> update(
            @PathVariable String xid,
            @RequestBody(required=true) AbstractDataSourceModel<?> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {
        
        DataSourceVO<?> vo = this.service.update(xid, model.toVO(), user);
        URI location = builder.path("/data-sources/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Partially update a data source",
            notes = "Requires edit permission"
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated data source", required = true)
            @PatchVORequestBody(
                    service=DataSourceService.class,
                    modelClass=AbstractDataSourceModel.class)
            AbstractDataSourceModel<?> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataSourceVO<?> vo = service.updateFull(xid, model.toVO(), user);
        
        URI location = builder.path("/data-sources/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }
    
    @ApiOperation(
            value = "Delete a data source",
            notes = "",
            response=AbstractDataSourceModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public AbstractDataSourceModel<?> delete(
            @ApiParam(value = "XID of data source to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.delete(xid, user), user);
    }
    
    /**
     * Perform a query
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminPermission()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, vo -> map.apply(vo, user), true);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, vo -> map.apply(vo, user), true);
        }
    }
}
