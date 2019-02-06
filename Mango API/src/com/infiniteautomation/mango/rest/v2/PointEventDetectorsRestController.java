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
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.v2.model.event.detectors.AbstractPointEventDetectorModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.spring.service.PointEventDetectorsService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Data Point Event Detectors, all edits will force a data point to restart")
@RestController()
@RequestMapping("/point-event-detectors")
public class PointEventDetectorsRestController {

    private final PointEventDetectorsService service;
    private final BiFunction<AbstractPointEventDetectorVO<?>, User, AbstractPointEventDetectorModel<?>> map;

    @Autowired
    public PointEventDetectorsRestController(PointEventDetectorsService service, RestModelMapper modelMapper){
        this.service = service;
        this.map = (vo, user) -> {
            AbstractPointEventDetectorModel<?> model = modelMapper.map(vo, AbstractPointEventDetectorModel.class, user);
            //TODO Set the handlers?
            model.setDataPoint(modelMapper.map(vo.getDataPoint(), DataPointModel.class, user));
            return model;
        };
    }
    
    @ApiOperation(
            value = "Query Point Event Detectors",
            notes = "Use RQL formatted query, filtered by data point permissions",
            response=AbstractPointEventDetectorModel.class,
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
            value = "Get an Point Event Detector",
            notes = "",
            response=AbstractPointEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractPointEventDetectorModel<?> get(
            @ApiParam(value = "XID of Event detector", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.getFull(xid, user), user);
    }
    
    @ApiOperation(
            value = "Create a Point Event Detector",
            notes = "Requires global data source permission",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractPointEventDetectorModel<?>> create(
            @RequestBody AbstractPointEventDetectorModel<?> model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventDetectorVO<?> vo = service.insertFull(model.toVO(), user);
        URI location = builder.path("/point-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply((AbstractPointEventDetectorVO<?>)vo, user), headers, HttpStatus.CREATED);
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
