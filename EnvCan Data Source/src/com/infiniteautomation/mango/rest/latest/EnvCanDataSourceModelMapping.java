/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.EnvCanDataSourceModel;
import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.envcan.EnvCanDataSourceDefinition;
import com.serotonin.m2m2.envcan.EnvCanDataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class EnvCanDataSourceModelMapping implements RestModelJacksonMapping<EnvCanDataSourceVO, EnvCanDataSourceModel> {

    @Override
    public Class<? extends EnvCanDataSourceVO> fromClass() {
        return EnvCanDataSourceVO.class;
    }

    @Override
    public Class<? extends EnvCanDataSourceModel> toClass() {
        return EnvCanDataSourceModel.class;
    }

    @Override
    public EnvCanDataSourceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new EnvCanDataSourceModel((EnvCanDataSourceVO)from);
    }

    @Override
    public String getTypeName() {
        return EnvCanDataSourceDefinition.DATA_SOURCE_TYPE;
    }

}
