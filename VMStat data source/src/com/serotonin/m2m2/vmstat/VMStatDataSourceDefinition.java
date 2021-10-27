/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class VMStatDataSourceDefinition extends DataSourceDefinition<VMStatDataSourceVO> {

    public static final String DATA_SOURCE_TYPE = "VMSTAT";

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.vmstat";
    }

    @Override
    protected VMStatDataSourceVO createDataSourceVO() {
        return new VMStatDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, VMStatDataSourceVO ds) {
        if (ds.getPollSeconds() < 1)
            response.addContextualMessage("pollSeconds", "validate.greaterThanZero", ds.getPollSeconds());

        if (!VMStatDataSourceVO.OUTPUT_SCALE_CODES.isValidId(ds.getOutputScale()))
            response.addContextualMessage("outputScale", "validate.invalidValue");
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo) {
        if (!(dsvo instanceof VMStatDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");
        VMStatPointLocatorVO pl = dpvo.getPointLocator();
        if (!VMStatPointLocatorVO.ATTRIBUTE_CODES.isValidId(pl.getAttributeId()))
            response.addContextualMessage("attributeId", "validate.invalidValue");
    }

}
