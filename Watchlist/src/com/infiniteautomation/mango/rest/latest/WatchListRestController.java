/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.infiniteautomation.mango.db.tables.Users;
import com.infiniteautomation.mango.rest.latest.bulk.VoAction;
import com.infiniteautomation.mango.rest.latest.model.ActionAndModel;
import com.infiniteautomation.mango.rest.latest.model.JSONStreamedArray;
import com.infiniteautomation.mango.rest.latest.model.ListWithTotal;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArray;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.WatchListModel;
import com.infiniteautomation.mango.rest.latest.model.WatchListModelMapping;
import com.infiniteautomation.mango.rest.latest.model.WatchListSummaryModel;
import com.infiniteautomation.mango.rest.latest.model.WatchListSummaryModelMapping;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.infiniteautomation.mango.rest.latest.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.WatchListService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.watchlist.WatchListEmportDefinition;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 */
@Api(value="Watch Lists")
@RestController
@RequestMapping("/watch-lists")
public class WatchListRestController {

    private static Logger LOG = LoggerFactory.getLogger(WatchListRestController.class);

    private final WatchListService service;
    private final WatchListModelMapping mapping;
    private final WatchListSummaryModelMapping summaryMapping;
    private final BiFunction<DataPointVO, PermissionHolder, DataPointModel> mapDataPoint;
    private final BiFunction<EventInstanceVO, PermissionHolder, EventInstanceModel> mapEventInstance;
    private final RestModelMapper mapper;

    private final Map<String, Function<Object, Object>> valueConverters;
    private final Map<String, Field<?>> fieldMap;

    @Autowired
    public WatchListRestController(WatchListService service, WatchListModelMapping mapping,
            WatchListSummaryModelMapping summaryMapping, RestModelMapper modelMapper){
        this.service = service;
        this.mapping = mapping;
        this.summaryMapping = summaryMapping;
        this.mapper = modelMapper;

        this.mapDataPoint = (vo, user) -> {
            return modelMapper.map(vo, DataPointModel.class, user);
        };

        this.mapEventInstance = (vo, user) -> {
            return modelMapper.map(vo, EventInstanceModel.class, user);
        };

        this.valueConverters = new HashMap<>();

        //Setup any exposed special query aliases to map model fields to db columns
        this.fieldMap = new HashMap<>();
        this.fieldMap.put("username", Users.USERS.username);
    }

    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface WatchListQueryResult extends ListWithTotal<WatchListSummaryModel> {
    }
    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface WatchListPointsResult extends ListWithTotal<DataPointModel> {
    }
    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface WatchListEventsResult extends ListWithTotal<EventInstanceModel> {
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal PermissionHolder user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        export.put(WatchListEmportDefinition.elementId, new StreamedSeroJsonVORqlQuery<>(service, rql, null, fieldMap, valueConverters));
        return export;
    }

    @ApiOperation(value = "Query WatchLists", response=WatchListQueryResult.class)
    @RequestMapping(method = RequestMethod.GET)
    public StreamedArrayWithTotal query(
            HttpServletRequest request,
            @ApiParam(value="User", required=true)
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return doQuery(rql, user);
    }

    @ApiOperation(
            value = "Get a WatchList"
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public WatchListModel get(
            @ApiParam(value = "XID of Watch List to get", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return mapping.map(service.get(xid), user, mapper);
    }

    @ApiOperation(value = "Create New WatchList")
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<WatchListModel> create(
            @RequestBody WatchListModel model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        WatchListVO vo = service.insert(mapping.unmap(model, user, mapper));
        URI location = builder.path("/watch-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update a WatchList",
            notes = "Requires edit permission"
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{xid}")
    public ResponseEntity<WatchListModel> update(
            @ApiParam(value = "XID of WatchList to update", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "WatchList of update", required = true, allowMultiple = false)
            @RequestBody WatchListModel model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        WatchListVO vo = service.update(xid, mapping.unmap(model, user, mapper));
        URI location = builder.path("/watch-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a WatchList",
            notes = "Requires edit permission"
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<WatchListModel> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated WatchList", required = true)
            @PatchVORequestBody(
                    service=WatchListService.class,
                    modelClass=WatchListModel.class)
            WatchListModel model,

            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {


        WatchListVO vo = service.update(xid, mapping.unmap(model, user, mapper));

        URI location = builder.path("/watch-lists/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(mapping.map(vo, user, mapper), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a WatchList"
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public ResponseEntity<WatchListModel> delete(
            @ApiParam(value = "XID of WatchList to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder) {
        return ResponseEntity.ok(mapping.map(service.delete(xid), user, mapper));
    }

    @ApiOperation(
            value = "Get Data Points for a Watchlist",
            response=WatchListPointsResult.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/data-points")
    public WatchListPointsQueryDataPageStream getDataPoints(
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {
        return new WatchListPointsQueryDataPageStream(xid, dp -> mapDataPoint.apply(dp, user));
    }

    @ApiOperation(
            value = "Get Data Points for a Watchlist for bulk import via CSV",
            notes = "Adds an additional action and originalXid column",
            response=String.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/data-points", produces="text/csv")
    public WatchListPointsQueryDataPageStream getDataPointsWithAction(
            @PathVariable String xid,
            @AuthenticationPrincipal PermissionHolder user) {
        return new WatchListPointsQueryDataPageStream(xid, dp -> {
            ActionAndModel<DataPointModel> actionAndModel = new ActionAndModel<>();
            actionAndModel.setAction(VoAction.UPDATE);
            actionAndModel.setOriginalXid(dp.getXid());
            actionAndModel.setModel(mapDataPoint.apply(dp, user));
            return actionAndModel;
        });
    }

    @ApiOperation(
            value = "Get Data Point Events for a Watchlist",
            response = WatchListEventsResult.class
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}/events")
    public WatchListEventQueryArrayWithTotal getEvents(
            @PathVariable String xid,
            @RequestParam(value = "limit", required = false)  Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset,
            @AuthenticationPrincipal PermissionHolder user) {
        return new WatchListEventQueryArrayWithTotal(xid, limit, offset, event -> mapEventInstance.apply(event, user));
    }

    /**
     * Class to stream data points and restrict based on permissions
     * @author Terry Packer
     *
     */
    @JsonPropertyOrder({"items", "total"}) // ensures that items is serialized first so total gets correct value
    class WatchListEventQueryArrayWithTotal implements StreamedArrayWithTotal {

        private final String xid;
        private final Integer limit;
        private final Integer offset;
        private int count = 0;
        private final Function<EventInstanceVO, ?> mapToModel;

        public WatchListEventQueryArrayWithTotal(String xid, Integer limit, Integer offset, Function<EventInstanceVO, ?> mapToModel) {
            this.xid = xid;
            this.limit = limit;
            this.offset = offset;
            this.mapToModel = mapToModel;
        }

        @Override
        public StreamedArray getItems() {
            return (JSONStreamedArray) (jgen) -> {
                service.getPointEvents(xid, limit, offset, (dp) -> {
                    Object model = mapToModel.apply(dp);
                    try {
                        jgen.writeObject(model);
                        count++;
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
            };
        }

        @Override
        public int getTotal() {
            return count;
        }

    }

    /**
     * Class to stream data points and restrict based on permissions
     * @author Terry Packer
     *
     */
    @JsonPropertyOrder({"items", "total"}) // ensures that items is serialized first so total gets correct value
    class WatchListPointsQueryDataPageStream implements StreamedArrayWithTotal {

        private final String xid;
        private int pointCount = 0;
        private final Function<DataPointVO, ?> mapToModel;

        public WatchListPointsQueryDataPageStream(String xid, Function<DataPointVO, ?> mapToModel) {
            this.xid = xid;
            this.mapToModel = mapToModel;
        }

        @Override
        public StreamedArray getItems() {
            return (JSONStreamedArray) (jgen) -> {
                service.getDataPoints(xid, (dp) -> {
                    Object model = mapToModel.apply(dp);
                    try {
                        jgen.writeObject(model);
                        pointCount++;
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
            };
        }

        @Override
        public int getTotal() {
            return pointCount;
        }

    }

    /**
     *
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, PermissionHolder user) {
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, fieldMap, valueConverters, (item) -> {
            return summaryMapping.map(item, user, mapper);
        });
    }

}
