/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.pojo.RQLFilterJavaBean;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.event.EventInstanceModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

/**
 * REST Endpoints for User Event Cache access, All un-acknowledged user events are held in the cache
 * on a per user basis.
 *
 * Note that querying an object is different to querying the database.  If a property is queried on
 * that does not exist in a given object, it will not fail and simply not match that criteria.  Since
 * the list of user events contains various event types, each item in the list can have different properties.
 *
 * @author Terry Packer
 */
@Api(value="User Events", description="User events are all un-acknowledged events for a user")
@RestController()
@RequestMapping("/user-events")
public class UserEventsController extends AbstractMangoRestController {

    private final BiFunction<EventInstance, PermissionHolder, EventInstanceModel> map;

    @Autowired
    public UserEventsController(RestModelMapper modelMapper) {
        this.map = (vo, user) -> {
            return modelMapper.map(vo, EventInstanceModel.class, user);
        };
    }

    @ApiOperation(
            value = "Query User Events",
            notes = "Query via rql in url against events for the current user",
            response=EventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "")
    public StreamWithTotal<EventInstanceModel> query(
            @AuthenticationPrincipal PermissionHolder user,
            Translations translations,
            ASTNode query) {

        List<EventInstanceModel> events = Common.eventManager.getAllActiveUserEvents(user).stream().map(e -> {
            return map.apply(e, user);
        }).collect(Collectors.toList());

        return new FilteredStreamWithTotal<>(events, new EventFilter(query, translations));
    }

    public static class EventFilter extends RQLFilterJavaBean<EventInstanceModel> {

        public EventFilter(ASTNode node, Translations translations) {
            super(node, translations);
        }

        @Override
        protected String mapPropertyName(String propertyName) {
            if ("eventType.eventSubtype".equals(propertyName)) {
                return "eventType.subType";
            }
            return propertyName;
        }

    }

}
