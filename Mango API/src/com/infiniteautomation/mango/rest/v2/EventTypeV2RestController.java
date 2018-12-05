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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLToPagedObjectListQuery;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.TypedResultWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventTypeVOModel;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.EventType.EventTypeNames;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.publish.PublisherVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

/**
 * Get possible events to handle.
 * @author Phillip Dunlap
 */
@Api(value="Event Types")
@RestController()
@RequestMapping("/event-types")
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
    public TypedResultWithTotal<EventTypeVOModel<?>> queryEventTypes(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        RQLToPagedObjectListQuery<EventTypeVOModel<?>> filter = new RQLToPagedObjectListQuery<>();

        //First prune the list based on permissions
        List<EventTypeVOModel<?>> models = getAllEventTypesForUser(user);

        List<EventTypeVOModel<?>> results = query.accept(filter, models);
        return new TypedResultWithTotal<EventTypeVOModel<?>>() {

            @Override
            public List<EventTypeVOModel<?>> getItems() {
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<EventTypeVOModel<?>> getAllEventTypesForUser(User user) {
        List<EventTypeVOModel<?>> types = new ArrayList<>();

        //Data Points
        for(DataPointVO vo : DataPointDao.getInstance().getAllFull()) {
            if(vo.getEventDetectors() != null) {
                for(AbstractPointEventDetectorVO<?> ed : vo.getEventDetectors()) {
                    EventTypeVO type = ed.getEventType();
                    if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                        AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                        types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
                    }
                }
            }
        }
        //Data Sources
        for(DataSourceVO<?> vo : DataSourceDao.getInstance().getAll()) {
            for(EventTypeVO type : vo.getEventTypes()) {
                if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                    AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                    types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
                }
            }
        }
        //Publishers
        for(PublisherVO<?> vo : PublisherDao.getInstance().getAll()) {
            for(EventTypeVO type : vo.getEventTypes()) {
                if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                    AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                    types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
                }
            }
        }
        //System
        for(EventTypeVO type : SystemEventType.getRegisteredEventTypes()) {
            if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
            }
        }
        // Audit
        for(EventTypeVO type : AuditEventType.getRegisteredEventTypes()) {
            if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
            }
        }
        //Module defined
        for(EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class)) {
            if(def.getHandlersRequireAdmin() && user.hasAdminPermission()) {
                for(EventTypeVO type : def.getEventTypeVOs()) {
                    AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                    types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
                }
            }else {
                for(EventTypeVO type : def.getEventTypeVOs()) {
                    if(Permissions.hasEventTypePermission(user, type.getEventType())) {
                        AbstractEventTypeModel<?> model = modelMapper.map(type.getEventType(), AbstractEventTypeModel.class, user);
                        types.add(new EventTypeVOModel(model, type.getDescription(), type.getAlarmLevel()));
                    }
                }
            }
        }
        return types;
    }
}
