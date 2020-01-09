/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.model.backgroundProcessing.HighPriorityThreadPoolSettingsModel;
import com.infiniteautomation.mango.rest.v2.model.backgroundProcessing.LowPriorityThreadPoolSettingsModel;
import com.infiniteautomation.mango.rest.v2.model.backgroundProcessing.MediumPriorityThreadPoolSettingsModel;
import com.infiniteautomation.mango.rest.v2.model.backgroundProcessing.ThreadPoolSettingsModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Terry Packer
 *
 */
@Api(value="Background Processing status and settings")
@RestController
@RequestMapping("/background-processing")
public class BackgroundProcessingRestController {

    @ApiOperation(value = "Get the High Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
    @RequestMapping(method = RequestMethod.GET, value = "/high-priority-thread-pool-settings")
    public ThreadPoolSettingsModel getHighPriorityThreadPoolSettings(
            @AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Common.timer.getExecutorService();
        int corePoolSize = executor.getCorePoolSize();
        int maximumPoolSize = executor.getMaximumPoolSize();
        int activeCount = executor.getActiveCount();
        int largestPoolSize = executor.getLargestPoolSize();

        return new HighPriorityThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    }

    @ApiOperation(value = "Update high priority queue settings")
    @RequestMapping(method = RequestMethod.PUT, value = "/high-priority-thread-pool-settings")
    public ThreadPoolSettingsModel setHighPrioritySettings(

            @ApiParam(value = "Settings", required = true, allowMultiple = false)
            @RequestBody
            HighPriorityThreadPoolSettingsModel model,
            @AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();
        model.ensureValid();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Common.timer.getExecutorService();

        if(model.getCorePoolSize() != null){
            executor.setCorePoolSize(model.getCorePoolSize());
            SystemSettingsDao.instance.setIntValue(SystemSettingsDao.HIGH_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
        }else{
            //Get the info for the user
            int corePoolSize = executor.getCorePoolSize();
            model.setCorePoolSize(corePoolSize);
        }
        if(model.getMaximumPoolSize() != null){
            executor.setMaximumPoolSize(model.getMaximumPoolSize());
            SystemSettingsDao.instance.setIntValue(SystemSettingsDao.HIGH_PRI_MAX_POOL_SIZE, model.getMaximumPoolSize());
        }else{
            //Get the info for the user
            int maximumPoolSize = executor.getMaximumPoolSize();
            model.setMaximumPoolSize(maximumPoolSize);
        }
        //Get the settings for the model
        int activeCount = executor.getActiveCount();
        int largestPoolSize = executor.getLargestPoolSize();
        model.setActiveCount(activeCount);
        model.setLargestPoolSize(largestPoolSize);
        return model;
    }

    @ApiOperation(value = "Get the Medium Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
    @RequestMapping(method = RequestMethod.GET, value = "/medium-priority-thread-pool-settings")
    public ThreadPoolSettingsModel getMediumPriorityThreadPoolSettings(@AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();
        int corePoolSize = Common.backgroundProcessing.getMediumPriorityServiceCorePoolSize();
        int maximumPoolSize = Common.backgroundProcessing.getMediumPriorityServiceMaximumPoolSize();
        int activeCount = Common.backgroundProcessing.getMediumPriorityServiceActiveCount();
        int largestPoolSize = Common.backgroundProcessing.getMediumPriorityServiceLargestPoolSize();

        return new MediumPriorityThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    }

    @ApiOperation(
            value = "Update medium priority queue settings",
            notes = "Only corePoolSize setting will change the pool size as it is setup as coreSize=maxSize"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/medium-priority-thread-pool-settings")
    public ThreadPoolSettingsModel setMediumPrioritySettings(

            @ApiParam(value = "Settings", required = true, allowMultiple = false)
            @RequestBody
            MediumPriorityThreadPoolSettingsModel model,
            @AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();
        model.ensureValid();

        if(model.getCorePoolSize() != null){
            Common.backgroundProcessing.setMediumPriorityServiceCorePoolSize(model.getCorePoolSize());
            SystemSettingsDao.instance.setIntValue(SystemSettingsDao.MED_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
        }else{
            //Get the info for the user
            int corePoolSize = Common.backgroundProcessing.getMediumPriorityServiceCorePoolSize();
            model.setCorePoolSize(corePoolSize);
        }
        if(model.getMaximumPoolSize() == null){
            //Get the info for the user
            int maximumPoolSize = Common.backgroundProcessing.getMediumPriorityServiceMaximumPoolSize();
            model.setMaximumPoolSize(maximumPoolSize);
        }else {

        }
        return model;
    }

    @ApiOperation(value = "Get the Low Priority Service Thread Pool Settings", notes="active count and largest pool size are read only")
    @RequestMapping(method = RequestMethod.GET, value = "/low-priority-thread-pool-settings")
    public ThreadPoolSettingsModel getLowPriorityThreadPoolSettings(@AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();
        int corePoolSize = Common.backgroundProcessing.getLowPriorityServiceCorePoolSize();
        int maximumPoolSize = Common.backgroundProcessing.getLowPriorityServiceMaximumPoolSize();
        int activeCount = Common.backgroundProcessing.getLowPriorityServiceActiveCount();
        int largestPoolSize = Common.backgroundProcessing.getLowPriorityServiceLargestPoolSize();

        return new LowPriorityThreadPoolSettingsModel(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    }

    @ApiOperation(
            value = "Update low priority service settings",
            notes = "Only corePoolSize setting will change the pool size as it is setup as coreSize=maxSize"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/low-priority-thread-pool-settings")
    public ThreadPoolSettingsModel setLowPrioritySettings(

            @ApiParam(value = "Settings", required = true, allowMultiple = false)
            @RequestBody
            LowPriorityThreadPoolSettingsModel model,
            @AuthenticationPrincipal User user) {
        user.ensureHasAdminRole();
        model.ensureValid();

        if(model.getCorePoolSize() != null){
            Common.backgroundProcessing.setLowPriorityServiceCorePoolSize(model.getCorePoolSize());
            SystemSettingsDao.instance.setIntValue(SystemSettingsDao.LOW_PRI_CORE_POOL_SIZE, model.getCorePoolSize());
        }else{
            //Get the info for the user
            int corePoolSize = Common.backgroundProcessing.getLowPriorityServiceCorePoolSize();
            model.setCorePoolSize(corePoolSize);
        }
        if(model.getMaximumPoolSize() == null){
            //Get the info for the user
            int maximumPoolSize = Common.backgroundProcessing.getLowPriorityServiceMaximumPoolSize();
            model.setMaximumPoolSize(maximumPoolSize);
        }
        //Get the settings for the model
        int activeCount = Common.backgroundProcessing.getLowPriorityServiceActiveCount();
        int largestPoolSize = Common.backgroundProcessing.getLowPriorityServiceLargestPoolSize();
        model.setActiveCount(activeCount);
        model.setLargestPoolSize(largestPoolSize);
        return model;
    }
}
