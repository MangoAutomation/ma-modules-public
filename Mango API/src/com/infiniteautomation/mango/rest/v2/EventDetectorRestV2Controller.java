/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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

import com.infiniteautomation.mango.rest.v2.exception.AlreadyExistsRestException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.FilteredPageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventDetector.EventDetectorStreamCallback;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.detectors.AbstractEventDetectorModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * Handle editing event detectors.
 *
 * There is currently an SQL constraint that enforces the rule
 * that you cannot delete a data point if it has an event detector,
 * so no code checks for a null sourceId reference yet.
 *
 * @author Terry Packer
 */
@Api(value="Event Detectors", description="All edits will force a data point to restart")
@RestController()
@RequestMapping("/event-detectors")
public class EventDetectorRestV2Controller extends AbstractMangoVoRestV2Controller<AbstractEventDetectorVO<?>, AbstractEventDetectorModel<?>, EventDetectorDao>{

    private final EventDetectorDao dao;

    @Autowired
    public EventDetectorRestV2Controller(EventDetectorDao dao) {
        super(dao);
        this.dao = dao;
    }

    @ApiOperation(
            value = "Query Event Detectors",
            notes = "Use RQL formatted query, filtered by data point permissions",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<QueryDataPageStream<AbstractEventDetectorVO<?>>> queryRQL(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        ASTNode node = RQLUtils.parseRQLtoAST(request.getQueryString());
        if(user.hasAdminPermission()){
            //admin users don't need to filter the results
            return new ResponseEntity<>(getPageStream(node), HttpStatus.OK);
        }else{
            EventDetectorStreamCallback callback = new EventDetectorStreamCallback(this, user);
            FilteredPageQueryStream<AbstractEventDetectorVO<?>, AbstractEventDetectorModel<?>, EventDetectorDao> stream  =
                    new FilteredPageQueryStream<AbstractEventDetectorVO<?>, AbstractEventDetectorModel<?>, EventDetectorDao>(EventDetectorDao.getInstance(),this, node, callback);
            stream.setupQuery();
            return new ResponseEntity<>(stream, HttpStatus.OK);
        }
    }

    @ApiOperation(
            value = "Get an Event Detector",
            notes = "",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> get(
            @AuthenticationPrincipal User user,
            @ApiParam(value = "Valid Event Detector XID", required = true, allowMultiple = false)
            @PathVariable String xid, HttpServletRequest request) {

        AbstractEventDetectorVO<?> vo = this.dao.getByXid(xid);
        if(vo == null)
            throw new NotFoundRestException();

        //Check permissions
        if(!user.hasAdminPermission()){
            DataPointVO dp = DataPointDao.getInstance().get(vo.getSourceId());
            Permissions.ensureDataPointReadPermission(user, dp);
        }

        return new ResponseEntity<>(vo.asModel(), HttpStatus.OK);
    }

    @ApiOperation(
            value = "Create an Event Detector",
            notes = "Cannot already exist, must have data source permission for the point"
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventDetectorModel<?>> save(
            @ApiParam(value = "Event Detector", required = true)
            @RequestBody(required=true)  AbstractEventDetectorModel<?> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {

        AbstractEventDetectorVO<?> vo = model.getData();

        // Set XID if required
        if (StringUtils.isEmpty(vo.getXid())) {
            vo.setXid(EventDetectorDao.getInstance().generateUniqueXid());
        }

        //Check to see if it already exists
        AbstractEventDetectorVO<?> existing = this.dao.getByXid(vo.getXid());
        if(existing != null){
            throw new AlreadyExistsRestException(vo.getXid());
        }

        //Check permission
        DataPointVO dp = DataPointDao.getInstance().get(vo.getSourceId());
        if(dp == null)
            throw new NotFoundRestException();
        Permissions.ensureDataSourcePermission(user, dp.getDataSourceId());

        //TODO Fix this when we have other types of detectors
        AbstractPointEventDetectorVO<?> ped = (AbstractPointEventDetectorVO<?>) vo;
        ped.njbSetDataPoint(dp);

        //Validate
        ped.ensureValid();

        //Add it to the data point
        DataPointDao.getInstance().setEventDetectors(dp);
        dp.getEventDetectors().add(ped);

        //Save the data point
        Common.runtimeManager.saveDataPoint(dp);

        // Put a link to the newly created resource in the location header
        URI location = builder.path("/event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(vo.asModel(), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update an Event Detector",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.PUT, value={"/{xid}"})
    public ResponseEntity<AbstractEventDetectorModel<?>> update(
            @ApiParam(value = "Valid Event Detector XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Event Detector", required = true)
            @RequestBody(required=true)  AbstractEventDetectorModel<?> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {

        AbstractEventDetectorVO<?> vo = model.getData();

        //Check to see if it already exists
        AbstractEventDetectorVO<?> existing = this.dao.getByXid(xid);
        if(existing == null){
            throw new NotFoundRestException();
        }else{
            //Set the ID
            vo.setId(existing.getId());
        }

        //Check permission
        DataPointVO dp = DataPointDao.getInstance().get(vo.getSourceId());
        if(dp == null)
            throw new NotFoundRestException();
        Permissions.ensureDataSourcePermission(user, dp.getDataSourceId());

        //TODO Fix this when we have other types of detectors
        AbstractPointEventDetectorVO<?> ped = (AbstractPointEventDetectorVO<?>) vo;
        ped.njbSetDataPoint(dp);

        //Validate
        ped.ensureValid();

        //Replace it on the data point, if it isn't replaced we fail.
        boolean replaced = false;
        DataPointDao.getInstance().setEventDetectors(dp);
        ListIterator<AbstractPointEventDetectorVO<?>> it = dp.getEventDetectors().listIterator();
        while(it.hasNext()){
            AbstractPointEventDetectorVO<?> ed = it.next();
            if(ed.getId() == ped.getId()){
                it.set(ped);
                replaced = true;
                break;
            }
        }

        if(!replaced)
            throw new ServerErrorException(new TranslatableMessage("rest.error.eventDetectorNotAssignedToThisPoint"));

        //Save the data point
        Common.runtimeManager.saveDataPoint(dp);

        // Put a link to the updated resource in the location header
        URI location = builder.path("/event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(vo.asModel(), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete an Event Detector",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.DELETE,
    value={"/{xid}"})
    public ResponseEntity<AbstractEventDetectorModel<?>> delete(
            @ApiParam(value = "Valid Event Detector XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {

        //Check to see if it already exists
        AbstractEventDetectorVO<?> existing = this.dao.getByXid(xid);
        if(existing == null){
            throw new NotFoundRestException();
        }

        //Check permission
        DataPointVO dp = DataPointDao.getInstance().get(existing.getSourceId());
        Permissions.ensureDataSourcePermission(user, dp.getDataSourceId());

        //TODO Fix this when we have other types of detectors
        AbstractPointEventDetectorVO<?> ped = (AbstractPointEventDetectorVO<?>) existing;
        ped.njbSetDataPoint(dp);

        //Remove it from the data point, if it isn't replaced we fail.
        boolean removed = false;
        DataPointDao.getInstance().setEventDetectors(dp);
        ListIterator<AbstractPointEventDetectorVO<?>> it = dp.getEventDetectors().listIterator();
        while(it.hasNext()){
            AbstractPointEventDetectorVO<?> ed = it.next();
            if(ed.getId() == ped.getId()){
                it.remove();
                removed = true;
                break;
            }
        }

        if(!removed)
            throw new ServerErrorException(new TranslatableMessage("rest.error.eventDetectorNotAssignedToThisPoint"));

        //Save the data point
        Common.runtimeManager.saveDataPoint(dp);

        return new ResponseEntity<>(existing.asModel(), HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get all Event Detectors for a given data point",
            notes = "",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-point/{xid}")
    public ResponseEntity<List<AbstractEventDetectorModel<?>>> getForDataPoint(
            @AuthenticationPrincipal User user,
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid, HttpServletRequest request) {

        DataPointVO dp = DataPointDao.getInstance().getByXid(xid);
        if(dp == null)
            throw new NotFoundRestException();

        Permissions.ensureDataPointReadPermission(user, dp);

        DataPointDao.getInstance().setEventDetectors(dp);
        List<AbstractEventDetectorModel<?>> models = new ArrayList<AbstractEventDetectorModel<?>>();
        for(AbstractPointEventDetectorVO<?> ped : dp.getEventDetectors())
            models.add(ped.asModel());
        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete all Event Detectors for a given data point",
            response=AbstractEventDetectorModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.DELETE, value="/data-point/{xid}")
    public ResponseEntity<Void> deleteForDataPoint(
            @AuthenticationPrincipal User user,
            @ApiParam(value = "Valid Data Point XID", required = true, allowMultiple = false)
            @PathVariable String xid) {

        DataPointVO dp = DataPointDao.getInstance().getByXid(xid);
        if (dp == null) {
            throw new NotFoundRestException();
        }

        Permissions.ensureDataSourcePermission(user, dp.getDataSourceId());

        dp.setEventDetectors(new ArrayList<>());
        Common.runtimeManager.saveDataPoint(dp);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get all Event Detectors for a given data source",
            notes = "Must have permission for all data points",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-source/{xid}")
    public ResponseEntity<List<AbstractEventDetectorModel<?>>> getForDataSource(
            @AuthenticationPrincipal User user,
            @ApiParam(value = "Valid Data Source XID", required = true, allowMultiple = false)
            @PathVariable String xid, HttpServletRequest request) {

        DataSourceVO<?> ds = DataSourceDao.getInstance().getByXid(xid);
        if(ds == null)
            throw new NotFoundRestException();

        List<DataPointVO> points = DataPointDao.getInstance().getDataPoints(ds.getId(), null, false);
        List<AbstractEventDetectorModel<?>> models = new ArrayList<AbstractEventDetectorModel<?>>();

        for(DataPointVO dp : points){
            //Check permissions
            if(Permissions.hasDataPointReadPermission(user, dp)) {
                DataPointDao.getInstance().setEventDetectors(dp);
                for(AbstractPointEventDetectorVO<?> ped : dp.getEventDetectors()) {
                    models.add(ped.asModel());
                }
            }
        }
        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @Override
    public AbstractEventDetectorModel<?> createModel(AbstractEventDetectorVO<?> vo) {
        return vo.asModel();
    }

}
