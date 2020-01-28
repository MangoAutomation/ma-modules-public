/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Field;
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
import com.infiniteautomation.mango.rest.v2.model.TranslatableMessageModel;
import com.infiniteautomation.mango.rest.v2.model.event.DataPointEventSummaryModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.v2.model.event.EventLevelSummaryModel;
import com.infiniteautomation.mango.spring.db.EventInstanceTableDefinition;
import com.infiniteautomation.mango.spring.service.EventInstanceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.DataPointEventLevelSummary;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.UserEventLevelSummary;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Events endpoints")
@RestController()
@RequestMapping("/events")
public class EventsRestController {

    private final RestModelMapper modelMapper;
    private final EventInstanceService service;
    private final BiFunction<EventInstanceVO, User, EventInstanceModel> map;

    private final Map<String, Function<Object, Object>> valueConverters;
    private final Map<String, Field<?>> fieldMap;

    @Autowired
    public EventsRestController(RestModelMapper modelMapper, EventInstanceService service,
            EventInstanceTableDefinition eventTable) {
        this.modelMapper = modelMapper;
        this.service = service;
        this.map = (vo, user) -> {
            return modelMapper.map(vo, EventInstanceModel.class, user);
        };

        this.valueConverters = new HashMap<>();
        //Setup any exposed special query aliases to map model fields to db columns
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("activeTimestamp", eventTable.getAlias("activeTs"));
        this.fieldMap.put("rtnTimestamp", eventTable.getAlias("rtnTs"));
        this.fieldMap.put("userNotified", eventTable.getAlias("silenced"));
        this.fieldMap.put("acknowledged", eventTable.getAlias("ackTs"));
        this.fieldMap.put("acknowledgedTimestamp", eventTable.getAlias("ackTs"));
        this.fieldMap.put("eventType", eventTable.getAlias("typeName"));
        this.fieldMap.put("referenceId1", eventTable.getAlias("typeRef1"));
        this.fieldMap.put("referenceId2", eventTable.getAlias("typeRef2"));
        this.fieldMap.put("acknowledged", eventTable.getAlias("ackTs"));
        this.fieldMap.put("active", eventTable.getAlias("rtnTs"));
    }

    @ApiOperation(
            value = "Get the active events summary",
            notes = "List of counts for all active events by type and the most recent active alarm for each."
            )
    @RequestMapping(method = RequestMethod.GET, value = "/active-summary")
    public List<EventLevelSummaryModel> getActiveSummary(@AuthenticationPrincipal User user) {
        List<UserEventLevelSummary> summaries = service.getActiveSummary(user);
        return summaries.stream().map(s -> {
            EventInstanceModel instanceModel = s.getLatest() != null ? modelMapper.map(s.getLatest(), EventInstanceModel.class, user) : null;
            return new EventLevelSummaryModel(s.getAlarmLevel(), s.getUnsilencedCount(), instanceModel);
        }).collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get summary of data point events",
            notes = "List of counts for all active events by type and the most recent active alarm for each."
            )
    @RequestMapping(method = RequestMethod.POST, value = "/data-point-summaries")
    public List<DataPointEventSummaryModel> getDataPointSummaries(
            @RequestBody(required=true)
            String[] xids,
            @AuthenticationPrincipal User user) {
        Collection<DataPointEventLevelSummary> summaries = service.getDataPointEventSummaries(xids, user);
        return summaries.stream().map(s -> new DataPointEventSummaryModel(s.getXid(), s.getCounts())).collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get event by ID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public EventInstanceModel getById(
            @ApiParam(value = "Valid Event ID", required = true, allowMultiple = false)
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(
            value = "Query Events",
            notes = "Use RQL formatted query",
            response=EventInstanceModel.class,
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
            value = "Acknowledge an existing event",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/acknowledge/{id}")
    public ResponseEntity<EventInstanceModel> acknowledgeEvent(
            @PathVariable Integer id,
            @RequestBody(required=false) TranslatableMessageModel message,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder, HttpServletRequest request) {
        TranslatableMessage tlm = null;
        if (message != null)
            tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());
        EventInstanceVO vo = service.acknowledgeEventById(id, user, tlm);
        URI location = builder.path("/events/{id}").buildAndExpand(id).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Acknowledge many existing events",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.POST, value = "/acknowledge")
    public int acknowledgeManyEvents(
            @RequestBody(required=false) TranslatableMessageModel message,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        TranslatableMessage tlm;
        if(message != null) {
            tlm = new TranslatableMessage(message.getKey(), message.getArgs().toArray());
        }else {
            tlm = null;
        }

        if (!user.hasAdminRole()) {
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "userId", user.getId()));
        }
        AtomicInteger total = new AtomicInteger();
        long ackTimestamp = Common.timer.currentTimeMillis();
        service.customizedQuery(rql, (EventInstanceVO vo, int index) -> {
            EventInstance event = Common.eventManager.acknowledgeEventById(vo.getId(), ackTimestamp, user, tlm);
            if (event != null && event.isAcknowledged()) {
                total.incrementAndGet();
            }
        });
        return total.get();
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        if (user.hasAdminRole()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, fieldMap, valueConverters, item -> true, vo -> map.apply(vo, user));
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, fieldMap, valueConverters, item -> service.hasReadPermission(user, item), vo -> map.apply(vo, user));
        }
    }
}
