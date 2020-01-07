/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import com.infiniteautomation.mango.db.query.pojo.RQLToObjectListQuery;
import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.infiniteautomation.mango.rest.v2.bulk.BulkResponse;
import com.infiniteautomation.mango.rest.v2.bulk.VoAction;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualRequest;
import com.infiniteautomation.mango.rest.v2.bulk.VoIndividualResponse;
import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.BadRequestException;
import com.infiniteautomation.mango.rest.v2.model.ActionAndModel;
import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.v2.model.event.detectors.AbstractEventDetectorModel;
import com.infiniteautomation.mango.rest.v2.patch.PatchVORequestBody;
import com.infiniteautomation.mango.rest.v2.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceStatusUpdate;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.EventDetectorsService;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.m2m2.i18n.TranslatableMessage;
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
@Api(value="Event Detectors, full REST v2 implementation")
@RestController()
@RequestMapping("/full-event-detectors")
public class EventDetectorsRestController<T extends AbstractEventDetectorVO<T>> {

    private final EventDetectorsService<T> service;
    private final BiFunction<AbstractEventDetectorVO<T>, User, AbstractEventDetectorModel<?>> map;

    @Autowired
    public EventDetectorsRestController(EventDetectorsService<T> service, RestModelMapper modelMapper, TemporaryResourceWebSocketHandler websocket){
        this.service = service;
        this.map = (vo, user) -> {
            AbstractEventDetectorModel<?> model = modelMapper.map(vo, AbstractEventDetectorModel.class, user);
            return model;
        };
        this.bulkResourceManager = new MangoTaskTemporaryResourceManager<EventDetectorBulkResponse>(websocket);
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
            @RequestBody AbstractEventDetectorModel<T> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        T vo = service.insertAndReload(model.toVO(), restart);
        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
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
            @RequestBody AbstractEventDetectorModel<T> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        T vo = service.updateAndReload(xid, model.toVO(), restart);
        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
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
            AbstractEventDetectorModel<T> model,
            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        T vo = service.updateAndReload(xid, model.toVO(), restart);

        URI location = builder.path("/full-event-detectors/{xid}").buildAndExpand(vo.getXid()).toUri();
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

    public static class EventDetectorIndividualRequest<ED extends AbstractEventDetectorVO<ED>> extends VoIndividualRequest<AbstractEventDetectorModel<ED>> {
    }

    public static class EventDetectorIndividualResponse extends VoIndividualResponse<AbstractEventDetectorModel<?>> {
    }

    public static class EventDetectorBulkRequest<ED extends AbstractEventDetectorVO<ED>> extends BulkRequest<VoAction, AbstractEventDetectorModel<ED>, EventDetectorIndividualRequest<ED>> {
    }

    public static class EventDetectorBulkResponse extends BulkResponse<EventDetectorIndividualResponse> {
    }

    private final TemporaryResourceManager<EventDetectorBulkResponse, AbstractRestV2Exception> bulkResourceManager;

    @ApiOperation(value = "Bulk get/create/update/delete event detectors",
            notes = "User must have read permission for the data point or edit permission for the data source",
            consumes=MediaTypes.CSV_VALUE)
    @RequestMapping(method = RequestMethod.POST, value="/bulk", consumes=MediaTypes.CSV_VALUE)
    public ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> bulkEventDetectorOperationCSV(
            @RequestBody
            List<AbstractEventDetectorModel<T>> eds,

            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        EventDetectorBulkRequest<T> bulkRequest = new EventDetectorBulkRequest<T>();

        bulkRequest.setRequests(eds.stream().map(actionAndModel -> {
            AbstractEventDetectorModel<T> ed = actionAndModel;
            VoAction action = actionAndModel.getAction();
            String originalXid = actionAndModel.getOriginalXid();
            if (originalXid == null && ed != null) {
                originalXid = ed.getXid();
            }

            EventDetectorIndividualRequest<T> request = new EventDetectorIndividualRequest<T>();
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
    public ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> bulkEventDetectorOperation(
            @RequestBody
            EventDetectorBulkRequest<T> requestBody,

            @ApiParam(value = "Restart the source to load in the changes", required = false, defaultValue="true", allowMultiple = false)
            @RequestParam(required=false, defaultValue="true") boolean restart,

            @AuthenticationPrincipal
            User user,

            UriComponentsBuilder builder) {

        VoAction defaultAction = requestBody.getAction();
        AbstractEventDetectorModel<T> defaultBody = requestBody.getBody();
        List<EventDetectorIndividualRequest<T>> requests = requestBody.getRequests();

        if (requests == null) {
            throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "requests"));
        } else if (requests.isEmpty()) {
            throw new BadRequestException(new TranslatableMessage("rest.error.cantBeEmpty", "requests"));
        }

        String resourceId = requestBody.getId();
        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> responseBody = bulkResourceManager.newTemporaryResource(
                RESOURCE_TYPE_BULK_EVENT_DETECTOR, resourceId, user.getId(), expiration, timeout, (resource, taskUser) -> {

                    EventDetectorBulkResponse bulkResponse = new EventDetectorBulkResponse();
                    int i = 0;

                    resource.progressOrSuccess(bulkResponse, i++, requests.size());

                    for (EventDetectorIndividualRequest<T> request : requests) {
                        UriComponentsBuilder reqBuilder = UriComponentsBuilder.newInstance();
                        EventDetectorIndividualResponse individualResponse = doIndividualRequest(request, restart, defaultAction, defaultBody, taskUser, reqBuilder);
                        bulkResponse.addResponse(individualResponse);

                        resource.progressOrSuccess(bulkResponse, i++, requests.size());
                    }

                    return null;
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/full-event-detectors/bulk/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Get a list of current bulk event detector operations",
            notes = "User can only get their own bulk operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/bulk")
    public MappingJacksonValue getBulkDataPointOperations(
            @AuthenticationPrincipal
            User user,

            HttpServletRequest request) {

        List<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> preFiltered =
                this.bulkResourceManager.list().stream()
                .filter((tr) -> user.hasAdminRole() || user.getId() == tr.getUserId())
                .collect(Collectors.toList());

        List<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> results;
        ASTNode query = RQLUtils.parseRQLtoAST(request.getQueryString());
        if (query != null) {
            results = query.accept(new RQLToObjectListQuery<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>>(), preFiltered);
        }else {
            results = preFiltered;
        }

        ListWithTotal<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> result =
                new ListWithTotal<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>>() {

            @Override
            public List<TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception>> getItems() {
                return results;
            }

            @Override
            public int getTotal() {
                return results.size();
            }
        };

        // hide result property by setting a view
        MappingJacksonValue resultWithView = new MappingJacksonValue(result);
        resultWithView.setSerializationView(Object.class);
        return resultWithView;
    }

    @ApiOperation(value = "Update a bulk  event detector operation using its id", notes = "Only allowed operation is to change the status to CANCELLED. " +
            "User can only update their own bulk operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.PUT, value="/bulk/{id}")
    public TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> updateBulkDataPointOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @RequestBody
            TemporaryResourceStatusUpdate body,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminRole() && user.getId() != resource.getUserId()) {
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
    public TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> getBulkDataPointTagOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @AuthenticationPrincipal
            User user) {

        TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminRole() && user.getId() != resource.getUserId()) {
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

        TemporaryResource<EventDetectorBulkResponse, AbstractRestV2Exception> resource = bulkResourceManager.get(id);

        if (!user.hasAdminRole() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        resource.remove();
    }

    //TODO improve performance by tracking all data sources that need to be restarted and restart at the end?
    private EventDetectorIndividualResponse doIndividualRequest(EventDetectorIndividualRequest<T> request, boolean restart, VoAction defaultAction, AbstractEventDetectorModel<T> defaultBody, User user, UriComponentsBuilder builder) {
        EventDetectorIndividualResponse result = new EventDetectorIndividualResponse();

        try {
            String xid = request.getXid();
            result.setXid(xid);

            VoAction action = request.getAction() == null ? defaultAction : request.getAction();
            if (action == null) {
                throw new BadRequestException(new TranslatableMessage("rest.error.mustNotBeNull", "action"));
            }
            result.setAction(action);

            AbstractEventDetectorModel<T> body = request.getBody() == null ? defaultBody : request.getBody();

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

    private StreamedArrayWithTotal doQuery(ASTNode rql, User user, Function<T, ?> toModel) {
        //If we are admin or have overall data source permission we can view all
        if (user.hasAdminRole()) {
            return new StreamedVORqlQueryWithTotal<>(service, rql, toModel);
        } else {
            return new StreamedVORqlQueryWithTotal<>(service, rql, user, toModel);
        }
    }
}
