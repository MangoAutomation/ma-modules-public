/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Phillip Dunlap
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.spring.dao.DataPointDao;
import com.infiniteautomation.mango.spring.dao.DataSourceDao;
import com.infiniteautomation.mango.spring.dao.PublisherDao;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.EventType.EventTypeNames;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Get possible events to handle.
 * @author Phillip Dunlap
 */
@Api(value="Event Types", description="Get event types")
@RestController()
@RequestMapping("/v2/event-types")
public class EventTypeV2RestController {

    public EventTypeV2RestController() {}
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current possible event types",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="")
    public ResponseEntity<List<EventTypeModel>> getEventTypes(@AuthenticationPrincipal User user,
            @RequestParam(value = "typeName", required = false) String typeName,
            @RequestParam(value = "subtypeName", required = false) String subtypeName,
            @RequestParam(value = "typeRef1", required = false) Integer typeRef1,
            @RequestParam(value = "typeRef2", required = false) Integer typeRef2) {
        
        List<EventTypeModel> result = new ArrayList<EventTypeModel>();
        
        if(typeName == null) {
            getAllDataPointEventTypes(result, user, typeRef1, typeRef2);
            getAllDataSourceEventTypes(result, user, typeRef1, typeRef2);
            getAllPublisherEventTypes(result, user, typeRef1, typeRef2);
            getAllSystemEventTypes(result, user, subtypeName);
            getAllAuditEventTypes(result, user, subtypeName, typeRef1);
            getAllModuleEventTypes(result, user, null, subtypeName, typeRef1, typeRef2, false);
        } else if(typeName.equals(EventTypeNames.DATA_POINT)) {
            getAllDataPointEventTypes(result, user, typeRef1, typeRef2);
        } else if(typeName.equals(EventTypeNames.DATA_SOURCE)) {
            getAllDataSourceEventTypes(result, user, typeRef1, typeRef2);
        } else if(typeName.equals(EventTypeNames.PUBLISHER)) {
            getAllPublisherEventTypes(result, user, typeRef1, typeRef2);
        } else if(typeName.equals(EventTypeNames.SYSTEM)) {
            getAllSystemEventTypes(result, user, subtypeName);
        } else if(typeName.equals(EventTypeNames.AUDIT)) {
            getAllAuditEventTypes(result, user, subtypeName, typeRef1);
        } else {
            getAllModuleEventTypes(result, user, typeName, subtypeName, typeRef1, typeRef2, false);
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current data point event types",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-point") 
    public ResponseEntity<List<EventTypeModel>> getDataPointEventTypes(@AuthenticationPrincipal User user,
            @ApiParam(value = "ID of the data point", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef1", required = false) Integer typeRef1,
            @ApiParam(value = "ID of the event type", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef2", required = false) Integer typeRef2) {
        List<EventTypeModel> result = new ArrayList<EventTypeModel>();
        getAllDataPointEventTypes(result, user, typeRef1, typeRef2);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current data point event types for a point",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-point/{dpid}") 
    public ResponseEntity<List<EventTypeModel>> getDataPointEventTypesByPoint(@AuthenticationPrincipal User user,
            @ApiParam(value = "ID of the data point", required = false, allowMultiple = false) 
            @PathVariable Integer dpid,
            @ApiParam(value = "ID of the event type", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef2", required = false) Integer typeRef2) {
        List<EventTypeModel> result = new ArrayList<EventTypeModel>();
        getAllDataPointEventTypes(result, user, dpid, typeRef2);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current data source event types",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-source") 
    public ResponseEntity<List<EventTypeModel>> getDataSourceEventTypes(@AuthenticationPrincipal User user,
            @ApiParam(value = "ID of the data source", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef1", required = false) Integer typeRef1,
            
            @ApiParam(value = "ID of the event type", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef2", required = false) Integer typeRef2) {
        List<EventTypeModel> result = new ArrayList<EventTypeModel>();
        getAllDataSourceEventTypes(result, user, typeRef1, typeRef2);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current data source event types for a source",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET, value="/data-source/{dsid}") 
    public ResponseEntity<List<EventTypeModel>> getDataSourceEventTypesBySource(@AuthenticationPrincipal User user,
            @ApiParam(value = "ID of the data source", required = false, allowMultiple = false) @PathVariable Integer dsid,
            
            @ApiParam(value = "ID of the event type", required = false, allowMultiple = false) 
            @RequestParam(value = "typeRef2", required = false) Integer typeRef2) {
        List<EventTypeModel> result = new ArrayList<EventTypeModel>();
        getAllDataSourceEventTypes(result, user, dsid, typeRef2);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    private void getAllDataPointEventTypes(List<EventTypeModel> types, User user, Integer dataPointId, Integer detectorId) {
        List<DataPointVO> dataPoints = DataPointDao.instance.getDataPoints(DataPointExtendedNameComparator.instance, true);
        final boolean admin = Permissions.hasAdminPermission(user);
        for(DataPointVO dpvo : dataPoints)
            if((dataPointId == null || dataPointId.intValue() == dpvo.getId()) && dpvo.getEventDetectors() != null)
                for(AbstractPointEventDetectorVO<?> ed : dpvo.getEventDetectors())
                    if((detectorId == null || detectorId.intValue() == ed.getId())) {
                        EventType dpet = ed.getEventType().createEventType();
                        if(admin || Permissions.hasEventTypePermission(user, dpet))
                            types.add(dpet.asModel());
                    }
                        
    }
    
    private void getAllDataSourceEventTypes(List<EventTypeModel> types, User user, Integer dataSourceId, Integer dataSourceEventId) {
        List<DataSourceVO<?>> dataSources = DataSourceDao.instance.getAll();
        final boolean admin = Permissions.hasAdminPermission(user);
        for(DataSourceVO<?> dsvo : dataSources)
            if(dataSourceId == null || dataSourceId.intValue() == dsvo.getId())
                for(EventTypeVO dset : (List<EventTypeVO>)dsvo.getEventTypes())
                    if(dataSourceEventId == null || dataSourceEventId.intValue() == dset.getTypeRef2()) {
                        EventType et = dset.createEventType();
                        if(admin || Permissions.hasEventTypePermission(user, et))
                            types.add(et.asModel());
                    }
    }
    
    private void getAllPublisherEventTypes(List<EventTypeModel> types, User user, Integer publisherId, Integer publisherEventId) {
        List<PublisherVO<?>> publishers = PublisherDao.instance.getAll();
        final boolean admin = Permissions.hasAdminPermission(user);
        for(PublisherVO<?> pvo : publishers)
            if(publisherId == null || publisherId.intValue() == pvo.getId())
                for(EventTypeVO pet : pvo.getEventTypes())
                    if(publisherEventId == null || publisherEventId.intValue() == pet.getTypeRef2()) {
                        EventType et = pet.createEventType();
                        if(admin || Permissions.hasEventTypePermission(user, et))
                            types.add(et.asModel());
                    }
    }
    
    private void getAllSystemEventTypes(List<EventTypeModel> types, User user, String subtypeName) {
        final boolean admin = Permissions.hasAdminPermission(user);
        for(EventTypeVO sets : SystemEventType.EVENT_TYPES)
            if(subtypeName == null || subtypeName.equals(sets.getSubtype())) {
                EventType set = sets.createEventType();
                if(admin || Permissions.hasEventTypePermission(user, set))
                    types.add(set.asModel());
                
            }
    }
    
    public void getAllAuditEventTypes(List<EventTypeModel> types, User user, String subtypeName, Integer typeRef1) {
        final boolean admin = Permissions.hasAdminPermission(user);
        for(EventTypeVO aets : AuditEventType.EVENT_TYPES)
            if(typeRef1 == null || aets.getTypeRef1() == typeRef1.intValue())
                if(subtypeName == null || subtypeName.equals(aets.getSubtype())) {
                    EventType aet = aets.createEventType();
                    if(admin || Permissions.hasEventTypePermission(user, aet))
                        types.add(aet.asModel());
                }
    }
    
    public void getAllModuleEventTypes(List<EventTypeModel> types, User user, String typeName, String subtypeName, 
            Integer typeRef1, Integer typeRef2, boolean adminOnly) {
        for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class))
            if(!adminOnly || def.getHandlersRequireAdmin())
                for(EventTypeVO etvo : def.getEventTypeVOs())
                    if(typeName == null || typeName.equals(etvo.getType()))
                        if(subtypeName == null || subtypeName.equals(etvo.getSubtype()))
                            if(typeRef1 == null || typeRef1.intValue() == etvo.getTypeRef1())
                                if(typeRef2 == null || typeRef2.intValue() == etvo.getTypeRef2())
                                    types.add(etvo.createEventType().asModel());
    }
}
