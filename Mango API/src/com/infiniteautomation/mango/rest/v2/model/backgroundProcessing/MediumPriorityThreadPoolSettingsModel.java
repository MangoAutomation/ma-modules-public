/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.backgroundProcessing;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.rt.maint.BackgroundProcessing;

/**
 *
 * @author Terry Packer
 */
public class MediumPriorityThreadPoolSettingsModel extends ThreadPoolSettingsModel {

    public MediumPriorityThreadPoolSettingsModel(){ }

    public MediumPriorityThreadPoolSettingsModel(Integer corePoolSize, Integer maximumPoolSize,
            Integer activeCount, Integer largestPoolSize) {
        super(corePoolSize, maximumPoolSize, activeCount, largestPoolSize);
    }

    @Override
    public void validate(ProcessResult result) {
        //Validate the settings
        int currentCorePoolSize = SystemSettingsDao.instance.getIntValue(SystemSettingsDao.MED_PRI_CORE_POOL_SIZE);
        validate(result, currentCorePoolSize, getCorePoolSize() == null ? currentCorePoolSize : getCorePoolSize());

        if((getCorePoolSize() != null) && (getCorePoolSize() < BackgroundProcessing.MED_PRI_MAX_POOL_SIZE_MIN)){
            //Test to ensure we aren't setting too low
            result.addContextualMessage("corePoolSize", "validate.greaterThanOrEqualTo", BackgroundProcessing.MED_PRI_MAX_POOL_SIZE_MIN);
        }

    }

}
