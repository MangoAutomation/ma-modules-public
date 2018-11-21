/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Phillip Dunlap
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.db.query.pojo.RQLToPagedObjectListQuery;
import com.infiniteautomation.mango.rest.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.TypedResultWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
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
import net.jazdw.rql.parser.ASTNode;

/**
 * Get possible events to handle.
 * @author Phillip Dunlap
 */
@Api(value="Event Types")
@RestController()
@RequestMapping("/v2/event-types")
public class EventTypeV2RestController {

    private final RestModelMapper modelMapper;
    
    @Autowired
    public EventTypeV2RestController(RestModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public static class EventTypeNameModel {
        private final String typeName;
        private final String descriptionKey;
        private final TranslatableMessage description;

        public EventTypeNameModel(String typeName, TranslatableMessage description) {
            this.typeName = typeName;
            this.description = description;
            this.descriptionKey = description.getKey();
        }

        public EventTypeNameModel(EventTypeDefinition def) {
            this.typeName = def.getTypeName();
            this.description = new TranslatableMessage(def.getDescriptionKey());
            this.descriptionKey = def.getDescriptionKey();
        }

        public String getTypeName() {
            return typeName;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        public TranslatableMessage getDescription() {
            return description;
        }
    }

    @ApiOperation(
            value = "Query current possible event types",
            notes = "Valid RQL ",
            response=AbstractEventTypeModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET)
    public TypedResultWithTotal<AbstractEventTypeModel> queryEventTypes(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        RQLToPagedObjectListQuery<AbstractEventTypeModel> filter = new RQLToPagedObjectListQuery<AbstractEventTypeModel>();
        
        //First prune the list based on permissions
        List<EventType> types = getAllEventTypesForUser(user);
        
        //TODO do this above when EventType/EventTypeVO is sorted out
        List<AbstractEventTypeModel> models = new ArrayList<>();
        for(EventType type : types)
            models.add(modelMapper.map(type, AbstractEventTypeModel.class));

        List<AbstractEventTypeModel> results = query.accept(filter, models);
        return new TypedResultWithTotal<AbstractEventTypeModel>() {

            @Override
            public List<AbstractEventTypeModel> getItems() {
                return results;
            }

            @Override
            public int getTotal() {
                return filter.getUnlimitedSize();
            }
            
        };
    }
    
    //TODO Remove when done testing
    @ApiOperation(
            value = "Echo back an event type",
            notes = "",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventTypeModel> create(
            @RequestBody AbstractEventTypeModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        URI location = builder.path("/v2/event-types/{xid}").buildAndExpand("XID").toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(model, headers, HttpStatus.CREATED);
    }
    
    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current possible event types",
            notes = "",
            response=EventTypeModel.class,
            responseContainer="Set")
    @RequestMapping(method = RequestMethod.GET, value="/type-names")
    public Set<EventTypeNameModel> getEventTypeNames(@AuthenticationPrincipal User user) {

        Set<EventTypeNameModel> typeNames = new HashSet<>();
        typeNames.add(new EventTypeNameModel(EventTypeNames.DATA_POINT, new TranslatableMessage("eventHandlers.pointEventDetector")));
        typeNames.add(new EventTypeNameModel(EventTypeNames.DATA_SOURCE, new TranslatableMessage("eventHandlers.dataSourceEvents")));
        typeNames.add(new EventTypeNameModel(EventTypeNames.PUBLISHER, new TranslatableMessage("eventHandlers.publisherEvents")));
        typeNames.add(new EventTypeNameModel(EventTypeNames.SYSTEM, new TranslatableMessage("eventHandlers.systemEvents")));
        typeNames.add(new EventTypeNameModel(EventTypeNames.AUDIT, new TranslatableMessage("eventHandlers.auditEvents")));

        for (EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
            typeNames.add(new EventTypeNameModel(def));
        }

        return typeNames;
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

    private List<EventType> getAllEventTypesForUser(User user) {
        List<EventType> types = new ArrayList<>();
        
        //Data Points
        for(DataPointVO vo : DataPointDao.getInstance().getAllFull()) {
            if(vo.getEventDetectors() != null) {
                for(AbstractPointEventDetectorVO<?> ed : vo.getEventDetectors()) {
                    EventType type = ed.getEventType().createEventType();
                    if(Permissions.hasEventTypePermission(user, type))
                        types.add(type);
                }
            }
        }
        
        //Data Sources
        for(DataSourceVO<?> vo : DataSourceDao.getInstance().getAll()) {
            for(EventTypeVO typeVo : vo.getEventTypes()) {
                EventType type = typeVo.createEventType();
                if(Permissions.hasEventTypePermission(user, type))
                    types.add(type);
            }
        }
        //Publishers
        for(PublisherVO<?> vo : PublisherDao.getInstance().getAll()) {
            for(EventTypeVO typeVo : vo.getEventTypes()) {
                EventType type = typeVo.createEventType();
                if(Permissions.hasEventTypePermission(user, type))
                    types.add(type);
            }
        }
        //System
        for(EventTypeVO typeVo : SystemEventType.EVENT_TYPES) {
            EventType type = typeVo.createEventType();
            if(Permissions.hasEventTypePermission(user, type))
                types.add(type);
        }
        
        // Audit
        for(EventTypeVO typeVo : AuditEventType.EVENT_TYPES) {
            EventType type = typeVo.createEventType();
            if(Permissions.hasEventTypePermission(user, type))
                types.add(type);
        }
        //Module defined
        for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
            if(def.getHandlersRequireAdmin() && user.hasAdminPermission()) {
                for(EventTypeVO typeVo : def.getEventTypeVOs())
                    types.add(typeVo.createEventType());
            }else {
                for(EventTypeVO typeVo : def.getEventTypeVOs()) {
                    EventType type = typeVo.createEventType();
                    if(Permissions.hasEventTypePermission(user, type))
                        types.add(type);
                }
            }
                
        }
        return types;
    }
    
    
    private void getAllDataPointEventTypes(List<EventTypeModel> types, User user, Integer dataPointId, Integer detectorId) {
        List<DataPointVO> dataPoints = DataPointDao.getInstance().getDataPoints(DataPointExtendedNameComparator.instance, true);
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
        List<DataSourceVO<?>> dataSources = DataSourceDao.getInstance().getAll();
        final boolean admin = Permissions.hasAdminPermission(user);
        for(DataSourceVO<?> dsvo : dataSources)
            if(dataSourceId == null || dataSourceId.intValue() == dsvo.getId())
                for(EventTypeVO dset : dsvo.getEventTypes())
                    if(dataSourceEventId == null || dataSourceEventId.intValue() == dset.getTypeRef2()) {
                        EventType et = dset.createEventType();
                        if(admin || Permissions.hasEventTypePermission(user, et))
                            types.add(et.asModel());
                    }
    }

    private void getAllPublisherEventTypes(List<EventTypeModel> types, User user, Integer publisherId, Integer publisherEventId) {
        List<PublisherVO<?>> publishers = PublisherDao.getInstance().getAll();
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
