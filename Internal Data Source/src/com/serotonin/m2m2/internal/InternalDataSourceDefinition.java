/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.PollingDataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

public class InternalDataSourceDefinition extends PollingDataSourceDefinition<InternalDataSourceVO> {

    public static final String DATA_SOURCE_TYPE = "INTERNAL";

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.internal";
    }

    @Override
    protected InternalDataSourceVO createDataSourceVO() {
        return new InternalDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, InternalDataSourceVO vo, PermissionHolder holder) {
        super.validate(response, vo, holder);
        if (!StringUtils.isEmpty(vo.getCreatePointsPattern())) {
            try {
                Pattern.compile(vo.getCreatePointsPattern());
            } catch(PatternSyntaxException e) {
                response.addContextualMessage("createPointsPattern", "validate.invalidRegex");
            }
        }
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo,
            PermissionHolder user) {
        if (!(dsvo instanceof InternalDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");
        InternalPointLocatorVO pl = dpvo.getPointLocator();
        try {
            Common.MONITORED_VALUES.getMonitor(pl.getMonitorId());
        }catch(Exception e) {
            response.addContextualMessage("monitorId", "internal.missingMonitor", pl.getMonitorId());
        }
    }

}
