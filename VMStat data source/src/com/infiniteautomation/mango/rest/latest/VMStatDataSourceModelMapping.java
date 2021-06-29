/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.VMStatDataSourceModel;
import com.serotonin.m2m2.vmstat.VMStatDataSourceDefinition;
import com.serotonin.m2m2.vmstat.VMStatDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class VMStatDataSourceModelMapping implements RestModelJacksonMapping<VMStatDataSourceVO, VMStatDataSourceModel> {

    @Override
    public Class<? extends VMStatDataSourceVO> fromClass() {
        return VMStatDataSourceVO.class;
    }

    @Override
    public Class<? extends VMStatDataSourceModel> toClass() {
        return VMStatDataSourceModel.class;
    }

    @Override
    public VMStatDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new VMStatDataSourceModel((VMStatDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return VMStatDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
