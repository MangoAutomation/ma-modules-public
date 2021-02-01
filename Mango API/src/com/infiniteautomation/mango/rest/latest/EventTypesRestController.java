/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.AuditEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.DataPointEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.DataSourceEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.EventTypeVOModel;
import com.infiniteautomation.mango.rest.latest.model.event.PublisherEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.SystemEventTypeModel;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.AbstractPointEventDetectorModel;
import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemEventTypeDefinition;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.EventType.EventTypeNames;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.publish.PublisherVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Event Types")
@RestController()
@RequestMapping("/event-types")
public class EventTypesRestController {

    private final DataPointService dataPointService;
    private final DataSourceDao dataSourceDao;
    private final PublisherDao publisherDao;
    private final EventDetectorDao eventDetectorDao;
    private final PermissionService permissionService;
    private final RestModelMapper modelMapper;

    @Autowired
    public EventTypesRestController(
            DataPointService dataPointService,
            DataSourceDao dataSourceDao,
            PublisherDao publisherDao,
            EventDetectorDao eventDetectorDao,
            PermissionService permissionService,
            RestModelMapper modelMapper) {
        this.dataPointService = dataPointService;
        this.dataSourceDao = dataSourceDao;
        this.publisherDao = publisherDao;
        this.eventDetectorDao = eventDetectorDao;
        this.permissionService = permissionService;
        this.modelMapper = modelMapper;
    }


    @ApiOperation(
            value = "Query all available event types",
            notes = "Not specific to any reference ids, results come back based on type/sub-type combinations",
            response=EventTypeVOModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET)
    public StreamWithTotal<EventTypeVOModel<?,?,?>> queryAllEventTypes(
            @AuthenticationPrincipal PermissionHolder user,
            Translations translations,
            ASTNode query) {

        List<EventTypeVOModel<?,?,?>> models = new ArrayList<>();

        //Data Source
        DataSourceEventType dset = new DataSourceEventType(0, 0);
        DataSourceEventTypeModel dsetm = new DataSourceEventTypeModel(dset);
        dsetm.setAlarmLevel(null);
        EventTypeVOModel<?,?,?> dataSource = new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>, String>(dsetm, new TranslatableMessage("eventHandlers.dataSourceEvents"), false, true, true);
        dataSource.setAlarmLevel(null);
        models.add(dataSource);

        //Data point
        DataPointEventType dpet = new DataPointEventType(0, 0);
        DataPointEventTypeModel dpetm = new DataPointEventTypeModel(dpet);
        EventTypeVOModel<?,?,?> dataPoint = new EventTypeVOModel<DataPointEventType, DataPointModel, AbstractPointEventDetectorModel<?>>(dpetm, new TranslatableMessage("eventHandlers.pointEventDetector"), false, true, true);
        dataPoint.setAlarmLevel(null);
        models.add(dataPoint);

        //Publisher
        PublisherEventType pet = new PublisherEventType(0, 0);
        PublisherEventTypeModel petm = new PublisherEventTypeModel(pet);
        EventTypeVOModel<?,?,?> publisher = new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>, String>(petm, new TranslatableMessage("eventHandlers.publisherEvents"), false, true, true);
        publisher.setAlarmLevel(null);
        models.add(publisher);

        //System
        //TODO Permissions
        //TODO since we some system event types support referenceId1 and 2 we can't be sure here
        SystemEventType set = new SystemEventType();
        SystemEventTypeModel setm = new SystemEventTypeModel(set);
        EventTypeVOModel<?,?,?> system = new EventTypeVOModel<>(setm, new TranslatableMessage("eventHandlers.systemEvents"), null, true, true, true);
        models.add(system);


        //Audit
        //TODO Permissions
        //TODO Audit Event Types will eventually support ref1 & 2
        AuditEventType aet = new AuditEventType();
        AuditEventTypeModel aetm = new AuditEventTypeModel(aet);
        EventTypeVOModel<?,?,?> audit = new EventTypeVOModel<>(aetm, new TranslatableMessage("eventHandlers.auditEvents"), null, true, false, false);
        models.add(audit);

        //Module defined
        for (EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
            if(!def.hasCreatePermission(user, permissionService))
                continue;

            EventType et = def.createDefaultEventType();
            AbstractEventTypeModel<?,?,?> model = modelMapper.map(et, AbstractEventTypeModel.class, user);
            models.add(new EventTypeVOModel<>(model, new TranslatableMessage(def.getDescriptionKey()), def.supportsSubType(), def.supportsReferenceId1(), def.supportsReferenceId2()));
        }

        return new FilteredStreamWithTotal<>(models, query, translations);
    }

    @ApiOperation(
            value = "Query event types from one eventType to get all subtypes for an event type",
            notes = "Subtypes are set",
            response=EventTypeVOModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET, value="/{type}")
    public StreamWithTotal<EventTypeVOModel<?,?,?>> queryEventTypesForType(
            @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
            @AuthenticationPrincipal PermissionHolder user,
            Translations translations,
            ASTNode query) {

        List<EventTypeVOModel<?,?,?>> models = getEventTypes(type, user);
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

    @ApiOperation(
            value = "Query event types from one eventType",
            notes = "ReferenceId 1 is set, only available if the type supports referenceId1",
            response=EventTypeVOModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET, value="/{type}/{subtype}")
    public StreamWithTotal<EventTypeVOModel<?,?,?>> queryEventTypesForTypeAndSubType(
            @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
            @PathVariable(value="subtype") @ApiParam(value = "Event subtype to query over", required = true)  String subtype,
            @AuthenticationPrincipal PermissionHolder user,
            Translations translations,
            ASTNode query) {

        List<EventTypeVOModel<?,?,?>> models = getEventTypesForSubtype(type,  StringUtils.equalsIgnoreCase(subtype, "null") ? null : subtype, user);
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

    @ApiOperation(
            value = "Query event types from one eventType",
            notes = "ReferenceId 1 and 2 are set accordinly",
            response=EventTypeVOModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET, value="/{type}/{subtype}/{referenceId1}")
    public StreamWithTotal<EventTypeVOModel<?,?,?>> queryEventTypesForTypeAndSubType(
            @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
            @PathVariable(value="subtype") @ApiParam(value = "Event subtype to query over", required = true) String subtype,
            @PathVariable(value="referenceId1") @ApiParam(value = "Reference ID 1 locator", required = true)  Integer referenceId1,
            @AuthenticationPrincipal PermissionHolder user,
            Translations translations,
            ASTNode query) {

        List<EventTypeVOModel<?,?,?>> models = getEventTypesForSubtypeAndReferenceId1(type, StringUtils.equalsIgnoreCase(subtype, "null") ? null : subtype, referenceId1, user);
        return new FilteredStreamWithTotal<>(models, query, translations);
    }

    /**
     *
     * @param typeName
     * @param user
     * @return
     * @throws NotFoundException
     */
    private List<EventTypeVOModel<?,?,?>> getEventTypes(String typeName, PermissionHolder user) throws NotFoundException {
        //track if the type was a default type
        List<EventTypeVOModel<?,?,?>> types = new ArrayList<>();
        boolean found = false;
        switch(typeName) {
            case EventTypeNames.DATA_POINT:
            case EventTypeNames.DATA_SOURCE:
            case EventTypeNames.PUBLISHER:
                throw new BadRequestException();
            case EventTypeNames.SYSTEM:
                found = true;
                for(SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class)) {
                    EventTypeVO type = SystemEventType.getEventType(def.getTypeName());
                    if(permissionService.hasEventTypePermission(user, type.getEventType())) {
                        SystemEventTypeModel model = modelMapper.map(type.getEventType(), SystemEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), true, def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                }
                break;
            case EventTypeNames.AUDIT:
                found = true;
                for(EventTypeVO vo : AuditEventType.getRegisteredEventTypes()) {
                    AuditEventType aet = (AuditEventType) vo.getEventType();
                    if(permissionService.hasEventTypePermission(user, aet)) {
                        AuditEventTypeModel aetm = new AuditEventTypeModel(aet);
                        EventTypeVOModel<?,?,?> audit = new EventTypeVOModel<>(aetm, vo.getDescription(), vo.getAlarmLevel(), true, false, false);
                        types.add(audit);
                    }
                }
                break;
        }

        if(!found) {
            //Module defined
            for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
                if(StringUtils.equals(typeName, def.getTypeName())) {
                    found = true;
                    for(EventTypeVO type : def.generatePossibleEventTypesWithSubtype(user, permissionService)) {
                        EventType eventType = type.getEventType();
                        AbstractEventTypeModel<?,?,?> model = modelMapper.map(eventType, AbstractEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), def.supportsSubType(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                    break;
                }
            }
        }
        if(!found)
            throw new NotFoundException();
        return types;
    }

    /**
     * Generate a list of all event types generalized by sub-type
     *
     * @param typeName
     * @param subtype
     * @param user
     * @return
     * @throws NotFoundException
     */
    private List<EventTypeVOModel<?,?,?>> getEventTypesForSubtype(String typeName, String subtype, PermissionHolder user) throws NotFoundException {
        //track if the type was a default type
        List<EventTypeVOModel<?,?,?>> types = new ArrayList<>();
        boolean found = false;
        switch(typeName) {
            case EventTypeNames.DATA_POINT:
                //There is no subtype for data points
                if(subtype != null)
                    throw new BadRequestException();

                //Get Event Detectors, ensure only 1 data point in list
                //TODO via query instead
                List<AbstractPointEventDetectorVO> detectors = this.eventDetectorDao.getAllPointEventDetectors();
                Map<Integer, DataPointVO> uniquePointsMap = new HashMap<>();
                for(AbstractPointEventDetectorVO detector : detectors) {
                    uniquePointsMap.put(detector.getDataPoint().getId(), detector.getDataPoint());
                }

                for(DataPointVO vo : uniquePointsMap.values()) {
                    //Shortcut to check permissions via event type
                    if(permissionService.hasPermission(user, vo.getReadPermission())) {
                        DataPointEventTypeModel model = new DataPointEventTypeModel(new DataPointEventType(vo.getDataSourceId(), vo.getId(), 0, null), modelMapper.map(vo, DataPointModel.class, user));
                        types.add(new EventTypeVOModel<DataPointEventType, DataPointModel,AbstractPointEventDetectorModel<?>>(model, new TranslatableMessage("event.eventsFor", vo.getName()), false, true, true));
                    }
                }
                found = true;
                break;
            case EventTypeNames.DATA_SOURCE:
                //There is no subtype for data sources
                if(subtype != null)
                    throw new BadRequestException();

                for(DataSourceVO vo : dataSourceDao.getAll()) {
                    if(permissionService.hasPermission(user, vo.getReadPermission())) {
                        AbstractDataSourceModel<?> dsModel = modelMapper.map(vo, AbstractDataSourceModel.class, user);
                        DataSourceEventTypeModel model = new DataSourceEventTypeModel(new DataSourceEventType(vo.getId(), 0), dsModel);
                        types.add(new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>, String>(model, new TranslatableMessage("event.eventsFor", vo.getName()), false, true, true));
                    }
                }
                found = true;
                break;
            case EventTypeNames.PUBLISHER:
                //There is no subtype for publishers
                if(subtype != null)
                    throw new BadRequestException();

                //There are no permissions for publishers
                if(!permissionService.hasAdminRole(user))
                    break;

                for(PublisherVO<?> vo : publisherDao.getAll()) {
                    AbstractPublisherModel<?,?> publisherModel = modelMapper.map(vo, AbstractPublisherModel.class, user);
                    PublisherEventTypeModel model = new PublisherEventTypeModel(new PublisherEventType(vo.getId(), 0), publisherModel);
                    types.add(new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>, String>(model, new TranslatableMessage("event.eventsFor", vo.getName()), false, true, true));
                }
                found = true;
                break;
            case EventTypeNames.SYSTEM:
                //System
                for(SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class)) {

                    if(!StringUtils.equals(def.getTypeName(), subtype))
                        continue;

                    found=true;
                    for(EventTypeVO type : def.generatePossibleEventTypesWithReferenceId1(user, subtype)) {
                        SystemEventType eventType = (SystemEventType) type.getEventType();
                        SystemEventTypeModel model = modelMapper.map(eventType, SystemEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), true, def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                    break;
                }
                break;
            case EventTypeNames.AUDIT:
                // Audit does not yet support reference id 1
                throw new BadRequestException();
        }

        if(!found) {
            //Module defined
            for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
                if(StringUtils.equals(typeName, def.getTypeName())) {
                    found = true;
                    for(EventTypeVO type : def.generatePossibleEventTypesWithReferenceId1(user, subtype, permissionService)) {
                        EventType eventType = type.getEventType();
                        AbstractEventTypeModel<?,?,?> model = modelMapper.map(eventType, AbstractEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), def.supportsSubType(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                    break;
                }
            }
        }
        if(!found)
            throw new NotFoundException();
        return types;
    }

    /**
     * Generate a list of all event types generalized by sub-type and referenceId1
     *
     * @param typeName
     * @param subtype
     * @param user
     * @return
     * @throws NotFoundException
     */
    private List<EventTypeVOModel<?,?,?>> getEventTypesForSubtypeAndReferenceId1(String typeName, String subtype, Integer referenceId1, PermissionHolder user) throws NotFoundException {
        //track if the type was a default type
        List<EventTypeVOModel<?,?,?>> types = new ArrayList<>();
        boolean found = false;
        switch(typeName) {
            case EventTypeNames.DATA_POINT:
                //There is no subtype for data points
                if(subtype != null)
                    throw new BadRequestException();

                DataPointVO dp = this.dataPointService.get(referenceId1);

                for(AbstractPointEventDetectorVO vo : eventDetectorDao.getWithSource(dp.getId(), dp)) {
                    AbstractPointEventDetectorModel<?> edm =  modelMapper.map(vo, AbstractPointEventDetectorModel.class, user);
                    EventTypeVO type = vo.getEventType();
                    DataPointEventType eventType = (DataPointEventType)type.getEventType();
                    DataPointEventTypeModel model = new DataPointEventTypeModel(eventType, modelMapper.map(dp, DataPointModel.class, user), edm);
                    types.add(new EventTypeVOModel<DataPointEventType, DataPointModel,AbstractPointEventDetectorModel<?>>(model, type.getDescription(), type.getAlarmLevel(), false, true, true));
                }
                found = true;
                break;
            case EventTypeNames.DATA_SOURCE:
                //There is no subtype for data sources
                if(subtype != null)
                    throw new BadRequestException();

                DataSourceVO ds = dataSourceDao.get(referenceId1);
                if(ds == null)
                    throw new NotFoundException();

                permissionService.ensurePermission(user, ds.getReadPermission());
                AbstractDataSourceModel<?> dsModel = modelMapper.map(ds, AbstractDataSourceModel.class, user);
                for(EventTypeVO type : ds.getEventTypes()) {
                    DataSourceEventType eventType = (DataSourceEventType)type.getEventType();
                    DataSourceEventTypeModel model = new DataSourceEventTypeModel(eventType, dsModel);
                    types.add(new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>, String>(model, type.getDescription(), type.getAlarmLevel(), false, true, true));
                }
                found = true;
                break;
            case EventTypeNames.PUBLISHER:
                //There is no subtype for publishers
                if(subtype != null)
                    throw new BadRequestException();

                //There are no permissions for publishers
                if(!permissionService.hasAdminRole(user))
                    throw new PermissionException(new TranslatableMessage("permission.exception.doesNotHaveRequiredPermission", user), user);

                PublisherVO<?> pub = publisherDao.get(referenceId1);
                if(pub == null)
                    throw new NotFoundException();

                for(EventTypeVO type : pub.getEventTypes()) {
                    PublisherEventType eventType = (PublisherEventType)type.getEventType();
                    AbstractPublisherModel<?,?> publisherModel = modelMapper.map(pub, AbstractPublisherModel.class, user);
                    PublisherEventTypeModel model = new PublisherEventTypeModel(eventType, publisherModel);
                    types.add(new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>, String>(model, type.getDescription(), type.getAlarmLevel(), false, true, true));
                }

                found = true;
                break;
            case EventTypeNames.SYSTEM:
                //System
                for(SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class)) {
                    if(!StringUtils.equals(def.getTypeName(), subtype))
                        continue;
                    found=true;
                    for(EventTypeVO type : def.generatePossibleEventTypesWithReferenceId2(user, subtype, referenceId1)) {
                        SystemEventType eventType = (SystemEventType) type.getEventType();
                        SystemEventTypeModel model = modelMapper.map(eventType, SystemEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), true, def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                    break;
                }
                break;
            case EventTypeNames.AUDIT:
                // Audit does not yet support reference id 2
                throw new BadRequestException();
        }

        if(!found) {
            //Module defined
            for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
                if(StringUtils.equals(typeName, def.getTypeName())) {
                    found = true;
                    for(EventTypeVO type : def.generatePossibleEventTypesWithReferenceId2(user, subtype, referenceId1, permissionService)) {
                        EventType eventType = type.getEventType();

                        if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                            continue;

                        if(!permissionService.hasEventTypePermission(user, eventType))
                            continue;

                        AbstractEventTypeModel<?,?,?> model = modelMapper.map(eventType, AbstractEventTypeModel.class, user);
                        types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), def.supportsSubType(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                    }
                    break;
                }
            }
        }
        if(!found)
            throw new NotFoundException();
        return types;
    }
}
