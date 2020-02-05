/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedBasicVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.audit.AuditEventInstanceModel;
import com.infiniteautomation.mango.spring.service.AuditEventService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
    private final Map<String, Function<Object, Object>> valueConverterMap;

    @Autowired
    public AuditRestController(AuditEventService service) {
        this.service = service;
        this.valueConverterMap = new HashMap<>();
        this.valueConverterMap.put("alarmLevel", (toConvert) -> {
            return Enum.valueOf(AlarmLevels.class, (String)toConvert).value();
        });
        this.valueConverterMap.put("changeType", (toConvert) -> {
            return AuditEventInstanceVO.CHANGE_TYPE_CODES.getId((String)toConvert);
        });
        this.modelMap = new HashMap<String,String>();
    }

    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface AuditQueryResult extends ListWithTotal<AuditEventInstanceModel> {
    }

    @ApiOperation(
            value = "Query Audit Events",
            notes = "Admin access only",
            response=AuditQueryResult.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "typeName", paramType="query"),
        @ApiImplicitParam(name = "alarmLevel", paramType="query", allowableValues = "NONE,INFORMATION,IMPORTANT,WARNING,URGENT,CRITICAL,LIFE_SAFETY,DO_NOT_LOG,IGNORE"),
        @ApiImplicitParam(name = "changeType", paramType="query", allowableValues = "CREATE,MODIFY,DELETE"),
        @ApiImplicitParam(name = "objectId", paramType="query", dataType = "int"),
        @ApiImplicitParam(name = "timestamp", paramType="query", dataType = "long")
    })
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
            notes = "Admin access only"
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

    protected StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        if (user.hasAdminRole()) {
            return new StreamedBasicVORqlQueryWithTotal<>(service, rql, null, valueConverterMap, vo -> map.apply(vo, user));
        } else {
            // Add some conditions to restrict based on user permissions
            rql = RQLUtils.addAndRestriction(rql, new ASTNode("eq", "id", user.getId()));
            return new StreamedBasicVORqlQueryWithTotal<>(service, rql, null, valueConverterMap, vo -> service.hasReadPermission(user, vo), vo -> map.apply(vo, user));
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
