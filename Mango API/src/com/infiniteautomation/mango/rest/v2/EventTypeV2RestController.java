/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Phillip Dunlap
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToPagedObjectListQuery;
import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.AuditEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataSourceEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventTypeVOModel;
import com.infiniteautomation.mango.rest.v2.model.event.PublisherEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.SystemEventTypeModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.DataPointTagsDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDetectorDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.db.dao.ResultsWithTotal;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;

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
@RequestMapping("/event-types")
public class EventTypeV2RestController {

    private final DataSourceDao<?> dataSourceDao;
    private final PublisherDao publisherDao;
    private final EventDetectorDao<?> eventDetectorDao;
    private final RestModelMapper modelMapper;

    @Autowired
    public EventTypeV2RestController(
            DataSourceDao<?> dataSourceDao,
            PublisherDao publisherDao,
            EventDetectorDao<?> eventDetectorDao,
            RestModelMapper modelMapper) {
        this.dataSourceDao = dataSourceDao;
        this.publisherDao = publisherDao;
        this.eventDetectorDao = eventDetectorDao;
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
    @RequestMapping(method = RequestMethod.GET, value="/{types}")
    public ListWithTotal<EventTypeVOModel<?,?>> queryEventTypes(
            @ApiParam(value = "Event types to query over", required = true, allowMultiple = true) 
            @PathVariable String[] types,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        RQLToPagedObjectListQuery<EventTypeVOModel<?,?>> filter = new RQLToPagedObjectListQuery<>();

        //First fill/prune the list based on permissions
        List<EventTypeVOModel<?,?>> models = new ArrayList<>();
        for(String type : types) {
            getEventTypesForUser(type, models, user);
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

    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(
            value = "Get current possible event types",
            notes = "",
            response=EventTypeNameModel.class,
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


    /**
     * Get all event types readable by a user
     * @param user
     * @return
     */

    private void getEventTypesForUser(String typeName, List<EventTypeVOModel<?,?>> types, User user) {
        //track if the type was a default type
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
                    //Shortcut to check permissions via event type
                    if(dp!= null && Permissions.hasDataPointReadPermission(user, dp)) {
                        dp.setTags(DataPointTagsDao.getInstance().getTagsForDataPointId(dp.getId()));
                        DataPointEventTypeModel model = new DataPointEventTypeModel(eventType, new DataPointModel(dp));
                        types.add(new EventTypeVOModel<DataPointEventType, DataPointModel>(model, type.getDescription(), type.getAlarmLevel()));
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
                        if(vo != null && Permissions.hasDataSourcePermission(user, vo)) {
                            DataSourceEventTypeModel model = new DataSourceEventTypeModel(eventType, vo.asModel());
                            types.add(new EventTypeVOModel<DataSourceEventType, AbstractDataSourceModel<?>>(model, type.getDescription(), type.getAlarmLevel()));
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
                        if(Permissions.hasEventTypePermission(user, eventType)) {
                            PublisherEventTypeModel model = new PublisherEventTypeModel(eventType, vo.asModel());
                            types.add(new EventTypeVOModel<PublisherEventType, AbstractPublisherModel<?,?>>(model, type.getDescription(), type.getAlarmLevel()));
                        }
                    }
                }
                found = true;
            break;
            case EventTypeNames.SYSTEM:
                //System
                for(EventTypeVO type : SystemEventType.getRegisteredEventTypes()) {
                    SystemEventType eventType = (SystemEventType)type.getEventType();
                    if(Permissions.hasEventTypePermission(user, eventType)) {
                        SystemEventTypeModel model = new SystemEventTypeModel(eventType);
                        types.add(new EventTypeVOModel<SystemEventType, Void>(model, type.getDescription(), type.getAlarmLevel()));
                    }
                }
                found=true;
            break;
            case EventTypeNames.AUDIT:
                // Audit
                for(EventTypeVO type : AuditEventType.getRegisteredEventTypes()) {
                    AuditEventType eventType = (AuditEventType)type.getEventType();
                    if(Permissions.hasEventTypePermission(user, eventType)) {
                        AuditEventTypeModel model = new AuditEventTypeModel(eventType);
                        types.add(new EventTypeVOModel<AuditEventType, Void>(model, type.getDescription(), type.getAlarmLevel()));
                    }
                }
                found = true;
            break;
        }
        if(!found) {
            //Module defined
            for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
                if(StringUtils.equals(typeName, def.getTypeName())) {
                    if(def.getHandlersRequireAdmin() && user.hasAdminPermission()) {
                        for(EventTypeVO type : def.getEventTypeVOs()) {
                            AbstractEventTypeModel<?,?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                            types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel()));
                        }
                    }else {
                        for(EventTypeVO type : def.getEventTypeVOs()) {
                            if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                                AbstractEventTypeModel<?,?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                                types.add(new EventTypeVOModel<>(model, type.getDescription(), type.getAlarmLevel()));
                            }
                        }
                    }
                }
            }
        }
    }
}
