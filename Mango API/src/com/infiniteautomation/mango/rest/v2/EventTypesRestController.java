/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToPagedObjectListQuery;
import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.v2.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.AuditEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataSourceEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventTypeVOModel;
import com.infiniteautomation.mango.rest.v2.model.event.PublisherEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.SystemEventTypeModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
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
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */@Api(value="Event Types")
 @RestController()
 @RequestMapping("/event-types-v2")
public class EventTypesRestController {

     
     private final DataSourceDao<?> dataSourceDao;
     private final PublisherDao publisherDao;
     private final EventDetectorDao<?> eventDetectorDao;
     private final RestModelMapper modelMapper;

     @Autowired
     public EventTypesRestController(
             DataSourceDao<?> dataSourceDao,
             PublisherDao publisherDao,
             EventDetectorDao<?> eventDetectorDao,
             RestModelMapper modelMapper) {
         this.dataSourceDao = dataSourceDao;
         this.publisherDao = publisherDao;
         this.eventDetectorDao = eventDetectorDao;
         this.modelMapper = modelMapper;
     }
     
     
     @ApiOperation(
             value = "Query all available event types",
             notes = "Not specific to any reference ids, results come back based on type/sub-type combinations",
             response=EventTypeVOModel.class,
             responseContainer="List")
     @RequestMapping(method = RequestMethod.GET)
     public ListWithTotal<EventTypeVOModel<?,?>> queryAllEventTypes(
             @AuthenticationPrincipal User user,
             HttpServletRequest request) {

         ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
         RQLToPagedObjectListQuery<EventTypeVOModel<?,?>> filter = new RQLToPagedObjectListQuery<>();

         List<EventTypeVOModel<?,?>> models = new ArrayList<>();
         
         //Data Source
         DataSourceEventType dset = new DataSourceEventType(0, 0);
         DataSourceEventTypeModel dsetm = new DataSourceEventTypeModel(dset, null);
         EventTypeVOModel<?,?> dataSource = new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>>(dsetm, new TranslatableMessage("eventHandlers.dataSourceEvents"), null, true, true);
         models.add(dataSource);
         
         //Data point
         DataPointEventType dpet = new DataPointEventType(0, 0);
         DataPointEventTypeModel dpetm = new DataPointEventTypeModel(dpet, null);
         EventTypeVOModel<?,?> dataPoint = new EventTypeVOModel<DataPointEventType, DataPointModel>(dpetm, new TranslatableMessage("eventHandlers.pointEventDetector"), null, true, true);
         models.add(dataPoint);

         //Publisher
         PublisherEventType pet = new PublisherEventType(0, 0);
         PublisherEventTypeModel petm = new PublisherEventTypeModel(pet, null);
         EventTypeVOModel<?,?> publisher = new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>>(petm, new TranslatableMessage("eventHandlers.publisherEvents"), null, true, true);
         models.add(publisher);
         
         //System
         for(SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class)) {
             EventTypeVO vo = SystemEventType.getEventType(def.getTypeName());
             SystemEventType set = (SystemEventType) vo.getEventType();
             SystemEventTypeModel setm = new SystemEventTypeModel(set);
             EventTypeVOModel<?,?> system = new EventTypeVOModel<SystemEventType, Void>(setm, vo.getDescription(), vo.getAlarmLevel(), def.supportsReferenceId1(), def.supportsReferenceId2());
             models.add(system);
         }
         
         //Audit
         for(EventTypeVO vo : AuditEventType.getRegisteredEventTypes()) {
             AuditEventType aet = (AuditEventType) vo.getEventType();
             AuditEventTypeModel aetm = new AuditEventTypeModel(aet);
             EventTypeVOModel<?,?> audit = new EventTypeVOModel<AuditEventType, Void>(aetm, vo.getDescription(), vo.getAlarmLevel(), true, false);
             models.add(audit);
         }

         //Module defined
         for (EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
             if(def.getHandlersRequireAdmin() && user.hasAdminPermission()) {
                 List<String> subtypes = def.getEventSubTypes();
                 for(String subtype: subtypes) {
                     EventType et = def.createEventType(subtype, 0, 0);
                     AbstractEventTypeModel<?,?> model = modelMapper.map(et, AbstractEventTypeModel.class, user);
                     models.add(new EventTypeVOModel<>(model, new TranslatableMessage(def.getDescriptionKey()), null, def.supportsReferenceId1(), def.supportsReferenceId2()));
                 }
             }else {
                 List<String> subtypes = def.getEventSubTypes();
                 for(String subtype: subtypes) {
                     EventType et = def.createEventType(subtype, 0, 0);
                     AbstractEventTypeModel<?,?> model = modelMapper.map(et, AbstractEventTypeModel.class, user);
                     models.add(new EventTypeVOModel<>(model, new TranslatableMessage(def.getDescriptionKey()), null, def.supportsReferenceId1(), def.supportsReferenceId2()));
                 } 
             }
         }

         List<EventTypeVOModel<?,?>> results = query.accept(filter, models);
         return new ListWithTotal<EventTypeVOModel<?,?>>() {

             @Override
             public List<EventTypeVOModel<?,?>> getItems() {
                 return results;
             }

             @Override
             public int getTotal() {
                 return filter.getUnlimitedSize();
             }

         };
     }
     
     @ApiOperation(
             value = "Query event types from one eventType",
             notes = "ReferenceId 1 is set, only available if the type supports referenceId1",
             response=EventTypeVOModel.class,
             responseContainer="List")
     @RequestMapping(method = RequestMethod.GET, value="/{type}/{subtype}")
     public ListWithTotal<EventTypeVOModel<?,?>> queryEventTypesForTypeAndSubType(
             @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
             @PathVariable(value="subtype") @ApiParam(value = "Event subtype to query over", required = true)  String subtype,
             @AuthenticationPrincipal User user,
             HttpServletRequest request) {

         ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
         RQLToPagedObjectListQuery<EventTypeVOModel<?,?>> filter = new RQLToPagedObjectListQuery<>();
         List<EventTypeVOModel<?,?>> models = getEventTypesForUser(type,  StringUtils.equalsIgnoreCase(subtype, "null") ? null : subtype, null, null, user);
         List<EventTypeVOModel<?,?>> results = query.accept(filter, models);
         return new ListWithTotal<EventTypeVOModel<?,?>>() {

             @Override
             public List<EventTypeVOModel<?,?>> getItems() {
                 return results;
             }

             @Override
             public int getTotal() {
                 return filter.getUnlimitedSize();
             }

         };
     }
     
     @ApiOperation(
             value = "Query event types from one eventType",
             notes = "ReferenceId 1 and 2 are set accordinly",
             response=EventTypeVOModel.class,
             responseContainer="List")
     @RequestMapping(method = RequestMethod.GET, value="/{type}/{subtype}/{referenceId1}")
     public ListWithTotal<EventTypeVOModel<?,?>> queryEventTypesForTypeAndSubType(
             @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
             @PathVariable(value="subtype") @ApiParam(value = "Event subtype to query over", required = true) String subtype,
             @PathVariable(value="referenceId1") @ApiParam(value = "Reference ID 1 locator", required = true)  Integer referenceId1,
             @AuthenticationPrincipal User user,
             HttpServletRequest request) {

         ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
         RQLToPagedObjectListQuery<EventTypeVOModel<?,?>> filter = new RQLToPagedObjectListQuery<>();
         List<EventTypeVOModel<?,?>> models = getEventTypesForUser(type, StringUtils.equalsIgnoreCase(subtype, "null") ? null : subtype, referenceId1, null, user);
         List<EventTypeVOModel<?,?>> results = query.accept(filter, models);
         return new ListWithTotal<EventTypeVOModel<?,?>>() {

             @Override
             public List<EventTypeVOModel<?,?>> getItems() {
                 return results;
             }

             @Override
             public int getTotal() {
                 return filter.getUnlimitedSize();
             }

         };
     }
     
     @ApiOperation(
             value = "Query event types from one eventType",
             notes = "ReferenceId 1 and 2 are set accordinly",
             response=EventTypeVOModel.class,
             responseContainer="List")
     @RequestMapping(method = RequestMethod.GET, value="/{type}/{subtype}/{referenceId1}/{referenceId2}")
     public ListWithTotal<EventTypeVOModel<?,?>> queryEventTypesForTypeAndSubType(
             @PathVariable(value="type") @ApiParam(value = "Event type to query over", required = true) String type,
             @PathVariable(value="subtype") @ApiParam(value = "Event subtype to query over", required = true) String subtype,
             @PathVariable(value="referenceId1") @ApiParam(value = "Reference ID 1 locator", required = true)  Integer referenceId1,
             @PathVariable(value="referenceId2") @ApiParam(value = "Reference ID 2 locator", required = true)  Integer referenceId2,
             @AuthenticationPrincipal User user,
             HttpServletRequest request) {

         ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
         RQLToPagedObjectListQuery<EventTypeVOModel<?,?>> filter = new RQLToPagedObjectListQuery<>();
         List<EventTypeVOModel<?,?>> models = getEventTypesForUser(type, StringUtils.equalsIgnoreCase(subtype, "null") ? null : subtype, referenceId1, referenceId2, user);
         List<EventTypeVOModel<?,?>> results = query.accept(filter, models);
         return new ListWithTotal<EventTypeVOModel<?,?>>() {

             @Override
             public List<EventTypeVOModel<?,?>> getItems() {
                 return results;
             }

             @Override
             public int getTotal() {
                 return filter.getUnlimitedSize();
             }

         };
     }
     
     
     private List<EventTypeVOModel<?,?>> getEventTypesForUser(String typeName, String subtype, Integer referenceId1, Integer referenceId2, User user) throws NotFoundException {
         //track if the type was a default type
         List<EventTypeVOModel<?,?>> types = new ArrayList<>();
         boolean found = false;
         switch(typeName) {
             case EventTypeNames.DATA_POINT:
                 //Get Event Detectors
                 List<AbstractPointEventDetectorVO<?>> peds = this.eventDetectorDao.getForSourceType(EventTypeNames.DATA_POINT);
                 for(AbstractPointEventDetectorVO<?> ped : peds) {
                     //This will load the data point into the type
                     EventTypeVO type = ped.getEventType();
                     DataPointVO dp = ped.getDataPoint();
                     DataPointEventType eventType = (DataPointEventType)type.getEventType();
                     if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                         continue;

                     //Shortcut to check permissions via event type
                     if(dp!= null && Permissions.hasDataPointReadPermission(user, dp)) {
                         if(referenceId1 == null) {
                             eventType = new DataPointEventType(eventType.getDataSourceId(), eventType.getDataPointId(), 0, eventType.getDuplicateHandling());
                         }else {
                             if(referenceId1 != eventType.getReferenceId1())
                                 continue;
                             //TODO Fill PED Model?
                         }
                         dp.setTags(DataPointTagsDao.getInstance().getTagsForDataPointId(dp.getId()));
                         DataPointEventTypeModel model = new DataPointEventTypeModel(eventType, new DataPointModel(dp));
                         types.add(new EventTypeVOModel<DataPointEventType, DataPointModel>(model, type.getDescription(), type.getAlarmLevel(), true, true));
                     }
                 }
                 found = true;
             break;
             case EventTypeNames.DATA_SOURCE:
                 //Data Sources
                 for(DataSourceVO<?> vo : dataSourceDao.getAll()) {
                     for(EventTypeVO type : vo.getEventTypes()) {
                         //Shortcut to check permissions via event type
                         DataSourceEventType eventType = (DataSourceEventType)type.getEventType();
                         if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                             continue;
                         if(vo != null && Permissions.hasDataSourcePermission(user, vo)) {
                             if(referenceId1 == null) {
                                 eventType = new DataSourceEventType(eventType.getDataSourceId(), 0, eventType.getAlarmLevel(), eventType.getDuplicateHandling());
                             }else {
                                 if(referenceId1 != eventType.getReferenceId1())
                                     continue;
                             }
                             AbstractDataSourceModel<?> dsModel = modelMapper.map(vo, AbstractDataSourceModel.class, user);
                             DataSourceEventTypeModel model = new DataSourceEventTypeModel(eventType, dsModel);
                             types.add(new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>>(model, type.getDescription(), type.getAlarmLevel(), true, true));
                         }
                     }
                 }
                 found = true;
             break;
             case EventTypeNames.PUBLISHER:
                 //Publishers
                 for(PublisherVO<?> vo : publisherDao.getAll()) {
                     for(EventTypeVO type : vo.getEventTypes()) {
                         PublisherEventType eventType = (PublisherEventType)type.getEventType();
                         if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                             continue;
                         if(Permissions.hasEventTypePermission(user, eventType)) {
                             if(referenceId1 == null) {
                                 eventType = new PublisherEventType(eventType.getPublisherId(), 0);
                             }else {
                                 if(referenceId1 != eventType.getReferenceId1())
                                     continue;
                             }
                             PublisherEventTypeModel model = new PublisherEventTypeModel(eventType, vo.asModel());
                             types.add(new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>>(model, type.getDescription(), type.getAlarmLevel(), true, true));
                         }
                     }
                 }
                 found = true;
             break;
             case EventTypeNames.SYSTEM:
                 //System
                 for(SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class)) {
                     EventTypeVO type = SystemEventType.getEventType(def.getTypeName());
                     SystemEventType eventType = (SystemEventType)type.getEventType();
                     if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                         continue;
                     if(Permissions.hasEventTypePermission(user, eventType)) {
                         if(referenceId1 == null) {
                             //Generate all the system events
                             if(def.supportsReferenceId1()) {
                                 for(SystemEventType possibleType : def.genegeneratePossibleEventTypesWithReferenceId1()) {
                                     SystemEventTypeModel model = new SystemEventTypeModel(possibleType);
                                     types.add(new EventTypeVOModel<SystemEventType, Void>(model, type.getDescription(), type.getAlarmLevel(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                                 }
                             }
                         }else {
                             if(referenceId1 != eventType.getReferenceId1())
                                 continue;
                             SystemEventTypeModel model = new SystemEventTypeModel(eventType);
                             types.add(new EventTypeVOModel<SystemEventType, Void>(model, type.getDescription(), type.getAlarmLevel(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                         }
                     }
                 }
                 found=true;
             break;
             case EventTypeNames.AUDIT:
                 // Audit
                 for(EventTypeVO type : AuditEventType.getRegisteredEventTypes()) {
                     AuditEventType eventType = (AuditEventType)type.getEventType();
                     if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                         continue;
                     if(Permissions.hasEventTypePermission(user, eventType)) {
                         AuditEventTypeModel model = new AuditEventTypeModel(eventType);
                         //For now we don't support type ref 1 however we could
                         types.add(new EventTypeVOModel<AuditEventType, Void>(model, type.getDescription(), type.getAlarmLevel(), false, false));
                     }
                 }
                 found = true;
             break;
         }
         if(!found) {
             //Module defined
             for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
                 if(StringUtils.equals(typeName, def.getTypeName())) {
                     found = true;
                     if(def.getHandlersRequireAdmin() && user.hasAdminPermission()) {
                         for(EventTypeVO type : def.getEventTypeVOs()) {
                             EventType eventType = type.getEventType();
                             if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                                 continue;
                             if(referenceId1 == null) {
                                 //Generate all possible 
                                 eventType = def.createEventType(subtype, eventType.getReferenceId1(), 0);
                             }else {
                                 if(referenceId1 != eventType.getReferenceId1())
                                     continue;
                             }
                             AbstractEventTypeModel<?,?> model = modelMapper.map(eventType, AbstractEventTypeModel.class, user);
                             types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                         }
                     }else {
                         for(EventTypeVO type : def.getEventTypeVOs()) {
                             EventType eventType = type.getEventType();
                             if(!StringUtils.equals(eventType.getEventSubtype(), subtype))
                                 continue;
                             if(Permissions.hasEventTypePermission(user, eventType)) {
                                 if(referenceId1 == null) {
                                     //Generate all possible 
                                     eventType = def.createEventType(subtype, eventType.getReferenceId1(), 0);
                                 }else {
                                     if(referenceId1 != eventType.getReferenceId1())
                                         continue;
                                 }
                                 AbstractEventTypeModel<?,?> model = modelMapper.map(eventType, AbstractEventTypeModel.class, user);
                                 types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel(), def.supportsReferenceId1(), def.supportsReferenceId2()));
                             }
                         }
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
