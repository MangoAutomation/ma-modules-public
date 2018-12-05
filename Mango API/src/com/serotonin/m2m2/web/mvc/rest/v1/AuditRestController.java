/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.db.query.appender.ReverseEnumColumnQueryAppender;
import com.infiniteautomation.mango.db.query.appender.ExportCodeColumnQueryAppender;
import com.infiniteautomation.mango.rest.v2.exception.InvalidRQLRestException;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.db.dao.AuditEventDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PageQueryStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.audit.AuditEventInstanceModel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.jazdw.rql.parser.ASTNode;


/**
 * Access to Audit Tracking.  Currently View Only.
 *
 * TODO Implement restoration, See Restorer and TemplateRestorer
 *
 * @author Terry Packer
 */
@Api(value="Audit System", description="Restore/Read Configuration From History")
@RestController
@RequestMapping("/audit")
public class AuditRestController extends MangoVoRestController<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao> {

    public AuditRestController() {
        super(AuditEventDao.getInstance());
        this.appenders.put("alarmLevel", new ReverseEnumColumnQueryAppender<>(AlarmLevels.class));
        this.appenders.put("changeType", new ExportCodeColumnQueryAppender(AuditEventInstanceVO.CHANGE_TYPE_CODES));
    }

    @ApiOperation(
            value = "Query Audit Events",
            notes = "Admin access only",
            response=AuditEventInstanceModel.class,
            responseContainer="Array"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>> queryRQL(HttpServletRequest request) {

        RestProcessResult<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>> result = new RestProcessResult<PageQueryStream<AuditEventInstanceVO, AuditEventInstanceModel, AuditEventDao>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if(result.isOk()){
            try{
                if(!user.isAdmin()){
                    result.addRestMessage(getUnauthorizedMessage());
                    return result.createResponseEntity();
                }else{
                    //Limit our results based on the fact that our permissions should be in the permissions strings
                    ASTNode root = RQLUtils.parseRQLtoAST(request.getQueryString());
                    return result.createResponseEntity(getPageStream(root));
                }
            }catch(InvalidRQLRestException e){
                result.addRestMessage(getInternalServerErrorMessage(e.getMessage()));
                return result.createResponseEntity();
            }
        }

        return result.createResponseEntity();
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

        if(!user.isAdmin())
            throw new AccessDeniedException(user.getUsername() + " not admin, permission denied.");

        return AuditEventType.getRegisteredEventTypes().stream().map(vo -> {
            EventTypeInfo info = new EventTypeInfo();
            info.type = vo.getEventType().getEventType();
            info.subtype = vo.getEventType().getEventSubtype();
            info.description = vo.getDescription();
            info.alarmLevel = vo.getAlarmLevel();
            return info;
        }).collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.rest.v1.MangoVoRestController#createModel(com.serotonin.m2m2.vo.AbstractBasicVO)
     */
    @Override
    public AuditEventInstanceModel createModel(AuditEventInstanceVO vo) {
        return new AuditEventInstanceModel(vo);
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
