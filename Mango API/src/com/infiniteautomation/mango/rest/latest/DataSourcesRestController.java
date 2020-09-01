/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.StreamedArrayWithTotal;
import com.infiniteautomation.mango.rest.latest.model.StreamedSeroJsonVORqlQuery;
import com.infiniteautomation.mango.rest.latest.model.StreamedVORqlQueryWithTotal;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.DataSourceWithPointsExport;
import com.infiniteautomation.mango.rest.latest.model.datasource.ReadOnlyDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.RuntimeStatusModel;
import com.infiniteautomation.mango.rest.latest.model.datasource.RuntimeStatusModel.PollStatus;
import com.infiniteautomation.mango.rest.latest.patch.PatchVORequestBody;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.RQLUtils;
import com.serotonin.db.pair.LongLongPair;
import com.serotonin.json.type.JsonStreamedArray;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.MediaTypes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 *
 * @author Terry Packer
 *
 */
@Api(value="Data source controller")
@RestController
@RequestMapping("/data-sources")
public class DataSourcesRestController {

    private final DataSourceService service;
    private final BiFunction<DataSourceVO, User, AbstractDataSourceModel<?>> map;
    private final DataPointService dataPointService;
    private final DataPointDao dataPointDao;

    @Autowired
    public DataSourcesRestController(final DataSourceService service, final DataPointDao dataPointDao, final RestModelMapper modelMapper, final DataPointService dataPointService) {
        this.service = service;
        this.map = (vo, user) -> {
            if(service.hasEditPermission(user, vo)) {
                return modelMapper.map(vo, AbstractDataSourceModel.class, user);
            }else {
                return new ReadOnlyDataSourceModel(vo);
            }
        };
        this.dataPointService = dataPointService;
        this.dataPointDao = dataPointDao;
    }

    @ApiOperation(
            value = "Query Data Sources",
            notes = "RQL Formatted Query",
            responseContainer="List"
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
            value = "Get Data Source by XID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value="/{xid}")
    public AbstractDataSourceModel<?> get(
            @ApiParam(value = "XID of Data Source", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(xid), user);
    }

    @ApiOperation(
            value = "Get Data Source by ID",
            notes = ""
            )
    @RequestMapping(method = RequestMethod.GET, value="/by-id/{id}")
    public AbstractDataSourceModel<?> getById(
            @ApiParam(value = "ID of Data Source", required = true, allowMultiple = false)
            @PathVariable int id,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.get(id), user);
    }

    @ApiOperation(value = "Create data source")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AbstractDataSourceModel<?>> save(
            @RequestBody(required=true) AbstractDataSourceModel<? extends DataSourceVO> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        DataSourceVO vo = this.service.insert(model.toVO());
        URI location = builder.path("/data-sources/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> update(
            @PathVariable String xid,
            @RequestBody(required=true) AbstractDataSourceModel<? extends DataSourceVO> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder,
            HttpServletRequest request) {

        DataSourceVO vo = this.service.update(xid, model.toVO());
        URI location = builder.path("/data-sources/{xid}").buildAndExpand(new Object[]{vo.getXid()}).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Partially update a data source",
            notes = "Requires edit permission"
            )
    @RequestMapping(method = RequestMethod.PATCH, value = "/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> partialUpdate(
            @PathVariable String xid,

            @ApiParam(value = "Updated data source", required = true)
            @PatchVORequestBody(
                    service=DataSourceService.class,
                    modelClass=AbstractDataSourceModel.class)
            AbstractDataSourceModel<? extends DataSourceVO> model,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataSourceVO vo = service.update(xid, model.toVO());

        URI location = builder.path("/data-sources/{xid}").buildAndExpand(vo.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(vo, user), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a data source",
            notes = "",
            response=AbstractDataSourceModel.class
            )
    @RequestMapping(method = RequestMethod.DELETE, value="/{xid}")
    public AbstractDataSourceModel<?> delete(
            @ApiParam(value = "XID of data source to delete", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {
        return map.apply(service.delete(xid), user);
    }

    @ApiOperation(value = "Enable/disable/restart a data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/enable-disable/{xid}")
    public void enableDisable(
            @PathVariable String xid,

            @ApiParam(value = "Enable or disable the data source", required = true, allowMultiple = false)
            @RequestParam(required=true) boolean enabled,

            @ApiParam(value = "Restart the data source, enabled must equal true", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean restart,

            @AuthenticationPrincipal User user) {
        service.restart(xid, enabled, restart);
    }


    @ApiOperation(
            value = "Get runtime status for data source",
            notes = "Only polling data sources have runtime status",
            response=RuntimeStatusModel.class)
    @RequestMapping(method = RequestMethod.GET, value = "/status/{xid}")
    public RuntimeStatusModel getRuntimeStatus(
            @ApiParam(value = "Valid Data Source XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        DataSourceVO vo = service.get(xid);
        RuntimeStatusModel model = new RuntimeStatusModel();
        DataSourceRT<?> ds = Common.runtimeManager.getRunningDataSource(vo.getId());

        if ((ds != null)&&(ds instanceof PollingDataSource)){
            List<LongLongPair> list = ((PollingDataSource<?>)ds).getLatestPollTimes();
            List<PollStatus> latestPolls = new ArrayList<>();
            for(LongLongPair poll : list){
                latestPolls.add(new PollStatus(new Date(poll.getKey()), poll.getValue()));
            }
            model.setLatestPolls(latestPolls);
            List<PollStatus> latestAbortedPolls = new ArrayList<>();
            List<Long> aborted = ((PollingDataSource<?>)ds).getLatestAbortedPollTimes();
            for(Long poll : aborted)
                latestAbortedPolls.add(new PollStatus(new Date(poll), -1L));
            model.setLatestAbortedPolls(latestAbortedPolls);
        }

        return model;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import",
            notes = "Optionally include data points",
            response=RuntimeStatusModel.class)
    @RequestMapping(method = RequestMethod.GET, value = "/export/{xid}", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, Object> exportDataSource(
            @ApiParam(value = "Valid Data Source XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @ApiParam(value = "Include data points")
            @RequestParam(value = "includePoints", required = false, defaultValue="false")
            Boolean includePoints,
            @AuthenticationPrincipal User user) {

        DataSourceVO vo = service.get(xid);
        Map<String,Object> export = new LinkedHashMap<>();
        export.put("dataSources", Collections.singletonList(vo));

        if(includePoints) {
            export.put("dataPoints", DataPointDao.getInstance().getDataPoints(vo.getId()));
        }
        return export;
    }

    @ApiOperation(value = "Copy data source", notes="Copy the data source and its points with optional new XID and Name.")
    @RequestMapping(method = RequestMethod.PUT, value = "/copy/{xid}")
    public ResponseEntity<AbstractDataSourceModel<?>> copy(
            @PathVariable String xid,
            @ApiParam(value = "Copy's new XID", required = false, defaultValue="null", allowMultiple = false)
            @RequestParam(required=false, defaultValue="null") String copyXid,
            @ApiParam(value = "Copy's name", required = false, defaultValue="null", allowMultiple = false)
            @RequestParam(required=false, defaultValue="null") String copyName,
            @ApiParam(value = "Device name for copied points", required = false, defaultValue="null", allowMultiple = false)
            @RequestParam(required=false, defaultValue="null") String copyDeviceName,
            @ApiParam(value = "Enable/disabled state of data source", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean enabled,
            @ApiParam(value = "Copy data points", required = false, defaultValue="false", allowMultiple = false)
            @RequestParam(required=false, defaultValue="false") boolean copyPoints,

            @AuthenticationPrincipal User user,
            UriComponentsBuilder builder) {

        DataSourceVO copy = service.copy(xid, copyXid, copyName, copyDeviceName, enabled, copyPoints);

        URI location = builder.path("/data-sources/{xid}").buildAndExpand(copy.getXid()).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(map.apply(copy, user), headers, HttpStatus.OK);
    }


    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public Map<String, JsonStreamedArray> exportQuery(HttpServletRequest request, @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());

        Map<String, JsonStreamedArray> export = new HashMap<>();
        export.put("dataSources", new StreamedSeroJsonVORqlQuery<>(service, rql, null, null, null));
        return export;
    }

    @ApiOperation(
            value = "Export formatted for Configuration Import by supplying an RQL query",
            notes = "User must have read permission")
    @RequestMapping(method = RequestMethod.GET, value = "/export-with-points", produces = MediaTypes.SEROTONIN_JSON_VALUE)
    public DataSourceWithPointsExport exportQueryWithPoints(HttpServletRequest request, @AuthenticationPrincipal User user) {
        ASTNode rql = RQLUtils.parseRQLtoAST(request.getQueryString());
        return new DataSourceWithPointsExport(service, rql, dataPointService, dataPointDao);
    }

    @ApiOperation(value = "Force Poll", notes="Must have edit access and be a polling style data source")
    @RequestMapping(method = RequestMethod.PUT, value = "/force-poll/{xid}")
    public void forcePoll(
            @PathVariable String xid,
            UriComponentsBuilder builder) {
        service.forceDataSourcePoll(xid);
    }

    /**
     * Perform a query
     * @param rql
     * @param user
     * @return
     */
    private StreamedArrayWithTotal doQuery(ASTNode rql, User user) {
        return new StreamedVORqlQueryWithTotal<>(service, rql, null, null, null, vo -> map.apply(vo, user));
    }

}
