/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.appender.ExportCodeColumnQueryAppender;
import com.infiniteautomation.mango.db.query.appender.ReverseEnumColumnQueryAppender;
import com.infiniteautomation.mango.db.query.appender.SQLColumnQueryAppender;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedBasicVORqlQueryWithTotal;
import com.infiniteautomation.mango.spring.service.AuditEventService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.web.mvc.rest.v1.model.audit.AuditEventInstanceModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;

/**
 * Access to Audit Tracking.  Currently View Only.
 *
 * TODO Implement restoration, See Restorer and DataSourceRestorer
 *
 * @author Terry Packer
 */
@Api(value="Audit System access")
@RestController
@RequestMapping("/audit")
public class AuditRestController {

    private final AuditEventService service;

    private final BiFunction<AuditEventInstanceVO, PermissionHolder, AuditEventInstanceModel> map = (vo, user) -> {return new AuditEventInstanceModel(vo);};

    //Map of keys -> model members to value -> Vo member/sql column
    protected Map<String, String> modelMap;
    //Map of Vo member/sql column to value converter
    protected Map<String, SQLColumnQueryAppender> appenders;

    @Autowired
    public AuditRestController(AuditEventService service) {
        this.service = service;
        this.appenders = new HashMap<String, SQLColumnQueryAppender>();
        this.appenders.put("alarmLevel", new ReverseEnumColumnQueryAppender<>(AlarmLevels.class));
        this.appenders.put("changeType", new ExportCodeColumnQueryAppender(AuditEventInstanceVO.CHANGE_TYPE_CODES));
        this.modelMap = new HashMap<String,String>();
    }

    @ApiOperation(
            value = "Query Audit Events",
            notes = "Admin access only",
            response=AuditEventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        user.ensureHasAdminRole();
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "List all Audit Event Types in the system",
            notes = "Admin access only",
            response=String.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "list-event-types")
    public List<EventTypeInfo> listEventTypes(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        user.ensureHasAdminRole();

        return AuditEventType.getRegisteredEventTypes().stream().map(vo -> {
            EventTypeInfo info = new EventTypeInfo();
            info.type = vo.getEventType().getEventType();
            info.subtype = vo.getEventType().getEventSubtype();
            info.description = vo.getDescription();
            info.alarmLevel = vo.getAlarmLevel();
            return info;
        }).collect(Collectors.toList());
    }

    public StreamedArrayWithTotal doQuery(ASTNode rql, User user) {

        if (user.hasAdminRole()) {
            return new StreamedBasicVORqlQueryWithTotal<>(service, rql, vo -> map.apply(vo, user));
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            return new StreamedBasicVORqlQueryWithTotal<>(service, rql, user, vo -> map.apply(vo, user));
        }
    }

    class EventTypeInfo {

        String type;
        String subtype;
        TranslatableMessage description;
        AlarmLevels alarmLevel;
        /**
         * @return the type
         */
        public String getType() {
            return type;
        }
        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }
        /**
         * @return the subtype
         */
        public String getSubtype() {
            return subtype;
        }
        /**
         * @param subtype the subtype to set
         */
        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }
        /**
         * @return the description
         */
        public TranslatableMessage getDescription() {
            return description;
        }
        /**
         * @param description the description to set
         */
        public void setDescription(TranslatableMessage description) {
            this.description = description;
        }
        /**
         * @return the alarmLevel
         */
        public AlarmLevels getAlarmLevel() {
            return alarmLevel;
        }
        /**
         * @param alarmLevel the alarmLevel to set
         */
        public void setAlarmLevel(AlarmLevels alarmLevel) {
            this.alarmLevel = alarmLevel;
        }
    }
}
