/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.model.datasource.RuntimeStatusModel;
import com.infiniteautomation.mango.rest.v2.model.datasource.RuntimeStatusModel.PollStatus;
import com.infiniteautomation.mango.rest.v2.model.event.EventTypeVOModel;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.serotonin.db.pair.LongLongPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Data source runtime status information")
@RestController()
@RequestMapping("/data-source-status")
public class DataSourceRuntimeStatusRestController {

    private final DataSourceService<?> service;
    
    @Autowired
    public DataSourceRuntimeStatusRestController(DataSourceService<?> service) {
        this.service = service;
    }
    
    @ApiOperation(
            value = "Query all available event types",
            notes = "Not specific to any reference ids, results come back based on type/sub-type combinations",
            response=EventTypeVOModel.class,
            responseContainer="List")
    @RequestMapping(method = RequestMethod.GET, value = "/{xid}")
    public RuntimeStatusModel getRuntimeStatus(            
            @ApiParam(value = "Valid Data Source XID", required = true, allowMultiple = false)
            @PathVariable String xid,
            @AuthenticationPrincipal User user) {
        DataSourceVO<?> vo = service.get(xid, user);
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
                latestAbortedPolls.add(new PollStatus(new Date(poll), -1));
            model.setLatestAbortedPolls(latestAbortedPolls);
        }
        
        return model;
    }
}
