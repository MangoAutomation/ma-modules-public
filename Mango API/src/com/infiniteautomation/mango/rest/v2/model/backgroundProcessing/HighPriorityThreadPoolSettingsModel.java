/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.backgroundProcessing;

import java.util.concurrent.ThreadPoolExecutor;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.rt.maint.BackgroundProcessing;

/**
 *
 * @author Terry Packer
 */
public class HighPriorityThreadPoolSettingsModel extends ThreadPoolSettingsModel {

    public HighPriorityThreadPoolSettingsModel(){ }

    public HighPriorityThreadPoolSettingsModel(Integer corePoolSize, Integer maximumPoolSize,
            Integer activeCount, Integer largestPoolSize) {
        super(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    }

    @Override
    public void validate(ProcessResult response) {
        //Validate the settings
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Common.timer.getExecutorService();
        int currentCorePoolSize = executor.getCorePoolSize();
        int currentMaxPoolSize = executor.getMaximumPoolSize();
        validate(response, currentCorePoolSize, currentMaxPoolSize);

        if((getMaximumPoolSize() != null)&&(getMaximumPoolSize() < BackgroundProcessing.HIGH_PRI_MAX_POOL_SIZE_MIN)){
            //Test to ensure we aren't setting too low
            response.addContextualMessage("corePoolSize", "validate.greaterThanOrEqualTo", BackgroundProcessing.HIGH_PRI_MAX_POOL_SIZE_MIN);
        }
    }

}
