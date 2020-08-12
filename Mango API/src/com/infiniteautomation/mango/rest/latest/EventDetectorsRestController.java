/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.latest.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.latest.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.latest.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.latest.model.ActionAndModel;
import com.infiniteautomation.mango.rest.latest.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.AbstractEventDetectorModel;
import com.infiniteautomation.mango.rest.latest.model.event.detectors.rt.AbstractEventDetectorRTModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.db.EventDetectorTableDefinition;
import com.infiniteautomation.mango.spring.service.EventDetectorsService;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.infiniteautomation.mango.util.exception.TranslatableIllegalStateException;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.event.detectors.PointEventDetectorRT;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Terry Packer
 *
 */
@Api(value="Event Detectors, full implementation")
@RestController()
@RequestMapping("/event-detectors")
public class EventDetectorsRestController {

    private final EventDetectorsService service;
    private final BiFunction<AbstractEventDetectorVO, User, AbstractEventDetectorModel<?>> map;
    private final RestModelMapper modelMapper;
    private final Map<String, Field<?>> fieldMap;

    @Autowired
    public EventDetectorsRestController(EventDetectorsService service,
            EventDetectorTableDefinition table,
            RestModelMapper modelMapper,
            TemporaryResourceWebSocketHandler websocket){
        this.service = service;
        this.map = (vo, user) -> {
            AbstractEventDetectorModel<?> model = modelMapper.map(vo, AbstractEventDetectorModel.class, user);
            return model;
        };
        this.modelMapper = modelMapper;
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<EventDetectorBulkResponse>(service.getPermissionService(), websocket);

        this.fieldMap = new HashMap<>();
        this.fieldMap.put("detectorSourceType", table.getAlias("sourceTypeName"));
        //TODO This will break if we add new detector types and keep the same table structure, we should break this out into a mapping table
        this.fieldMap.put("sourceId", table.getAlias("dataPointId"));
    }

    @ApiOperation(
            value = "Query Event Detectors",
            notes = "Use RQL formatted query, filtered by data point read permissions",
            response=AbstractEventDetectorModel.class,
            responseContainer="List"
            )
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal queryRQL(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user, vo -> map.apply(vo, user));
    }

    @ApiOperation(
            value = "Get an Event Detector by xid",
            notes = "User must have read permission for the data point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractEventDetectorModel<?> getByXid(
            @ApiParam(value = "XID of Event detector", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get an Event Detector by id",
            notes = "User must have read permission for the data point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractEventDetectorModel<?> getById(
            @ApiParam(value = "ID of Event detector", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(
            value = "Create an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractEventDetectorModel<?>> create(
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @RequestBody AbstractEventDetectorModel<? extends AbstractEventDetectorVO> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventDetectorVO vo = service.insertAndReload(model.toVO(), restart);
        URI location = builder.path("/event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> update(
            @ApiParam(value = "XID of Event Handler to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Event Handler of update", required = true, allowMultiple = false)
            @RequestBody AbstractEventDetectorModel<? extends AbstractEventDetectorVO> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventDetectorVO vo = service.updateAndReload(xid, model.toVO(), restart);
        URI location = builder.path("/event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update an Event Detector",
            notes = "User must have data source edit permission for source of the point",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> partialUpdate(
            @PathVariable String xid,
            @ApiParam(value = "Event detector to patch", required = true)
            @PatchVORequestBody(
                    service=EventHandlerService.class,
                    modelClass=AbstractEventDetectorModel.class)
            AbstractEventDetectorModel<? extends AbstractEventDetectorVO> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        AbstractEventDetectorVO vo = service.updateAndReload(xid, model.toVO(), restart);

        URI location = builder.path("/event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete an Event Detector",
            notes = "User must have data source edit permission for source of the point, data point will restart",
            response=AbstractEventDetectorModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<AbstractEventDetectorModel<?>> delete(
            @ApiParam(value = "XID to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(map.apply(service.delete(xid), user));
    }

    @ApiOperation(value = "Gets a list of event detectors for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.GET, produces=MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsv(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return this.queryCsvPost(rql, user);
    }

    @ApiOperation(value = "Gets a list of event detectors for bulk import via CSV", notes = "Adds an additional action and originalXid column")
    @RequestMapping(method = RequestMethod.POST, value = "/query", produces=MediaTypes.CSV_VALUE)
    public StreamedArrayWithTotal queryCsvPost(
            @ApiParam(value="RQL query AST", required = true)
            @RequestBody ASTNode rql,

            @AuthenticationPrincipal User user) {

        return doQuery(rql, user, eventDetectorVO -> {
            ActionAndModel<AbstractEventDetectorModel<?>> actionAndModel = new ActionAndModel<>();
            actionAndModel.setAction(VoAction.UPDATE);
            actionAndModel.setOriginalXid(eventDetectorVO.getXid());
            actionAndModel.setModel(map.apply(eventDetectorVO, user));
            return actionAndModel;
        });
    }

    private static final String RESOURCE_TYPE_BULK_EVENT_DETECTOR = "BULK_EVENT_DETECTOR";

    public static class EventDetectorIndividualRequest extends VoIndividualRequest<AbstractEventDetectorModel<? extends AbstractEventDetectorVO>> {
    }

    public static class EventDetectorIndividualResponse extends VoIndividualResponse<AbstractEventDetectorModel<?>> {
    }

    public static class EventDetectorBulkRequest extends BulkRequest<VoAction, AbstractEventDetectorModel<? extends AbstractEventDetectorVO>, EventDetectorIndividualRequest> {
    }

    public static class EventDetectorBulkResponse extends BulkResponse<EventDetectorIndividualResponse> {
    }

    private final TemporaryResourceManager<EventDetectorBulkResponse, AbstractRestException> bulkResourceManager;

    @ApiOperation(value = "Bulk get/create/update/delete event detectors",
            notes = "User must have read permission for the data point or edit permission for the data source",
            consumes=MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestException>> bulkEventDetectorOperationCSV(
            @RequestBody
            List<AbstractEventDetectorModel<? extends AbstractEventDetectorVO>> eds,

            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        EventDetectorBulkRequest bulkRequest = new EventDetectorBulkRequest();

        bulkRequest.setRequests(eds.stream().map(actionAndModel -> {
            AbstractEventDetectorModel<? extends AbstractEventDetectorVO> ed = actionAndModel;
            VoAction action = actionAndModel.getAction();
            String originalXid = actionAndModel.getOriginalXid();
            if (originalXid == null && ed != null) {
                originalXid = ed.getXid();
            }

            EventDetectorIndividualRequest request = new EventDetectorIndividualRequest();
            request.setAction(action == null ? VoAction.UPDATE : action);
            request.setXid(originalXid);
            request.setBody(ed);
            return request;
        }).collect(Collectors.toList()));

        return this.bulkEventDetectorOperation(bulkRequest, restart, user, builder);
    }

    @ApiOperation(value = "Bulk get/create/update/delete event detectors",
            notes = "User must have read permission for the data point or edit permission for the data source")
    @RequestMapping(method = RequestMethod.POST, value="/bulk")
    public ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestException>> bulkEventDetectorOperation(
            @RequestBody
            EventDetectorBulkRequest requestBody,

            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        VoAction defaultAction = requestBody.getAction();
        AbstractEventDetectorModel<? extends AbstractEventDetectorVO> defaultBody = requestBody.getBody();
        List<EventDetectorIndividualRequest> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (requests.isEmpty()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantBeEmpty", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<EventDetectorBulkResponse, AbstractRestException> responseBody = bulkResourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_EVENT_DETECTOR, resourceId, user.getId(), expiration, timeout, (resource) -> {

                    EventDetectorBulkResponse bulkResponse = new EventDetectorBulkResponse();
                    int i = 0;

                    resource.progressOrSuccess(bulkResponse, i++, requests.size());

                    for (EventDetectorIndividualRequest request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        EventDetectorIndividualResponse individualResponse = doIndividualRequest(request, restart, defaultAction, defaultBody, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/event-detectors/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk event detector operations",
            notes = "User can only get their own bulk operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointOperations(
            @AuthenticationPrincipal
            User user,

            ASTNode query,
            Translations translations) {

        List<TemporaryResource<EventDetectorBulkResponse, AbstractRestException>> preFiltered =
                this.bulkResourceManager.list().stream()
                .filter((tr) -> service.getPermissionService().hasAdminRole(user) || user.getId() == tr.getUserId())
                .collect(Collectors.toList());

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(new FilteredStreamWithTotal<>(preFiltered, query, translations));
        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk  event detector operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<EventDetectorBulkResponse, AbstractRestException> updateBulkDataPointOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<EventDetectorBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        if (body.getStatus() == TemporaryResourceStatus.CANCELLED) {
            resource.cancel();
        } else {
            throw new BadRequestException(new TranslatableMessage("rest.error.onlyCancel"));
        }

        return resource;
    }

    @ApiOperation(value = "Get the status of a bulk event detector operation using its id", notes = "User can only get their own bulk operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk/{id}")
    public TemporaryResource<EventDetectorBulkResponse, AbstractRestException> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<EventDetectorBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        return resource;
    }

    @ApiOperation(value = "Remove a bulk event detector operation using its id",
            notes = "Will only remove a bulk operation if it is complete. " +
            "User can only remove their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/bulk/{id}")
    public void removeBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<EventDetectorBulkResponse, AbstractRestException> resource = bulkResourceManager.get(id);

        if (!service.getPermissionService().hasAdminRole(user) && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        resource.remove();
    }

    @ApiOperation(
            value = "Get Event Detector's internal state",
            notes = "User must have read permission for the data point",
            response = AbstractEventDetectorRTModel.class
            )
    @RequestMapping(method = RequestMethod.GET, value="/runtime/{xid}")
    public AbstractEventDetectorRTModel<?> getState(
            @ApiParam(value = "ID of Event detector", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        AbstractEventDetectorVO vo = service.get(xid);
        //For now all detectors are data point type
        DataPointRT rt = Common.runtimeManager.getDataPoint(vo.getSourceId());
        if(rt == null){
            throw new TranslatableIllegalStateException(new TranslatableMessage("rest.error.pointNotEnabled", xid));
        }
        for(PointEventDetectorRT<?> edrt : rt.getEventDetectors()) {
            if(edrt.getVO().getId() == vo.getId()) {
                return modelMapper.map(edrt, AbstractEventDetectorRTModel.class, user);
            }
        }
        throw new NotFoundRestException();
    }

    //TODO improve performance by tracking all data sources that need to be restarted and restart at the end?
    private EventDetectorIndividualResponse doIndividualRequest(EventDetectorIndividualRequest request, boolean restart, VoAction defaultAction, AbstractEventDetectorModel<? extends AbstractEventDetectorVO> defaultBody, UriComponentsBuilder builder) {
        EventDetectorIndividualResponse result = new EventDetectorIndividualResponse();

        try {
            String xid = request.getXid();
            result.setXid(xid);

            VoAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            AbstractEventDetectorModel<? extends AbstractEventDetectorVO> body = request.getBody() == null ? defaultBody : request.getBody();

            User user = (User) Common.getUser();

            switch (action) {
                case GET:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.getByXid(xid, user));
                    break;
                case CREATE:
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.create(restart, body, user, builder).getBody());
                    break;
                case UPDATE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    if (body == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "body"));
                    }
                    result.setBody(body);
                    result.setBody(this.update(xid, body, restart, user, builder).getBody());
                    break;
                case DELETE:
                    if (xid == null) {
                        throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "xid"));
                    }
                    result.setBody(this.delete(xid, user, builder).getBody());
                    break;
            }
        } catch (Exception e) {
            result.exceptionCaught(e);
        }

        return result;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        if (service.getPermissionService().hasAdminRole(user)) {
            export.put("eventDetectors", new StreamedSeroJsonVORqlQuery<>(service, rql, null, fieldMap, null));
        }else {
            export.put("eventDetectors", new StreamedSeroJsonVORqlQuery<>(service, rql, null, fieldMap, null,  vo -> service.hasReadPermission(user, vo)));
        }
        return export;
    }

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<AbstractEventDetectorVO, ?> toModel) {
        //If we are admin or have overall data source permission we can view all
        if (service.getPermissionService().hasAdminRole(user)) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, fieldMap, null, toModel);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, null, fieldMap, null, vo -> service.hasReadPermission(user, vo), toModel);
        }
    }
}
