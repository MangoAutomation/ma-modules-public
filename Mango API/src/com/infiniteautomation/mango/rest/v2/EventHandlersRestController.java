/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jooq.Field;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptModel;
import com.infiniteautomation.mango.rest.v2.model.javascript.MangoJavaScriptResultModel;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataTypeEnum;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.db.EventHandlerTableDefinition;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.infiniteautomation.mango.spring.service.MangoJavaScriptService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.WorkItemInfo;
import com.infiniteautomation.mango.util.script.MangoJavaScript;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.email.UsedImagesDirective;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.RenderedPointValueTime;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.rt.event.handlers.EmailHandlerRT;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.script.EventInstanceWrapper;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Event Handlers Rest Controller")
@RestController("EventHandlersRestControllerV2")
@RequestMapping("/event-handlers")
public class EventHandlersRestController {

    private final EventHandlerService service;
    private final BiFunction<AbstractEventHandlerVO, User, AbstractEventHandlerModel<? extends AbstractEventHandlerVO>> map;
    private final MangoJavaScriptService javaScriptService;

    private final Map<String, Function<Object, Object>> valueConverters;
    private final Map<String, Field<?>> fieldMap;

    @Autowired
    public EventHandlersRestController(EventHandlerService service, MangoJavaScriptService javaScriptService, final RestModelMapper modelMapper) {
        this.service = service;

        //Map the event types into the model
        this.map = (vo, user) -> {
            List<AbstractEventTypeModel<?,?, ?>> eventTypes = service.getDao().getEventTypesForHandler(vo.getId()).stream().map(type -> {
                return (AbstractEventTypeModel<?,?, ?>) modelMapper.map(type, AbstractEventTypeModel.class, user);
            }).collect(Collectors.toList());
            @SuppressWarnings("unchecked")
            AbstractEventHandlerModel<? extends AbstractEventHandlerVO> model = modelMapper.map(vo, AbstractEventHandlerModel.class, user);
            model.setEventTypes(eventTypes);
            return model;
        };
        this.javaScriptService = javaScriptService;

        this.valueConverters = new HashMap<>();
        //Setup any exposed special query aliases to map model fields to db columns
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("eventTypeName", EventHandlerTableDefinition.EVENT_HANDLER_MAPPING_EVENT_TYPE_NAME_ALIAS);
        this.fieldMap.put("eventSubtypeName", EventHandlerTableDefinition.EVENT_HANDLER_MAPPING_EVENT_SUB_TYPE_NAME_ALIAS);
        this.fieldMap.put("eventTypeRef1", EventHandlerTableDefinition.EVENT_HANDLER_MAPPING_TYPEREF1_ALIAS);
        this.fieldMap.put("eventTypeRef2", EventHandlerTableDefinition.EVENT_HANDLER_MAPPING_TYPEREF2_ALIAS);
    }

    @ApiOperation(
            value = "Query Event Handlers",
            notes = "",
            responseContainer="List",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Get an Event Handler",
            notes = "",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractEventHandlerModel<?> get(
            @ApiParam(value = "XID to get", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Create an Event Handler",
            notes = "Requires global Event Handler privileges",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventHandlerModel<?>> create(
            @RequestBody AbstractEventHandlerModel<? extends AbstractEventHandlerVO> model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventHandlerVO vo = service.insert(model.toVO());
        URI location = builder.path("/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update an Event Handler",
            notes = "Requires edit permission",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<? extends AbstractEventHandlerVO>> update(
            @ApiParam(value = "XID of Event Handler to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Event Handler of update", required = true, allowMultiple = false)
            @RequestBody AbstractEventHandlerModel<? extends AbstractEventHandlerVO> model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventHandlerVO vo = service.update(xid, model.toVO());
        URI location = builder.path("/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update an Event Handler",
            notes = "Requires edit permission",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<?>> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated maintenance event", required = true)
            @PatchVORequestBody(
                    service=EventHandlerService.class,
                    modelClass=AbstractEventHandlerModel.class)
            AbstractEventHandlerModel<? extends AbstractEventHandlerVO> model,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        AbstractEventHandlerVO vo = service.update(xid, model.toVO());

        URI location = builder.path("/event-handlers/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete an EventHandler",
            notes = "",
            response=AbstractEventHandlerModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<AbstractEventHandlerModel<?>> delete(
            @ApiParam(value = "XID of EventHandler to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(map.apply(service.delete(xid), user));
    }

    @ApiOperation(
            value = "Validate an Event Handler without saving it",
            notes = "Admin Only",
            response=Void.class
            )
    @PreAuthorize("isAdmin()")
    @RequestMapping(method = RequestMethod.POST, value="/validate")
    public void validate(
            @RequestBody AbstractEventHandlerModel<? extends AbstractEventHandlerVO> model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        service.ensureValid(model.toVO(), user);
    }

    @ApiOperation(
            value = "Validate a set point event handler script",
            notes = "Admin Only"
            )
    @RequestMapping(method = RequestMethod.POST, value="/validate-set-point-handler-script")
    public MangoJavaScriptResultModel validateSetPointHandlerScript(
            @RequestBody MangoJavaScriptModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) throws IOException, EncryptedDocumentException, InvalidFormatException {

        //Add in the additional validation context
        if(model.getAdditionalContext() == null) {
            model.setAdditionalContext(new HashMap<>());
        }
        model.getAdditionalContext().computeIfAbsent(EventInstance.CONTEXT_KEY, (k) -> {
            return new ValidationEventInstance();
        });
        model.getAdditionalContext().computeIfAbsent(EventInstanceWrapper.CONTEXT_KEY, (k) -> {
            return new EventInstanceWrapper(new ValidationEventInstance());
        });

        model.getAdditionalContext().computeIfAbsent(MangoJavaScriptService.UNCHANGED_KEY, (k) -> {
            return MangoJavaScriptService.UNCHANGED;
        });

        return validateScript(model, user, "eventHandlers.setPoint.successNoValueSet");
    }

    @ApiOperation(
            value = "Validate an email event handler script",
            notes = "Admin Only"
            )
    @RequestMapping(method = RequestMethod.POST, value="/validate-email-handler-script")
    public MangoJavaScriptResultModel validateEmailHandlerScript(
            @RequestBody MangoJavaScriptModel model,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) throws IOException, EncryptedDocumentException, InvalidFormatException {

        //Add in the additional validation context
        if(model.getAdditionalContext() == null) {
            model.setAdditionalContext(new HashMap<>());
        }
        model.getAdditionalContext().computeIfAbsent(EmailHandlerRT.DO_NOT_SEND_KEY, (k) -> {
            return MangoJavaScriptService.UNCHANGED;
        });

        //Setup the email ftl model
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put(EventInstance.CONTEXT_KEY,  new ValidationEventInstance());
        emailModel.put(EventInstanceWrapper.CONTEXT_KEY, new EventInstanceWrapper(new ValidationEventInstance()));
        emailModel.put("additionalContext", new HashMap<>());
        emailModel.put("renderedPointValues", new ArrayList<RenderedPointValueTime>());
        emailModel.put("renderedHtmlPointValues", new ArrayList<RenderedPointValueTime>());
        emailModel.put("img", new UsedImagesDirective());
        emailModel.put("instanceDescription", SystemSettingsDao.instance.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));

        //Get the Work Items
        List<WorkItemInfo> highPriorityWorkItems = Common.backgroundProcessing.getHighPriorityServiceItems();
        emailModel.put("highPriorityWorkItems", highPriorityWorkItems);
        List<WorkItemInfo> mediumPriorityWorkItems = Common.backgroundProcessing.getMediumPriorityServiceQueueItems();
        emailModel.put("mediumPriorityWorkItems", mediumPriorityWorkItems);
        List<WorkItemInfo> lowPriorityWorkItems = Common.backgroundProcessing.getLowPriorityServiceQueueItems();
        emailModel.put("lowPriorityWorkItems", lowPriorityWorkItems);
        emailModel.put("threadList",  new ArrayList<Map<String,Object>> ());

        model.getAdditionalContext().computeIfAbsent("model", (k) -> {
            return emailModel;
        });
        return validateScript(model, user, "eventHandlers.script.successNoEmail");
    }

    private MangoJavaScriptResultModel validateScript(MangoJavaScriptModel model, User user, String noChangeTranslationKey) {
        //Set to potentially return a String
        model.setResultDataType(DataTypeEnum.ALPHANUMERIC.name());
        MangoJavaScript jsVo = model.toVO();
        jsVo.setWrapInFunction(true);
        return new MangoJavaScriptResultModel(javaScriptService.testScript(jsVo, noChangeTranslationKey));
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminRole()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, this.fieldMap, this.valueConverters, item -> true, vo -> map.apply(vo, user));
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, this.fieldMap, this.valueConverters, item -> service.hasReadPermission(user, item), vo -> map.apply(vo, user));
        }
    }

    public static class ValidationEventInstance extends EventInstance {
        public ValidationEventInstance() {
            super(new ValidationEventType(), Common.timer.currentTimeMillis(), true, AlarmLevels.URGENT, new TranslatableMessage("common.validate"), new HashMap<>());
        }
    }

    public static class ValidationEventType extends EventType {

        @Override
        public String getEventType() {
            return "VALIDATION";
        }

        @Override
        public String getEventSubtype() {
            return "validationSubType";
        }

        @Override
        public DuplicateHandling getDuplicateHandling() {
            return DuplicateHandling.ALLOW;
        }

        @Override
        public int getReferenceId1() {
            return 0;
        }

        @Override
        public int getReferenceId2() {
            return 0;
        }

        @Override
        public boolean hasPermission(PermissionHolder user, PermissionService service) {
            return true;
        }

    }
}
